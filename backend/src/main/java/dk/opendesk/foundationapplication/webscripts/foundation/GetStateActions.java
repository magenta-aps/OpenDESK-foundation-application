package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.JSONAction;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.List;

public class GetStateActions extends JacksonBackedWebscript {


    @Override
    protected List<JSONAction> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        NodeRef stateRef = new NodeRef(getUrlParams().get("stateId"));
        return getActionBean().getActions(stateRef);
    }
}
