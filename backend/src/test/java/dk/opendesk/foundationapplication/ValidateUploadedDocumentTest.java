package dk.opendesk.foundationapplication;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class ValidateUploadedDocumentTest extends AbstractTestClass {

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

    public void testBehaviourEnabling() throws Exception {
        System.out.println("\n------ Test start ------");

        ServiceRegistry sr = getServiceRegistry();
        NodeService ns = sr.getNodeService();
        ContentService cs = sr.getContentService();
        NodeRef appRef = TestUtils.application1;

        System.out.println("\n-------doc-folder-----------------");
        NodeRef docFolderRef = getApplicationBean().getOrCreateDocumentFolder(appRef);


        System.out.println("\n-------temp-folder-----------------");
        NodeRef tempFolderRef = getApplicationBean().getOrCreateTempDocumentFolder(appRef);


        System.out.println("\n-------with sendRequest-----------------");


        //byte[] fileContent = IOUtils.toByteArray(getClass().getResourceAsStream("/alfresco/module/repo/bootstrap/files/testFile1.txt"));

        OdfBaseApiTest test = new OdfBaseApiTest();
        //test.setup();
        test.setupTests();
        test.createTextFile(tempFolderRef.getId(),"test", "This is file content" );
        // TODO: the request object below probably has to be constructed in another way...
        test.tearDown();

        //transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
        //TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest("/api/upload", fileContent, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        //TestWebScriptServer.Response response = sendRequest(request, 200, TestUtils.ADMIN_USER);

    }


}
