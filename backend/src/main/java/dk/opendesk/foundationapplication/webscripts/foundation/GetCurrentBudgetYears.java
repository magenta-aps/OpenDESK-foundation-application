/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.BudgetYear;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.List;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetCurrentBudgetYears extends JacksonBackedWebscript{

    @Override
    protected List<BudgetYear> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<BudgetYear> budgetYears = getFoundationBean().getCurrentBudgetYears();
        return budgetYears;
    }
    
    
    
}
