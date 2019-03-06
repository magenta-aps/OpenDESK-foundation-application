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

import static dk.opendesk.foundationapplication.actions.AddBlocksToApplicationAction.EXCEPTION_BLOCK_OVERLAP;
import static dk.opendesk.foundationapplication.actions.AddFieldsToApplicationAction.EXCEPTION_FIELD_OVERLAP;
import static dk.opendesk.foundationapplication.actions.AddBlocksToApplicationAction.PARAM_BLOCKS;

public class AddBlocksTest extends AbstractTestClass {

    public AddBlocksTest() {
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


    public void testAddUniqueBlocks() throws Exception {
        NodeRef appRef = TestUtils.application1;
        int noBlocks = getApplicationBean().getApplication(appRef).getBlocks().size();

        ApplicationPropertiesContainer uniqueBlockNoFields = new ApplicationPropertiesContainer();
        uniqueBlockNoFields.setId("A");

        ApplicationPropertiesContainer uniqueBlockUniqueFields = new ApplicationPropertiesContainer();
        uniqueBlockUniqueFields.setId("B");
        ApplicationPropertyValue field1 = new ApplicationPropertyValue();
        field1.setId("b");
        uniqueBlockUniqueFields.setFields(Collections.singletonList(field1));

        List<ApplicationPropertiesContainer> blocks = Arrays.asList(new ApplicationPropertiesContainer[]{uniqueBlockNoFields, uniqueBlockUniqueFields});
        Map<String, Serializable> param = new HashMap<>();
        param.put(PARAM_BLOCKS, (Serializable) blocks);
        Action action = getServiceRegistry().getActionService().createAction("addBlocks", param);
        getServiceRegistry().getActionService().executeAction(action, appRef);

        List<ApplicationPropertiesContainer> actBlocks = getApplicationBean().getApplication(appRef).getBlocks();
        assertEquals(noBlocks + 2, actBlocks.size());

        ApplicationPropertiesContainer blockA = null;
        ApplicationPropertiesContainer blockB = null;
        for (ApplicationPropertiesContainer block : actBlocks) {
            if ("A".equals(block.getId())) {
                blockA = block;
            }
            if ("B".equals(block.getId())) {
                blockB = block;
            }
        }
        assertNotNull(blockA);
        assertNotNull(blockB);
        assertEquals("b", blockB.getFields().get(0).getId());



    }

    public void testAddUniqueBlocksWithDuplicateFields() {
        NodeRef appRef = TestUtils.application1;

        ApplicationPropertiesContainer uniqueBlockNoFields = new ApplicationPropertiesContainer();
        uniqueBlockNoFields.setId("A");

        ApplicationPropertiesContainer uniqueBlockDuplicateField = new ApplicationPropertiesContainer();
        uniqueBlockDuplicateField.setId("B");
        ApplicationPropertyValue field3 = new ApplicationPropertyValue();
        ApplicationPropertyValue field4 = new ApplicationPropertyValue();
        field3.setId("a");
        field4.setId("1"); //already exists
        uniqueBlockDuplicateField.setFields(Arrays.asList(new ApplicationPropertyValue[]{field3, field4}));

        List<ApplicationPropertiesContainer> blocks = Arrays.asList(new ApplicationPropertiesContainer[]{uniqueBlockNoFields, uniqueBlockDuplicateField});
        Map<String, Serializable> param = new HashMap<>();
        param.put(PARAM_BLOCKS, (Serializable) blocks);
        Action action = getServiceRegistry().getActionService().createAction("addBlocks", param);
        try {
            getServiceRegistry().getActionService().executeAction(action, appRef);
            fail();
        } catch (AlfrescoRuntimeException e) {
            assertTrue(e.getMessage().contains(EXCEPTION_FIELD_OVERLAP));
        }
    }


    public void testAddDuplicateBlock() throws Exception {
        NodeRef appRef = TestUtils.application1;

        List<ApplicationPropertiesContainer> oldBlocks = getApplicationBean().getApplication(appRef).getBlocks();


        ApplicationPropertiesContainer uniqueBlockNoFields = new ApplicationPropertiesContainer();
        uniqueBlockNoFields.setId("A");

        ApplicationPropertiesContainer duplicateBlockUniqueFields = new ApplicationPropertiesContainer();
        duplicateBlockUniqueFields.setId("1"); //already exists
        ApplicationPropertyValue field5 = new ApplicationPropertyValue();
        ApplicationPropertyValue field6 = new ApplicationPropertyValue();
        field5.setId("a");
        field6.setId("b");
        duplicateBlockUniqueFields.setFields(Arrays.asList(new ApplicationPropertyValue[]{field5,field6}));

        List<ApplicationPropertiesContainer> blocks = Arrays.asList(new ApplicationPropertiesContainer[]{uniqueBlockNoFields, duplicateBlockUniqueFields});
        Map<String, Serializable> param = new HashMap<>();
        param.put(PARAM_BLOCKS, (Serializable) blocks);
        Action action = getServiceRegistry().getActionService().createAction("addBlocks", param);
        try {
            getServiceRegistry().getActionService().executeAction(action, appRef);
            fail();
        } catch (AlfrescoRuntimeException e) {
            assertTrue(e.getMessage().contains(EXCEPTION_BLOCK_OVERLAP));
        }

    }
}
