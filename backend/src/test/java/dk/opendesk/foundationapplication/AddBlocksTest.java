package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
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

        ApplicationBlock uniqueBlockNoFields = new ApplicationBlock();
        uniqueBlockNoFields.setId("A");

        ApplicationBlock uniqueBlockUniqueFields = new ApplicationBlock();
        uniqueBlockUniqueFields.setId("B");
        ApplicationFieldValue field1 = new ApplicationFieldValue();
        field1.setId("b");
        uniqueBlockUniqueFields.setFields(Collections.singletonList(field1));

        List<ApplicationBlock> blocks = Arrays.asList(new ApplicationBlock[]{uniqueBlockNoFields, uniqueBlockUniqueFields});
        Map<String, Serializable> param = new HashMap<>();
        param.put(PARAM_BLOCKS, (Serializable) blocks);
        Action action = getServiceRegistry().getActionService().createAction("addBlocks", param);
        getServiceRegistry().getActionService().executeAction(action, appRef);

        List<ApplicationBlock> actBlocks = getApplicationBean().getApplication(appRef).getBlocks();
        assertEquals(noBlocks + 2, actBlocks.size());

        ApplicationBlock blockA = null;
        ApplicationBlock blockB = null;
        for (ApplicationBlock block : actBlocks) {
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

        ApplicationBlock uniqueBlockNoFields = new ApplicationBlock();
        uniqueBlockNoFields.setId("A");

        ApplicationBlock uniqueBlockDuplicateField = new ApplicationBlock();
        uniqueBlockDuplicateField.setId("B");
        ApplicationFieldValue field3 = new ApplicationFieldValue();
        ApplicationFieldValue field4 = new ApplicationFieldValue();
        field3.setId("a");
        field4.setId("1"); //already exists
        uniqueBlockDuplicateField.setFields(Arrays.asList(new ApplicationFieldValue[]{field3, field4}));

        List<ApplicationBlock> blocks = Arrays.asList(new ApplicationBlock[]{uniqueBlockNoFields, uniqueBlockDuplicateField});
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

        List<ApplicationBlock> oldBlocks = getApplicationBean().getApplication(appRef).getBlocks();


        ApplicationBlock uniqueBlockNoFields = new ApplicationBlock();
        uniqueBlockNoFields.setId("A");

        ApplicationBlock duplicateBlockUniqueFields = new ApplicationBlock();
        duplicateBlockUniqueFields.setId("1"); //already exists
        ApplicationFieldValue field5 = new ApplicationFieldValue();
        ApplicationFieldValue field6 = new ApplicationFieldValue();
        field5.setId("a");
        field6.setId("b");
        duplicateBlockUniqueFields.setFields(Arrays.asList(new ApplicationFieldValue[]{field5,field6}));

        List<ApplicationBlock> blocks = Arrays.asList(new ApplicationBlock[]{uniqueBlockNoFields, duplicateBlockUniqueFields});
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
