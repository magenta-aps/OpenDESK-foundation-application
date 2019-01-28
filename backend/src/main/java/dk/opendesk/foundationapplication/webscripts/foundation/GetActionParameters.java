package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetActionParameters extends JacksonBackedWebscript {
    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected Map<String, QName> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String actionName = getUrlParams().get("action");
        List<ParameterDefinition> paramDefs = foundationBean.getActionParameters(actionName);
        Map<String, QName> params = new HashMap<>();
        for (ParameterDefinition paramDef : paramDefs) params.put(paramDef.getName(),paramDef.getType());
        return params;
    }
}
