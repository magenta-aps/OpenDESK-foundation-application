package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.FoundationAction;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.ArrayList;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.ACTION_BEAN_NAME_EMAIL;
import static dk.opendesk.foundationapplication.Utilities.ACTION_NAME_EMAIL;

public class GetActions extends JacksonBackedWebscript {


    @Override
    protected List<FoundationAction> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<FoundationAction> actions = new ArrayList<>();

        actions.add(new FoundationAction(ACTION_NAME_EMAIL, getFoundationBean().getActionParameters(ACTION_BEAN_NAME_EMAIL)));

        return actions;
    }
}
