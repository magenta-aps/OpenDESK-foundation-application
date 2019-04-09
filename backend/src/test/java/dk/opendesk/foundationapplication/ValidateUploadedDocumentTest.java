package dk.opendesk.foundationapplication;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;

import javax.mail.internet.ContentType;
import java.io.InputStream;

import static org.alfresco.model.ContentModel.TYPE_CONTENT;

public class ValidateUploadedDocumentTest extends AbstractTestClass{

    public ValidateUploadedDocumentTest() {
        super("/api/upload");
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

    /*
    public void testBehaviourEnabling() throws Exception {
        System.out.println("\n------ Test start ------");

        ServiceRegistry sr = getServiceRegistry();
        NodeService ns = sr.getNodeService();
        ContentService cs = sr.getContentService();
        NodeRef appRef = TestUtils.application1;

        System.out.println("\n-------doc-folder-----------------");
        NodeRef docFolderRef = getApplicationBean().getOrCreateDocumentFolder(appRef);

        System.out.println("\n-------test-folder-----------------");
        NodeRef testFolder = ns.createNode(docFolderRef, ContentModel.ASSOC_CONTAINS, Utilities.getODFName("testFolder"), ContentModel.TYPE_FOLDER).getChildRef();

        System.out.println("\n-------temp-folder-----------------");
        NodeRef tempFolderRef = getApplicationBean().getOrCreateTempDocumentFolder(appRef);

        System.out.println("\n------- creating node -----------------");
        NodeRef docNodeRef = ns.createNode(testFolder, ContentModel.ASSOC_CONTAINS, Utilities.getODFName("testDocument"), TYPE_CONTENT).getChildRef();

        System.out.println("\n------- adding content to node -----------------");
        InputStream stream = getClass().getResourceAsStream("/alfresco/module/repo/bootstrap/files/testFile1.txt");
        ContentWriter writer = cs.getWriter(docNodeRef, ContentModel.PROP_CONTENT, true);
        //writer.setLocale(CONTENT_LOCALE);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent(stream);

        System.out.println("\n-------moving node to tempfolder-----------------");
        ns.moveNode(docNodeRef, tempFolderRef, Utilities.getCMName("contains"), Utilities.getODFName("testDocument"));


    }
    */
}
