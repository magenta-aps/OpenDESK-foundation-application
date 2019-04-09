package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;



public class PostStartGroupsAndUsers extends JacksonBackedWebscript {
    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {

        JSONObject jsonRequest = getRequestAs(JSONObject.class);

        JSONObject roles = jsonRequest.getJSONObject("groups");
        JSONArray users = jsonRequest.getJSONArray("users");

        NodeRef emailTemplate = getActionBean().getEmailTemplate("ekstern-bruger-osflow.html.ftl");

        getAuthorityBean().loadUsers(roles,users,"Velkommen til OSflow-Danva", emailTemplate);

        return new JSONObject().put("status", "OK");

    }
}
