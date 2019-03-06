/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.BudgetSummary;
import dk.opendesk.foundationapplication.DAO.BudgetYearReference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.List;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetBudgets extends JacksonBackedWebscript{

    @Override
    protected List<BudgetSummary> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String budgetYearID = getUrlParams().get("budgetYearID");
        BudgetYearReference budgetYear = new BudgetYearReference();
        budgetYear.setNodeID(budgetYearID);
        List<BudgetSummary> budgets = getBudgetBean().getBudgetSummaries(budgetYear);
        return budgets;
    }
    
    
    
}
