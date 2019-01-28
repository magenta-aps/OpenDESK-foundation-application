package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.ArrayList;
import java.util.List;

public class GetActions extends JacksonBackedWebscript {
    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected List<String> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<String> actions = new ArrayList<>();
        actions.add("MailActionExecuter.mail");
        return actions;
    }
}
