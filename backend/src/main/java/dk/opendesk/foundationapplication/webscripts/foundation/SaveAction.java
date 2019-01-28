package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.beans.FoundationBean;
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

public class SaveAction extends JacksonBackedWebscript {
    FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

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
                aspect = Utilities.getODFName((String) body.get(param));
            }
            else {
                params.put(param,(String) body.get(param));
            }
        }

        if (stateRef == null) {
            throw new Exception("stateRef has to be set");
        } else if (!aspect.equals("onCreate") && !aspect.equals("beforeDelete")) {
            throw new Exception("aspect has to be set to either onCreate or beforeDelete");
        } else {
            foundationBean.saveAction(actionName, stateRef, aspect, params);
            return new JSONObject().put("status", "ok");
        }

    }
}
