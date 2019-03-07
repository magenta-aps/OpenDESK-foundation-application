package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetApplicationHistory extends JacksonBackedWebscript {

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        NodeRef appRef = new NodeRef(getUrlParams().get("applicationID"));
        return getApplicationBean().getApplicationHistory(appRef);
    }
}
