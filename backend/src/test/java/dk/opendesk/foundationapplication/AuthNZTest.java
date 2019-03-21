/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
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

    public void testPermissionApplication1() throws Exception {
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

}
