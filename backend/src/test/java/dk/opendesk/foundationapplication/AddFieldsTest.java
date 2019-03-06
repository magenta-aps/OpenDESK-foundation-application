package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.*;

import static dk.opendesk.foundationapplication.actions.AddFieldsToApplicationAction.*;

public class AddFieldsTest extends AbstractTestClass {

    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public AddFieldsTest() {
        super("");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(serviceRegistry);
    }

    public void testAddUniqueFieldsExistingBlock() throws Exception {
        NodeRef appRef = TestUtils.application1;
        ApplicationPropertiesContainer blockBefore = foundationBean.getApplication(appRef).getBlocks().get(0);
        int noFields = blockBefore.getFields().size();

        ApplicationPropertyValue field1 = new ApplicationPropertyValue();
        ApplicationPropertyValue field2 = new ApplicationPropertyValue();
        field1.setId("a");
        field2.setId("b");

        List<ApplicationPropertyValue> fields = Arrays.asList(new ApplicationPropertyValue[]{field1,field2});

        Map<String, Serializable> params = new HashMap<>();
        params.put(PARAM_FIELDS, (Serializable) fields);
        params.put(PARAM_BLOCK_ID, blockBefore.getId());
        Action action = serviceRegistry.getActionService().createAction("addFields", params);
        serviceRegistry.getActionService().executeAction(action, appRef);

        ApplicationPropertiesContainer blockAfter = foundationBean.getApplication(appRef).getBlocks().get(0);
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
        ApplicationPropertiesContainer blockBefore = foundationBean.getApplication(appRef).getBlocks().get(0);

        ApplicationPropertyValue field1 = new ApplicationPropertyValue();
        ApplicationPropertyValue field2 = new ApplicationPropertyValue();
        field1.setId("a");
        field2.setId(blockBefore.getFields().get(0).getId());

        List<ApplicationPropertyValue> fields = Arrays.asList(new ApplicationPropertyValue[]{field1,field2});

        Map<String, Serializable> params = new HashMap<>();
        params.put(PARAM_FIELDS, (Serializable) fields);
        params.put(PARAM_BLOCK_ID, blockBefore.getId());
        Action action = serviceRegistry.getActionService().createAction("addFields", params);
        try {
            serviceRegistry.getActionService().executeAction(action, appRef);
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
        Action action = serviceRegistry.getActionService().createAction("addFields", params);
        try {
            serviceRegistry.getActionService().executeAction(action, appRef);
            fail();
        } catch (AlfrescoRuntimeException e) {
            assertTrue(e.getMessage().contains(EXCEPTION_BLOCK_NOT_FOUND));
        }
    }

}
