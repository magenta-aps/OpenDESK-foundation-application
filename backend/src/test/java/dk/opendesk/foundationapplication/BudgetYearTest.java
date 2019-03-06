/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetSummary;
import dk.opendesk.foundationapplication.DAO.BudgetYear;
import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class BudgetYearTest extends AbstractTestClass{

    public BudgetYearTest() {
        super("/foundation/budgetYear");
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
    
    public void testGetBudgetYears() throws Exception{
        List<BudgetYearSummary> summariesRest = get(List.class, BudgetYearSummary.class);
        List<BudgetYearSummary> summaries = getBudgetBean().getBudgetYearSummaries();

        containsSameElements(summaries, summariesRest);
        assertEquals(1, summariesRest.size());
        assertEquals(TestUtils.BUDGETYEAR1_NAME+TestUtils.TITLE_POSTFIX, summariesRest.get(0).getTitle());
    }
    
    public void testGetBudgetYearsAndBudgets() throws Exception{
        List<BudgetYearSummary> summariesRest = get(List.class, BudgetYearSummary.class);
        for(BudgetYearSummary summary : summariesRest){
            BudgetYear budgetYear = get(BudgetYear.class, summary.getNodeID());
            assertEquals(summary.getNodeRef(), budgetYear.getNodeRef());
            assertEquals(summary.getStartDate(), budgetYear.getStartDate());
            assertEquals(summary.getEndDate(), budgetYear.getEndDate());
            assertEquals(summary.getAmountTotal(), budgetYear.getAmountTotal());
            assertEquals(summary.getTitle(), budgetYear.getTitle());
        }
        
    }
    
    public void testGetBudgetYear() throws Exception{
        BudgetYear budgetYear = get(BudgetYear.class, TestUtils.budgetYearRef1.getId());
        Long expectedAmount = TestUtils.BUDGET1_AMOUNT + TestUtils.BUDGET2_AMOUNT;
        assertEquals(expectedAmount, budgetYear.getAmountTotal());
        assertEquals(Long.valueOf(0), budgetYear.getAmountApplied());
        assertEquals(Long.valueOf(0), budgetYear.getAmountClosed());
        assertEquals(Long.valueOf(0), budgetYear.getAmountAccepted());
        
        expectedAmount = TestUtils.APPLICATION1_AMOUNT+TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, budgetYear.getAmountNominated());
        assertTrue(budgetYear.getEndDate().compareTo(budgetYear.getStartDate()) > 0);
        
        
    }
    
    public void testAddBudgetYear() throws Exception{
        Instant now = Instant.now();
        Instant later = now.plus(7, ChronoUnit.DAYS);
        BudgetYearSummary newBudgetYear = new BudgetYearSummary();
        newBudgetYear.setTitle("newBudgetOfDoom");
        newBudgetYear.setStartDate(Date.from(now));
        newBudgetYear.setEndDate(Date.from(later));
        post(newBudgetYear);
        
        List<BudgetYearSummary> summaries = get(List.class, BudgetYearSummary.class);
        assertEquals(2, summaries.size());
        
        for(BudgetYearSummary summary : summaries){
            if(summary.getTitle().equals(newBudgetYear.getTitle()) && 
                    summary.getStartDate().equals(newBudgetYear.getStartDate()) && 
                    summary.getEndDate().equals(newBudgetYear.getEndDate())){
                return;
            }
        }
        fail("Could not find the new budget year");
    }
    
    public void testGetBudgets() throws Exception{
        List<BudgetSummary> summaries = get(List.class, BudgetSummary.class, TestUtils.budgetYearRef1.getId()+"/budget");
        
         for(BudgetSummary summary : summaries){
             Budget budget = getBudgetBean().getBudget(summary.asNodeRef());
            assertEquals(summary.getNodeRef(), budget.getNodeRef());
            assertEquals(summary.getAmountTotal(), budget.getAmountTotal());
            assertEquals(summary.getTitle(), budget.getTitle());
        }
    }
    
    
}