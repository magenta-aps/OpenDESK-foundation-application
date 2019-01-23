/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.io.IOException;
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
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public BudgetYearTest() {
        super("/foundation/budgetYear");
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
    
    public void testGetBudgetYears() throws Exception{
        List<BudgetYearSummary> summariesRest = get(List.class, BudgetYearSummary.class);
        List<BudgetYearSummary> summaries = foundationBean.getBudgetYearSummaries();

        containsSameElements(summaries, summariesRest);
        assertEquals(1, summariesRest.size());
        assertEquals(TestUtils.BUDGETYEAR1_NAME+TestUtils.TITLE_POSTFIX, summariesRest.get(0).getTitle());
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
    
    
}
