/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.ApplicationField;
import dk.opendesk.foundationapplication.DAO.ApplicationSchema;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.List;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class UpdateApplicationStaticData extends JacksonBackedWebscript{

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<ApplicationField> fields = getRequestAs(List.class, ApplicationField.class);
        getApplicationBean().updateApplicationStaticData(fields);
        return new JSONObject().put("status", "OK");
    }
    
}
