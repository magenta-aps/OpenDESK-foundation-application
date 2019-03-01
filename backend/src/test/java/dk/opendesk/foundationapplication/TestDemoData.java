/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.json.JSONObject;

/**
 *
 * @author martin
 */
public class TestDemoData extends AbstractTestClass{
    
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public TestDemoData() {
        super("/foundation/demodata");
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
    
    public void testSetupDemoData() throws Exception {
        post(new JSONObject().append("doesnt", "matter"));
        
        assertEquals(4, foundationBean.getBranchSummaries().size());
        assertEquals(2, foundationBean.getBudgetYearSummaries().size());
        for(BudgetYearSummary budgetYear : foundationBean.getBudgetYearSummaries()){
            if(budgetYear.getTitle().equals(ResetDemoData.BUDGETYEAR1_TITLE)){
                assertEquals(6, foundationBean.getBudgetSummaries(budgetYear));
            }else if (budgetYear.getTitle().equals(ResetDemoData.BUDGETYEAR2_TITLE)){
                assertEquals(0, foundationBean.getBudgetSummaries(budgetYear));
            }
        }
        
        assertEquals(3, foundationBean.getWorkflowSummaries().size());
        assertEquals(16, foundationBean.getApplicationSummaries().size());
    }
    
}
