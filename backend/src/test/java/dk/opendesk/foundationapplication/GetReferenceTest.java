package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.patches.InitialStructure;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.IOException;

import static dk.opendesk.foundationapplication.DAO.Reference.DEFAULT_STORE;
import static dk.opendesk.foundationapplication.Utilities.getCMName;

public class GetReferenceTest extends AbstractTestClass{

    public GetReferenceTest() {
        super("/foundation/reference");
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
    }

    public void testGetEmailFolderReference() throws IOException {
        String folderId = get(String.class, "emailTemplateFolder");
        NodeRef folderRef = new NodeRef(DEFAULT_STORE + "/" + folderId); //todo This must have a better way?
        assertEquals(ContentModel.TYPE_FOLDER, getServiceRegistry().getNodeService().getType(folderRef));
        assertEquals(getCMName(InitialStructure.MAIL_TEMPLATE_FOLDER_NAME), getServiceRegistry().getNodeService().getPrimaryParent(folderRef).getQName());
    }
}

