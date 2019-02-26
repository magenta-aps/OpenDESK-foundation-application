/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.BudgetYear;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetBudgetYear extends JacksonBackedWebscript{

    @Override
    protected BudgetYear doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String budgetYearID = getUrlParams().get("budgetYearID");
        BudgetYear budgetYear = getFoundationBean().getBudgetYear(Reference.refFromID(budgetYearID));
        return budgetYear;
    }
    
    
    
}
