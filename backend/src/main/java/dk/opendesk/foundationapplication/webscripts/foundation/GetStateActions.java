package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.JSONAction;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.ArrayList;
import java.util.List;

public class GetStateActions extends JacksonBackedWebscript {

    FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected List<JSONAction> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {

        List<JSONAction> jsonActions = new ArrayList<>();

        NodeRef stateRef = new NodeRef(getUrlParams().get("stateId"));
        List<Action> actions = foundationBean.getActions(stateRef);

        for (Action a : actions) {
            JSONAction jsonAction = new JSONAction();
            jsonAction.setId(a.getId());
            jsonAction.setName(a.getActionDefinitionName());
            jsonAction.setParameters(a.getParameterValues());
            jsonActions.add(jsonAction);
        }

        return jsonActions;
    }
}
