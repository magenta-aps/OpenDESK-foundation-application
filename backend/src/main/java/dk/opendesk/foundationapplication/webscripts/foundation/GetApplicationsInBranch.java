/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.FoundationArrayWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetApplicationsInBranch extends FoundationArrayWebScript{
    
    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected JSONArray doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String branchID = urlParams.get("branchID");
        JSONArray applications = new JSONArray();
        for(ApplicationSummary app : foundationBean.getBranchApplications(new NodeRef("/"+branchID))){
            applications.add(new JSONObject(app));
        }
        return applications;
    }

    
    
}