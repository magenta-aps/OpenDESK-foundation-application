package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetApplicationHistory extends JacksonBackedWebscript {

    FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        NodeRef appRef = new NodeRef(getUrlParams().get("applicationID"));
        return foundationBean.getApplicationHistory(appRef);
    }
}
