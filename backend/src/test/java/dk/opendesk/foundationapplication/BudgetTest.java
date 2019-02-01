/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetSummary;
import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class BudgetTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public BudgetTest() {
        super("/foundation/budget");
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
    
    public void testGetBudget() throws Exception{
        Budget budget1 = get(Budget.class, TestUtils.budgetRef1.getId());
        
        assertEquals(TestUtils.BUDGET1_AMOUNT, budget1.getAmountTotal());
        assertEquals(TestUtils.BUDGET1_NAME+TestUtils.TITLE_POSTFIX, budget1.getTitle());
        
        Long expectedAmount = TestUtils.APPLICATION1_AMOUNT+TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, budget1.getAmountNominated());
        assertEquals(Long.valueOf(0), budget1.getAmountAccepted());
        assertEquals(Long.valueOf(0), budget1.getAmountApplied());
        assertEquals(TestUtils.BUDGET1_AMOUNT, budget1.getAmountAvailable());
        assertEquals(Long.valueOf(0), budget1.getAmountClosed());
        assertEquals(2, budget1.getApplications().size());
        
        Budget budget2 = get(Budget.class, TestUtils.budgetRef2.getId());
        
        assertEquals(TestUtils.BUDGET2_AMOUNT, budget2.getAmountTotal());
        assertEquals(TestUtils.BUDGET2_NAME+TestUtils.TITLE_POSTFIX, budget2.getTitle());
        
        assertEquals(Long.valueOf(0), budget2.getAmountNominated());
        assertEquals(Long.valueOf(0), budget2.getAmountAccepted());
        assertEquals(Long.valueOf(0), budget2.getAmountApplied());
        assertEquals(TestUtils.BUDGET2_AMOUNT, budget2.getAmountAvailable());
        assertEquals(Long.valueOf(0), budget2.getAmountClosed());
        assertEquals(0, budget2.getApplications().size());

        
    }
    
}