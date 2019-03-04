package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.ArrayList;
import java.util.List;

public class GetEmails extends JacksonBackedWebscript {


    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String applicationId = getUrlParams().get("applicationId");
        Reference appRef = new Reference();
        appRef.setNodeID(applicationId);
        List<String> emailRefs= new ArrayList<>();
        for (NodeRef ref : getFoundationBean().getApplicationEmails(appRef.asNodeRef())) {
            emailRefs.add(ref.getId());
        }
        return emailRefs;
    }
}
