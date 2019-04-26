/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.StateSummary;
import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import java.util.List;


/**
 *
 * @author martin
 */
public class TestDemoData extends AbstractTestClass{

    public TestDemoData() {
        super("/foundation/demodata");
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
    
    public void testSetupDemoData() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"));
        
        assertEquals(4, getBranchBean().getBranchSummaries().size());
        assertEquals(2, getBudgetBean().getBudgetYearSummaries().size());
        for(BudgetYearSummary budgetYear : getBudgetBean().getBudgetYearSummaries()){
            if(budgetYear.getTitle().equals(ResetDemoData.BUDGETYEAR1_TITLE)){
                assertEquals(6, getBudgetBean().getBudgetSummaries(budgetYear));
            }else if (budgetYear.getTitle().equals(ResetDemoData.BUDGETYEAR2_TITLE)){
                assertEquals(0, getBudgetBean().getBudgetSummaries(budgetYear));
            }
        }
        
        assertEquals(3, getWorkflowBean().getWorkflowSummaries().size());
        assertEquals(16, getApplicationBean().getApplicationSummaries().size());
    }
    
    
    
    public void testSetupDemoDataDanva() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"), "danva");
        
        assertEquals(1, getBranchBean().getBranchSummaries().size());
        assertEquals(0, getBudgetBean().getBudgetYearSummaries().size());
        
        
        assertEquals(1, getWorkflowBean().getWorkflowSummaries().size());
        assertEquals(0, getApplicationBean().getApplicationSummaries().size());

    }
    
    
    public void testSetupDemoDataDanvaDataAlreadyExists() throws Exception {
        try{
            
        }catch(RuntimeException ex){
            post(new JSONObject().append("doesnt", "matter"), "danva");
            fail("Did not throw Exception");
        }
    }


    public void testAddBlocksActionExecutingOnDanvaData() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"), "danva");

        NodeRef branchRef = getBranchBean().getBranchSummaries().get(0).asNodeRef();
        NodeRef workflowRef = getBranchBean().getBranchWorkflow(branchRef);
        Workflow workflow = getWorkflowBean().getWorkflow(workflowRef);
        List<StateSummary> stateList = workflow.getStates();
        NodeRef meeting1 = null;
        NodeRef expanded = null;
        for (StateSummary stateSummary : stateList) {
            if (stateSummary.getTitle().equals("Bestyrelsesmøde 1")) {
                meeting1 = stateSummary.asNodeRef();
            }
            if (stateSummary.getTitle().equals("Udvidet ansøgning")) {
                expanded = stateSummary.asNodeRef();
            }
        }

        //adding application
        assertEquals(0, getApplicationBean().getApplicationSummaries().size());
        ApplicationReference appRef = getApplicationBean().addNewApplication("1", branchRef, null,"title");
        assertEquals(1, getApplicationBean().getApplicationSummaries().size());

        //move application to meeting1
        Application change = new Application();
        change.parseRef(appRef.asNodeRef());
        StateReference state = getWorkflowBean().getStateReference(meeting1);
        change.setState(state);

        getApplicationBean().updateApplication(change);

        assertEquals(0, getApplicationBean().getApplication(appRef.asNodeRef()).getBlocks().size());

        //move application to meeting1
        change = new Application();
        change.parseRef(appRef.asNodeRef());
        state = getWorkflowBean().getStateReference(expanded);
        change.setState(state);

        getApplicationBean().updateApplication(change);

        //asserting blocks and fields got created
        assertEquals(2, getApplicationBean().getApplication(appRef.asNodeRef()).getBlocks().size());

        ApplicationBlock newBlock1 = getApplicationBean().getApplication(appRef.asNodeRef()).getBlocks().get(0);
        assertEquals( "additional_info", newBlock1.getId());
        assertEquals(8, newBlock1.getFields().size());

        ApplicationBlock newBlock2 = getApplicationBean().getApplication(appRef.asNodeRef()).getBlocks().get(1);
        assertEquals( "files", newBlock2.getId());
        assertEquals(12, newBlock2.getFields().size());


    }
    
}
