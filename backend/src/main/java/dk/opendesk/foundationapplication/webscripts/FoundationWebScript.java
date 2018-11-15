/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts;

import dk.opendesk.webscripts.OpenDeskWebScript;
import java.io.IOException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public abstract class FoundationWebScript extends OpenDeskWebScript{
    
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        super.execute(req, res);
        try {
            objectResult = doAction(req, res);
        } catch (Exception e) {
            error(res, e);
        }
        write(res);
    
    
    }
    
    protected abstract JSONObject doAction(WebScriptRequest req, WebScriptResponse res) throws Exception;
    
}
