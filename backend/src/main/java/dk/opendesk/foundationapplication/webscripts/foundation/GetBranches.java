/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.FoundationArrayWebScript;
import java.util.HashSet;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetBranches extends FoundationArrayWebScript{
    
    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected JSONArray doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<BranchSummary> branchList = foundationBean.getBranchSummaries();
        //JSONArray array = new JSONArray(branchList.toArray(new BranchSummary[branchList.size()]));
        JSONArray toReturn = new JSONArray();
        for(BranchSummary branch : branchList){
            toReturn.add(new JSONObject(branch));
        }
        return toReturn;
    }
    
}
