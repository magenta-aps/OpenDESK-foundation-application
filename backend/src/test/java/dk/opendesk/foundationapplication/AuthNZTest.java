/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import static dk.opendesk.foundationapplication.TestUtils.USER_ALL_PERMISSIONS;
import java.util.List;
import java.util.Set;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;

/**
 *
 * @author martin
 */
public class AuthNZTest extends AbstractTestClass {

    public AuthNZTest() {
        super("/foundation");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setDefaultRunAs(AuthenticationUtil.getAdminUserName());
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupFullTestUsers(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
    }
    
    public void testUserCreated() {
        PersonService ps = getServiceRegistry().getPersonService();
        NodeRef testUserRef = ps.getPerson(TestUtils.USER_ALL_PERMISSIONS);
        PersonService.PersonInfo testUser = ps.getPerson(testUserRef);
        assertEquals(TestUtils.USER_ALL_PERMISSIONS, testUser.getUserName());
        assertEquals(TestUtils.USER_ALL_PERMISSIONS, testUser.getFirstName());
    }

    public void testPermissionApplicationForBranch() throws Exception {
        Application change = Utilities.buildChange(getApplicationBean().getApplication(TestUtils.application1)).setTitle("my new title").build();
        Set<String> authorities1 = getServiceRegistry().getAuthorityService().getAuthorities();
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_BRANCH_READ);
        Set<String> authorities2 = getServiceRegistry().getAuthorityService().getAuthorities();
        assertEquals(TestUtils.application1, getApplicationBean().getApplicationReference(TestUtils.application1).asNodeRef());
        assertEquals(TestUtils.application2, getApplicationBean().getApplicationReference(TestUtils.application2).asNodeRef());
        try {
            getApplicationBean().getApplicationReference(TestUtils.application3);
            fail("Exception was not thrown");
        } catch (AccessDeniedException ex) {
        }
        try {
            getApplicationBean().updateApplication(change);
            fail("Exception was not thrown");
        } catch (AccessDeniedException ex) {
        }
    }
    
    public void testNonBranchApplication() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_SINGLE_APPLICATION_WRITE);
        try {
            getApplicationBean().getApplicationReference(TestUtils.application1);
            fail("Exception was not thrown");
        } catch (AccessDeniedException ex) {
        }
        try {
            getApplicationBean().getApplicationReference(TestUtils.application2);
            fail("Exception was not thrown");
        } catch (AccessDeniedException ex) {
        }
        
        assertEquals(TestUtils.application3, getApplicationBean().getApplicationReference(TestUtils.application3).asNodeRef());
    }
    
    public void testApplicationWithWorkflowRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_WORKFLOW_READ);
        Application application = getApplicationBean().getApplication(TestUtils.application1);
        Set<String> authorities = getServiceRegistry().getAuthorityService().getAuthorities();
        assertEquals(TestUtils.application1, application.asNodeRef());
        
        assertNull(application.getBudget());
        assertEquals(TestUtils.workFlowRef1, application.getWorkflow().asNodeRef());
        assertEquals(TestUtils.w1StateRecievedRef, application.getState().asNodeRef());
    }
    
    public void testUpdateBudgetWithBranchRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_BRANCH_WRITE);
        Application application = getApplicationBean().getApplication(TestUtils.application1);
        
        Set<String> auths = getServiceRegistry().getAuthorityService().getAuthorities();
        
        assertEquals(TestUtils.budgetRef1, application.getBudget().asNodeRef());
        
        Application change = Utilities.buildChange(application).setBudget(TestUtils.budgetRef2).build();
        getApplicationBean().updateApplication(change);
        
        application = getApplicationBean().getApplication(TestUtils.application1);
        assertEquals(TestUtils.budgetRef2, application.getBudget().asNodeRef());
    }
    
    public void testGetStatesWithBranchRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_BRANCH_READ);
        
        Workflow workflow = getWorkflowBean().getWorkflow(TestUtils.workFlowRef1);
        assertEquals(4, workflow.getStates().size());
    }
    
    public void testGetWorkflowWithMissingRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_NO_RIGHTS);
        
        try{
            getWorkflowBean().getWorkflow(TestUtils.workFlowRef1);
            fail("Getting workflow with no rights should fail");
        }catch(Exception ex){
            
        }
    }
    
    public void testGetBranchWithNoRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_NO_RIGHTS);
        
        try{
            getBranchBean().getBranch(TestUtils.branchRef1);
            fail("Getting branch with no rights should fail");
        }catch(Exception ex){
            
        }
    }
    
    public void testGetBranchWithInsuficcientRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_BRANCH_READ);
        
        try{
            getBranchBean().getBranch(TestUtils.branchRef2);
            fail("Getting branch with no rights should fail");
        }catch(Exception ex){
            
        }
    }
    
    public void testWorkflow() throws Exception{
        Workflow workflow = get(Workflow.class, "workflow/"+TestUtils.workFlowRef1.getId(), TestUtils.USER_BRANCH_READ);
        assertEquals(TestUtils.workFlowRef1, workflow.asNodeRef());  
    }
    
    public void testGetSingleApplication() throws Exception{
        Application application = get(Application.class, "application/"+TestUtils.application3.getId(), TestUtils.USER_SINGLE_APPLICATION_WRITE);
        assertEquals(TestUtils.application3, application.asNodeRef());
    }
    
    public void testGetSingleApplicationThroughBean() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_SINGLE_APPLICATION_WRITE);
        Application application = getApplicationBean().getApplication(TestUtils.application3);
        assertEquals(TestUtils.application3, application.asNodeRef());
    }
    
    public void testGetSingleApplicationThroughBeanNoRights() throws Exception{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_NO_RIGHTS);
        Application application = getApplicationBean().getApplication(TestUtils.application3);
        assertEquals(TestUtils.application3, application.asNodeRef());
    }
    
    public void testGetActiveWorkflowsAdmin() throws Exception{
        List<WorkflowReference> activeWorkflows = get(List.class, WorkflowReference.class, "activeworkflow", TestUtils.ADMIN_USER);
        assertEquals(2, activeWorkflows.size());   
    }
    
    public void testGetActiveWorkflowsFullRights() throws Exception{
        List<WorkflowReference> activeWorkflows = get(List.class, WorkflowReference.class, "activeworkflow", TestUtils.USER_ALL_PERMISSIONS);
        assertEquals(2, activeWorkflows.size());    
    }
    
    public void testGetActiveWorkflows() throws Exception{
        List<WorkflowReference> activeWorkflows = get(List.class, WorkflowReference.class, "activeworkflow", TestUtils.USER_BRANCH_READ);
        assertEquals(1, activeWorkflows.size()); 
    }
    
    public void testGetActiveWorkflowsNoRights() throws Exception{
        List<WorkflowReference> activeWorkflows = get(List.class, WorkflowReference.class, "activeworkflow", TestUtils.USER_NO_RIGHTS);
        assertEquals(0, activeWorkflows.size());
    }
}
