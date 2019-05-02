/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import java.util.Set;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
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
        System.out.println(authorities);
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
    
    public void testGetAggregateUserFields() throws Exception {
        Application app1 = getApplicationBean().getApplication(TestUtils.application1);

        String olduser = AuthenticationUtil.getFullyAuthenticatedUser();
        try{
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_BRANCH_WRITE);
        Application change1 = Utilities.buildChange(app1).changeField("17").setValue(10).done().build();
            Application returnedApp = get(Application.class, "/application/"+TestUtils.application1.getId(), TestUtils.USER_BRANCH_WRITE);
            ApplicationFieldValue returnedAggregateField = getField(returnedApp, "17");

            assertNotNull(returnedAggregateField);
            assertNull(returnedAggregateField.getSingleValue());
            
            post(change1, null, "/application/"+TestUtils.application1.getId(), 200, TestUtils.USER_BRANCH_WRITE);
            
            returnedApp = get(Application.class, "/application/"+TestUtils.application1.getId(), TestUtils.USER_BRANCH_WRITE);
            
            returnedAggregateField = getField(returnedApp, "17");
            assertNotNull(returnedAggregateField);
            assertEquals(10, returnedAggregateField.getSingleValue());
        
            AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.USER_BRANCH_WRITE_ALL_READ);
        Application change2 = Utilities.buildChange(app1).changeField("17").setValue(20).done().build();
        //AuthenticationUtil.runAs(() -> {
            Application returnedApp1 = get(Application.class, "/application/"+TestUtils.application1.getId(), TestUtils.USER_BRANCH_WRITE_ALL_READ);
            
            returnedAggregateField = getField(returnedApp1, "17");

            assertNotNull(returnedAggregateField);
            assertNull(returnedAggregateField.getSingleValue());
            
            post(change2, null, "/application/"+TestUtils.application1.getId(), 200, TestUtils.USER_BRANCH_WRITE_ALL_READ);
            
            returnedApp1 = get(Application.class, "/application/"+TestUtils.application1.getId(), TestUtils.USER_BRANCH_WRITE_ALL_READ);
            
            returnedAggregateField = getField(returnedApp1, "17");

            assertNotNull(returnedAggregateField);
            assertEquals(20, returnedAggregateField.getSingleValue());
        //}, TestUtils.USER_BRANCH_WRITE_ALL_READ);
        }finally{
            AuthenticationUtil.setFullyAuthenticatedUser(olduser);
        }
    }
    
    public void testGetAggregateData() throws Exception{
        testGetAggregateUserFields();
        Application app1 = getApplicationBean().getApplication(TestUtils.application1);
        Application change = Utilities.buildChange(app1).setState(TestUtils.w1StateAccessRef).build();
        post(change, "/application/"+TestUtils.application1.getId());
        Application returnedApp1 = get(Application.class, "/application/"+TestUtils.application1.getId());
        ApplicationFieldValue returnedAggregateField = getField(returnedApp1, "17");
        assertNotNull(returnedAggregateField);
        assertEquals(15, returnedAggregateField.getSingleValue());
    }
    
    public ApplicationFieldValue getField(Application app, String id){
        for (ApplicationBlock block : app.getBlocks()) {
                for (ApplicationFieldValue field : block.getFields()) {
                    if (field.getId().equals(id)) {
                        return field;
                    }
                }
            }
        return null;
    }

}
