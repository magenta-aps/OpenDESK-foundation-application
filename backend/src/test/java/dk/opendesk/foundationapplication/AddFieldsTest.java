package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.opendesk.foundationapplication.actions.AddFieldsToApplicationAction.*;

public class AddFieldsTest extends AbstractTestClass {

    public AddFieldsTest() {
        super("");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
    }

    public void testAddUniqueFieldsExistingBlock() throws Exception {
        NodeRef appRef = TestUtils.application1;
        ApplicationPropertiesContainer blockBefore = getApplicationBean().getApplication(appRef).getBlocks().get(0);
        int noFields = blockBefore.getFields().size();

        ApplicationPropertyValue field1 = new ApplicationPropertyValue();
        ApplicationPropertyValue field2 = new ApplicationPropertyValue();
        field1.setId("a");
        field2.setId("b");

        List<ApplicationPropertyValue> fields = Arrays.asList(new ApplicationPropertyValue[]{field1,field2});

        Map<String, Serializable> params = new HashMap<>();
        params.put(PARAM_FIELDS, (Serializable) fields);
        params.put(PARAM_BLOCK_ID, blockBefore.getId());
        Action action = getServiceRegistry().getActionService().createAction("addFields", params);
        getServiceRegistry().getActionService().executeAction(action, appRef);

        ApplicationPropertiesContainer blockAfter = getApplicationBean().getApplication(appRef).getBlocks().get(0);
        assertEquals(noFields + 2, blockAfter.getFields().size());

        ApplicationPropertyValue fieldA = null;
        ApplicationPropertyValue fieldB = null;
        for (ApplicationPropertyValue field : blockAfter.getFields()) {
            if ("a".equals(field.getId())) {
                fieldA = field;
            }
            if ("b".equals(field.getId())) {
                fieldB = field;
            }
        }
        assertNotNull(fieldA);
        assertNotNull(fieldB);
    }

    public void testAddDuplicateFieldsExistingBlock() throws Exception {
        NodeRef appRef = TestUtils.application1;
        ApplicationPropertiesContainer blockBefore = getApplicationBean().getApplication(appRef).getBlocks().get(0);

        ApplicationPropertyValue field1 = new ApplicationPropertyValue();
        ApplicationPropertyValue field2 = new ApplicationPropertyValue();
        field1.setId("a");
        field2.setId(blockBefore.getFields().get(0).getId());

        List<ApplicationPropertyValue> fields = Arrays.asList(new ApplicationPropertyValue[]{field1,field2});

        Map<String, Serializable> params = new HashMap<>();
        params.put(PARAM_FIELDS, (Serializable) fields);
        params.put(PARAM_BLOCK_ID, blockBefore.getId());
        Action action = getServiceRegistry().getActionService().createAction("addFields", params);
        try {
            getServiceRegistry().getActionService().executeAction(action, appRef);
            fail();
        } catch (AlfrescoRuntimeException e) {
            assertTrue(e.getMessage().contains(EXCEPTION_FIELD_OVERLAP));
        }

    }

    public void testAddUniqueFieldsWrongBlockId() {
        NodeRef appRef = TestUtils.application1;

        ApplicationPropertyValue field = new ApplicationPropertyValue();
        field.setId("a");

        Map<String, Serializable> params = new HashMap<>();
        params.put(PARAM_FIELDS, (Serializable) Collections.singletonList(field));
        params.put(PARAM_BLOCK_ID, "A");
        Action action = getServiceRegistry().getActionService().createAction("addFields", params);
        try {
            getServiceRegistry().getActionService().executeAction(action, appRef);
            fail();
        } catch (AlfrescoRuntimeException e) {
            assertTrue(e.getMessage().contains(EXCEPTION_BLOCK_NOT_FOUND));
        }
    }

}