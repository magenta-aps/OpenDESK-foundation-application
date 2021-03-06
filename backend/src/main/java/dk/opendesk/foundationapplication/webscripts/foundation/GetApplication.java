/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetApplication extends JacksonBackedWebscript{

    @Override
    protected Application doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String applicationID = getUrlParams().get("applicationID");
        Reference ref = new Reference();
        ref.setNodeID(applicationID);
        return getApplicationBean().getApplication(ref.asNodeRef());
    }
    
    
    
}
