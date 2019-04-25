/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.beans.AuthorityBean;
import dk.opendesk.foundationapplication.beans.BranchBean;
import dk.opendesk.foundationapplication.beans.BudgetBean;
import dk.opendesk.foundationapplication.beans.WorkflowBean;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import java.util.Date;

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

    public void testAddFieldsActionOnDanvaData() throws Exception {
        TestUtils.wipeData(getServiceRegistry());

        ServiceRegistry serviceRegistry = getServiceRegistry();

        ActionBean actionBean = new ActionBean();
        actionBean.setServiceRegistry(serviceRegistry);
        ApplicationBean applicationBean = new ApplicationBean();
        applicationBean.setServiceRegistry(serviceRegistry);
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);

        actionBean.setApplicationBean(applicationBean);

        applicationBean.setActionBean(actionBean);
        applicationBean.setAuthBean(authBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);

        branchBean.setApplicationBean(applicationBean);
        branchBean.setAuthBean(authBean);
        branchBean.setBudgetBean(budgetBean);
        branchBean.setWorkflowBean(workflowBean);

        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setAuthBean(authBean);
        budgetBean.setWorkflowBean(workflowBean);

        workflowBean.setApplicationBean(applicationBean);
        workflowBean.setAuthBean(authBean);

        post(new JSONObject().append("doesnt", "matter"), "danva");

        System.out.println(getApplicationBean().getApplicationSummaries().size());

        NodeRef centralBudgetYear = budgetBean.addNewBudgetYear("centralBudgetYear","title", new Date(), new Date());
        NodeRef centralBudget = budgetBean.addNewBudget(centralBudgetYear, "centralBudget", "title", 1000000L);


        NodeRef branchRef = getBranchBean().getBranchSummaries().get(0).asNodeRef();
        //NodeRef budgetRef = getBudgetBean().getBudgetSummaries(getBudgetBean().getBudgetYearSummaries().get(0)).get(0).asNodeRef();
        //getApplicationBean().addNewApplication("app1", branchRef, budgetRef,"title");
        getApplicationBean().addNewApplication("1", branchRef, centralBudget,"title");
        System.out.println(getApplicationBean().getApplicationSummaries().size());
    }
    
}
