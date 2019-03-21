/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.AssertionFailedError;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author martin
 */
public class BranchTest extends AbstractTestClass {

    public BranchTest() {
        super("/foundation/branch");
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
    
    public Reference testAddBranchWebScript() throws Exception{
        assertEquals(1, getBranchBean().getBranches().size());
        assertEquals(TestUtils.BRANCH_NAME+TestUtils.TITLE_POSTFIX, getBranchBean().getBranchSummaries().get(0).getTitle());
        JSONObject requestData = new JSONObject();
        requestData.put("title", "My new branch");
        Reference ref = post(requestData, Reference.class);
        assertEquals(2, getBranchBean().getBranches().size());
        return ref;
    }
    
    public void testGetBranches() throws Exception{
        List<BranchSummary> restSummaries = get(List.class, BranchSummary.class);
        List<BranchSummary> beanSummaries = getBranchBean().getBranchSummaries();
        
        containsSameElements(restSummaries, beanSummaries);
        assertEquals(1, restSummaries.size());
        assertEquals(TestUtils.BRANCH_NAME+TestUtils.TITLE_POSTFIX, restSummaries.get(0).getTitle());
    }
    
    public void testUpdateBranch() throws IOException, JSONException{
        String myNewTitle = "newTitle";
        List<BranchSummary> restSummaries = get(List.class, BranchSummary.class);
        assertEquals(1, restSummaries.size());
        BranchSummary summary = restSummaries.get(0);
        assertEquals(TestUtils.BRANCH_NAME+TestUtils.TITLE_POSTFIX, summary.getTitle());
        summary.setTitle(myNewTitle);
        post(summary, summary.getNodeID());
        restSummaries = get(List.class, BranchSummary.class);
        assertEquals(1, restSummaries.size());
        summary = restSummaries.get(0);
        assertEquals(myNewTitle, summary.getTitle());
        
    }


    public void testAddWorkflowToBranch() throws Exception{
        String workflowTitle = "TestWorkFlow";
        Reference ref = testAddBranchWebScript();
        BranchSummary branch = getBranchBean().getBranch(ref.asNodeRef());
        assertNull(branch.getWorkflowRef());
        
        
        BranchSummary summary = new BranchSummary();
        summary.parseRef(ref.asNodeRef());
        NodeRef newWorkflowRef = getWorkflowBean().addNewWorkflow(workflowTitle, workflowTitle);
        WorkflowReference workflow = new WorkflowReference();
        workflow.parseRef(newWorkflowRef);
        summary.setWorkflowRef(workflow);
        post(summary, ref.getNodeID());
        
        
        
        branch = getBranchBean().getBranch(ref.asNodeRef());
        assertEquals(getWorkflowBean().getWorkflowReference(newWorkflowRef), branch.getWorkflowRef());
    }
    
    public void testGetBranchApplications() throws Exception{
        List<ApplicationSummary> applications = get(List.class, ApplicationSummary.class, TestUtils.branchRef.getId()+"/applications");
        for(ApplicationSummary app : applications){
            switch(app.getTitle()){
                case TestUtils.APPLICATION1_NAME: continue;
                case TestUtils.APPLICATION2_NAME: continue;
                default:
                    fail("Application list did not contain the expected applications");
            }
        }
    }
    
    public void testGetBranchApplicationsByBudget() throws Exception{
        List<ApplicationSummary> applications = get(List.class, ApplicationSummary.class, TestUtils.branchRef.getId()+"/applications");
        assertEquals(2, applications.size());
        applications = get(List.class, ApplicationSummary.class, TestUtils.branchRef.getId()+"/applications?budgetID="+TestUtils.budgetRef1.getId());
        assertEquals(2, applications.size());
        applications = get(List.class, ApplicationSummary.class, TestUtils.branchRef.getId()+"/applications?budgetID="+TestUtils.budgetRef2.getId());
        assertEquals(0, applications.size());
        
        Application change = new Application();
        change.parseRef(TestUtils.application2);
        BudgetReference newBudget = new BudgetReference();
        newBudget.parseRef(TestUtils.budgetRef2);
        change.setBudget(newBudget);
        getApplicationBean().updateApplication(change);
        
        applications = get(List.class, ApplicationSummary.class, TestUtils.branchRef.getId()+"/applications?budgetID="+TestUtils.budgetRef1.getId());
        assertEquals(1, applications.size());
        applications = get(List.class, ApplicationSummary.class, TestUtils.branchRef.getId()+"/applications?budgetID="+TestUtils.budgetRef2.getId());
        assertEquals(1, applications.size());
    }
    
    
    public void testContainsSameElement(){
        //Completely equal
        List<Integer> c1 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,4,4,5,6,7,8,9}));
        List<Integer> c2 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,4,4,5,6,7,8,9}));
        
        //Contains different elements
        List<Integer> c3 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,3,4,5,6,7,8,9}));
        List<Integer> c4 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,4,4,5,6,7,8,9}));
        
        //Contains same elements but in different order
        List<Integer> c5 = new ArrayList<>(Arrays.asList(new Integer[]{1,3,2,3,4,4,6,5,7,8,9}));
        List<Integer> c6 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,4,4,5,6,7,8,9}));
        
        //Contains same elements but in different amounts
        List<Integer> c7 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,4,4,5,6,7,8,9}));
        List<Integer> c8 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,4,5,6,7,8,9}));
        
        //Contains same elements but in different amounts
        List<Integer> c9 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3}));
        List<Integer> c10 = new ArrayList<>(Arrays.asList(new Integer[]{1,2,3,3,3,3,3,3}));
        
        containsSameElements(c1, c2);
        containsSameElements(c5, c6);
        
        try{
            containsSameElements(c3, c4);
            fail("This call should have failed as the lists does not contain the same elements");
        }catch(AssertionFailedError e){
            
        }
        
        try{
            containsSameElements(c7, c8);
            fail("This call should have failed as the lists does not contain the same elements");
        }catch(AssertionFailedError e){
            
        }        
        
        try{
            containsSameElements(c9, c10);
            fail("This call should have failed as the lists does not contain the same elements");
        }catch(AssertionFailedError e){
            
        }
        
    }
    
}
