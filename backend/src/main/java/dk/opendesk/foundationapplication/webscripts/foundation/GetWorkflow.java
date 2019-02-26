/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetWorkflow extends JacksonBackedWebscript{

    @Override
    protected Workflow doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String workflowID = getUrlParams().get("workflowID");
        Reference reference = new Reference();
        reference.setNodeID(workflowID);
        return getFoundationBean().getWorkflow(reference.asNodeRef());
    }
    
    
    
}
