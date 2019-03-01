/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class AddBudgetYear extends JacksonBackedWebscript {

    @Override
    protected Reference doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        BudgetYearSummary budgetYear = getRequestAs(BudgetYearSummary.class);
        return Reference.from(getFoundationBean().addNewBudgetYear(budgetYear.getTitle(), budgetYear.getTitle(), budgetYear.getStartDate(), budgetYear.getEndDate()));
    }
    
    
}
