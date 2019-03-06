/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetYearReference;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class AddBudget extends JacksonBackedWebscript {

    @Override
    protected Reference doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String budgetYearID = getUrlParams().get("budgetYearID");
        BudgetYearReference budgetYear = new BudgetYearReference();
        budgetYear.setNodeID(budgetYearID);
        Budget budget = getRequestAs(Budget.class);
        return Reference.from(getBudgetBean().addNewBudget(budgetYear.asNodeRef(), budget.getTitle(), budget.getTitle(), budget.getAmountTotal()));
    }
    
    
}
