package dk.opendesk.foundationapplication.webscripts.foundation;

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
import java.util.Map;

import static dk.opendesk.foundationapplication.Utilities.ASPECT_BEFORE_DELETE;
import static dk.opendesk.foundationapplication.Utilities.ASPECT_ON_CREATE;

public class SaveAction extends JacksonBackedWebscript {
    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {

        String actionName = getUrlParams().get("action");
        JSONObject body = new JSONObject(req.getContent().getContent());

        NodeRef stateRef = null;
        QName aspect = null;

        Map<String, Serializable> params = new HashMap<>();

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
                params.put(param,(String) body.get(param));
            }
        }

        if (stateRef == null || aspect == null) {
            throw new Exception("'stateRef' and 'aspect' has to be set");
        } else {
            getActionBean().saveAction(actionName, stateRef, aspect, params);
            return new JSONObject().put("status", "OK");
        }

    }
}
