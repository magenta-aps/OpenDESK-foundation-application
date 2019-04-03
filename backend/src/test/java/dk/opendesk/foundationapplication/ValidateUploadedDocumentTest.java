package dk.opendesk.foundationapplication;

import org.activiti.engine.impl.form.FormDataImpl;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.TestWebScriptServer;

import java.io.File;

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


    public void testBehaviourEnabling() throws Exception {
        System.out.println("\n------ Test start ------");
        NodeRef appRef = TestUtils.application1;
        System.out.println("\n-------docfolder-----------------");
        NodeRef docFolderRef = getApplicationBean().getOrCreateDocumentFolder(appRef);
        System.out.println("\n-------tempfolder-----------------");
        NodeRef tempFolderRef = getApplicationBean().getOrCreateTempDocumentFolder(appRef);

        System.out.println("\n-------content in tempfolder-----------------");
        getServiceRegistry().getNodeService().createNode(tempFolderRef, Utilities.getCMName("contains"), Utilities.getODFName("testDocument"), TYPE_CONTENT);


        System.out.println("\n-------posting file-----------------");
        File file1 = new File("alfresco/module/repo/bootstrap/files/testFile1.txt");

        System.out.println(file1.toString());

        //getServiceRegistry().getFileFolderService().

        
        FormData formData = new FormData();
        formData.addFieldData("filename", "testTxtFile");
        formData.addFieldData("destination", tempFolderRef);
        formData.addFieldData("filedata", file1);

        System.out.println(formData.toString());
        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest(getPath(""), formData.toString(), "application/json");
        TestWebScriptServer.Response response = sendRequest(request, 200, TestUtils.ADMIN_USER);
        /*
        RetryingTransactionHelper trans = getServiceRegistry().getRetryingTransactionHelper();
        trans.doInTransaction(() -> {
        BehaviourFilter filter = getBehaviourFilter();

        System.out.println(filter.isEnabled(docFolderRef));
        System.out.println(filter.isEnabled(tempFolderRef));

        return null;
        });
        */
    }
}
