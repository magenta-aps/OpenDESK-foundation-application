/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class UpdateBudget extends JacksonBackedWebscript {
    public static final String BUDGET_DID_NOT_MATCH = "foundation.service.budget.mismatch";

    private FoundationBean foundationBean;

    public void setFoundationBean (FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }
    
    @Override
    protected JSONObject doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String branchID = getUrlParams().get("budgetID");
        Budget budget = getRequestAs(Budget.class);
        
        resolveNodeRef(budget, branchID);
        
        foundationBean.updateBudget(budget);
        return new JSONObject().put("status", "OK");
    }
    
}
