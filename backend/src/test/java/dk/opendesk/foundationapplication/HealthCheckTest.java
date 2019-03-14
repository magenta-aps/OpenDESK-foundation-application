package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.patches.InitialStructure;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.HEALTH_CHECK_STRUCTURE;
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static dk.opendesk.foundationapplication.Utilities.getEmailTemplateDir;
import static dk.opendesk.foundationapplication.Utilities.getOdfEmailTemplateFolder;
import static dk.opendesk.foundationapplication.patches.InitialStructure.MAIL_TEMPLATE_FOLDER_NAME;

public class HealthCheckTest extends AbstractTestClass{

    public HealthCheckTest() {
        super("/foundation/healthcheck");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());

        createEmailTemplateFolderIfMissing();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(getServiceRegistry());

        createEmailTemplateFolderIfMissing();
    }

    public void testStructureHealthCheck() throws IOException, JSONException {

        //everything healthy
        JSONObject result = get(JSONObject.class,"");
        assertEquals(true, ((JSONObject) result.get(HEALTH_CHECK_STRUCTURE)).get("healthy"));

        //removing the email template folder
        ServiceRegistry sr = getServiceRegistry();
        getServiceRegistry().getNodeService().removeChild(getEmailTemplateDir(sr), getOdfEmailTemplateFolder(sr));

        //not healthy anymore
        result = get(JSONObject.class,"");
        assertEquals(false, ((JSONObject) result.get(HEALTH_CHECK_STRUCTURE)).get("healthy"));
        assertEquals("Cannot find emailTemplateFolder", ((JSONObject) result.get(HEALTH_CHECK_STRUCTURE)).get("errorMsg"));

    }

    private void createEmailTemplateFolderIfMissing() {
        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(getEmailTemplateDir(getServiceRegistry()), ContentModel.ASSOC_CONTAINS,  Utilities.getCMName(InitialStructure.MAIL_TEMPLATE_FOLDER_NAME));
        if (childAssociationRefs.size() == 0) {
            getServiceRegistry().getNodeService().createNode(Utilities.getEmailTemplateDir(getServiceRegistry()), ContentModel.ASSOC_CONTAINS, getCMName(MAIL_TEMPLATE_FOLDER_NAME), ContentModel.TYPE_FOLDER).getChildRef();
        }
    }
}

