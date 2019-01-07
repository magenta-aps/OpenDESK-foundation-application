/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class NewApplicationsTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public NewApplicationsTest(){
        super("/foundation/incomming");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(serviceRegistry);
    }
    
    public void testGetNewApplications() throws Exception{
        List<ApplicationSummary> newApplications = foundationBean.getNewApplicationSummaries();
        List<ApplicationSummary> newApplicationsRest = get(List.class, ApplicationSummary.class);
        
        assertEquals(1, newApplications.size());
        containsSameElements(newApplications, newApplicationsRest);
        
        assertEquals(TestUtils.APPLICATION3_NAME+TestUtils.TITLE_POSTFIX, newApplicationsRest.get(0).getTitle());
        assertEquals(TestUtils.APPLICATION3_AMOUNT, newApplicationsRest.get(0).getAmountApplied());
    }
    
}
