package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetApplicationDocumentFolder extends JacksonBackedWebscript {

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String applicationID = getUrlParams().get("applicationID");
        Reference ref = new Reference();
        ref.setNodeID(applicationID);
        return getActionBean().getOrCreateDocumentFolder(ref.asNodeRef());
    }
}
