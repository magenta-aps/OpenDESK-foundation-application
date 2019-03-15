package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.DAO.FoundationActionValue;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static dk.opendesk.foundationapplication.Utilities.ASPECT_BEFORE_DELETE;
import static dk.opendesk.foundationapplication.Utilities.ASPECT_ON_CREATE;
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

        /*
        for (Iterator it = body.keys(); it.hasNext(); ) {
            String param = (String) it.next();

            if (param.equals("stateRef")) {
                stateRef = new NodeRef((String) body.get(param));
            }
            else if (param.equals("aspect")) {
                if (body.get(param).equals(ASPECT_ON_CREATE) || body.get(param).equals(ASPECT_BEFORE_DELETE)) {
                    aspect = Utilities.getODFName((String) body.get(param));
                } else {
                    throw new Exception("aspect has to be set to either " + ASPECT_ON_CREATE + " or " + ASPECT_BEFORE_DELETE);
                }
            }
            else {
                params.put(param,(Serializable) body.get(param));
            }
        }
        */

        getActionBean().saveAction(actionName, stateRef, aspect, params);
        return new JSONObject().put("status", "OK");

    }
}
