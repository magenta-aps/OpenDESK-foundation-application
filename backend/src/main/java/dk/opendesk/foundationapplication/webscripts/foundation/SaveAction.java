package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.DAO.FoundationActionValue;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.getODFName;

public class SaveAction extends JacksonBackedWebscript {
    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {

        String actionName = getUrlParams().get("action");
        FoundationActionValue foundationAction = getRequestAs(FoundationActionValue.class);

        if (foundationAction.getStateIdParam() == null || foundationAction.getAspectParam() == null) {
            throw new Exception("'stateRef' and 'aspect' has to be set");
        }

        NodeRef stateRef = Reference.refFromID((String) foundationAction.getStateIdParam().getValue());
        QName aspect = getODFName((String) foundationAction.getAspectParam().getValue());

        List<FoundationActionParameterValue> params = foundationAction.getParams();

        getActionBean().saveAction(actionName, stateRef, aspect, params);
        return new JSONObject().put("status", "OK");

    }
}
