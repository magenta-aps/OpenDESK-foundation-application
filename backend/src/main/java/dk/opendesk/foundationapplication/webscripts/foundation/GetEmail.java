package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetEmail extends JacksonBackedWebscript {

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String applicationId = getUrlParams().get("applicationId");
        String emailId = getUrlParams().get("emailId");
        Reference appRef = new Reference();
        Reference emailRef = new Reference();
        appRef.setNodeID(applicationId);
        emailRef.setNodeID(emailId);
        return getFoundationBean().getEmail(appRef.asNodeRef(), emailRef.asNodeRef());
    }
}
