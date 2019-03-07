/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetBudget extends JacksonBackedWebscript{

    @Override
    protected Budget doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String budgetID = getUrlParams().get("budgetID");
        Budget budget = getBudgetBean().getBudget(Reference.refFromID(budgetID));
        return budget;
    }
    
    
    
}
