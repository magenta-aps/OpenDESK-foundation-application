/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.List;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetBudgets extends JacksonBackedWebscript{
    
    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected List<Budget> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<Budget> budgets = foundationBean.getBudgets();
        return budgets;
    }
    
    
    
}
