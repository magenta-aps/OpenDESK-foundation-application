/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.FoundationWebScript;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class AddBranch extends FoundationWebScript {
    
    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }
    
    @Override
    protected JSONObject doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String branchTitle = getContentString("title");
        return new JSONObject().put("reference", foundationBean.addNewBranch(branchTitle, branchTitle));
    }
    
}
