/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONObject;

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
    
}
