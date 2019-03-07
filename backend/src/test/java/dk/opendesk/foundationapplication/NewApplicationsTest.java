/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 *
 * @author martin
 */
public class NewApplicationsTest extends AbstractTestClass{

    public NewApplicationsTest(){
        super("/foundation/incomming");
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
    
    public void testGetNewApplications() throws Exception{
        List<ApplicationSummary> newApplications = getApplicationBean().getNewApplicationSummaries();
        List<ApplicationSummary> newApplicationsRest = get(List.class, ApplicationSummary.class);
        
        assertEquals(1, newApplications.size());
        containsSameElements(newApplications, newApplicationsRest);
        
        assertEquals(TestUtils.APPLICATION3_NAME, newApplicationsRest.get(0).getTitle());
        assertEquals(TestUtils.APPLICATION3_AMOUNT, newApplicationsRest.get(0).totalAmount().getValue());
    }
    
}
