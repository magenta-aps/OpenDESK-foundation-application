/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts;

import dk.opendesk.webscripts.OpenDeskWebScript;
import java.io.IOException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public abstract class FoundationArrayWebScript extends OpenDeskWebScript{
    
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        super.execute(req, res);
        try {
            arrayResult = doAction(req, res);
        } catch (Exception e) {
            error(res, e);
        }
        write(res);
    
    
    }
    
    protected abstract JSONArray doAction(WebScriptRequest req, WebScriptResponse res) throws Exception;
    
}
