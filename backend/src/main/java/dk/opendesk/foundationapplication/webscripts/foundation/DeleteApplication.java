package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class DeleteApplication extends JacksonBackedWebscript {

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String applicationID = getUrlParams().get("applicationId");
        Reference ref = new Reference();
        ref.setNodeID(applicationID);
        getFoundationBean().deleteApplication(ref.asNodeRef());
        return new JSONObject().put("status", "OK");
    }
}
