package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetReference extends JacksonBackedWebscript {

    public static final String EXCEPTION_WRONG_ID = "getReference.id.exception";

    @Override
    protected Object doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String id = getUrlParams().get("id");

        switch (id) {
            case "emailTemplateFolder":
                return Utilities.getOdfEmailTemplateFolder(getServiceRegistry()).getId();
            default:
                throw new AlfrescoRuntimeException(EXCEPTION_WRONG_ID);
        }

    }
}
