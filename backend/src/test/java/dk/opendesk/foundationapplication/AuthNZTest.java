/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import static dk.opendesk.foundationapplication.TestUtils.TEST_USER;
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
        TestUtils.setupSimpleFlow(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
    }

    public void testUserCreated() {
        PersonService ps = getServiceRegistry().getPersonService();
        NodeRef testUserRef = ps.getPerson(TEST_USER);
        PersonService.PersonInfo testUser = ps.getPerson(testUserRef);
        assertEquals(TestUtils.TEST_USER, testUser.getUserName());
        assertEquals(TestUtils.TEST_USER_FIRST_NAME, testUser.getFirstName());
    }

    public void testPermissionApplication1() throws Exception {
        Application change = Utilities.buildChange(getApplicationBean().getApplication(TestUtils.application1)).setTitle("my new title").build();
        AuthenticationUtil.setFullyAuthenticatedUser(TestUtils.TEST_USER);
        assertEquals(TestUtils.application1, getApplicationBean().getApplicationReference(TestUtils.application1).asNodeRef());

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
