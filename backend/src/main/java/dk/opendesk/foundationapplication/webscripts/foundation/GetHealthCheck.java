package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetHealthCheck extends JacksonBackedWebscript {
    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        return getHealthCheckBean().runCheck().toString();
    }
}
