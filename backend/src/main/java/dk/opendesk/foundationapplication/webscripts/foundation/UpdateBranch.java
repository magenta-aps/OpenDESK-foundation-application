/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class UpdateBranch extends JacksonBackedWebscript {

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String branchID = getUrlParams().get("branchID");
        Branch branch = getRequestAs(Branch.class);
        resolveNodeRef(branch, branchID);
        
        getBranchBean().updateBranch(branch);
        return new JSONObject().put("status", "OK");
    }

}
