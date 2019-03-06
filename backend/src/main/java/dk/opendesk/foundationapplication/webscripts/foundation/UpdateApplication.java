/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class UpdateApplication extends JacksonBackedWebscript{

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String applicationID = getUrlParams().get("applicationID");
        Application application = getRequestAs(Application.class);
        resolveNodeRef(application, applicationID);
        
        getApplicationBean().updateApplication(application);
        return new JSONObject().put("status", "OK");
    }
}
