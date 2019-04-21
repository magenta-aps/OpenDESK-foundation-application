package dk.opendesk.foundationapplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;

import java.util.Collections;
import java.util.Scanner;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;
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
    
    public void testUploadDocument() throws Exception{
        TestWebScriptServer.GetRequest request = new TestWebScriptServer.GetRequest("api/upload");
        request.setHeaders(Collections.singletonMap("Content-Type", "application/json"));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK); //as user
    }
    
    public void testFormData() throws Exception{
        MultipartEntityBuilder uploadRequest = MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"));
        
        uploadRequest.addTextBody("mytest", "mybody", ContentType.TEXT_PLAIN);
        HttpEntity entity = uploadRequest.build();
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        entity.writeTo(stream);
        
        
        System.out.println("Content\n" +stream.toString());
        
        System.out.println("Headername " + entity.getContentType().getName());
        System.out.println("HeaderValue " +entity.getContentType().getValue());
    }
    
    public void testUpload() throws IOException, JSONException{
        String nodeRef = Utilities.getOdfEmailTemplateFolder(getServiceRegistry()).toString();
        String filename = "myfile.txt";
        
        MultipartEntityBuilder uploadRequest = MultipartEntityBuilder.create();
        uploadRequest.addTextBody("filename", filename);
        uploadRequest.addTextBody("destination", nodeRef);
        
        InputStream fileInputStream = getClass().getResourceAsStream("/alfresco/module/repo/bootstrap/textTemplates.xml");
        
        System.out.println(fileInputStream.available());
        
        uploadRequest.addBinaryBody("filedata", fileInputStream, ContentType.TEXT_XML, filename);
        
        HttpEntity entity = uploadRequest.build();
        Header contentType = entity.getContentType();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        entity.writeTo(stream);
        
        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest("api/upload", stream.toString(), contentType.getValue());
        //request.setHeaders(Collections.singletonMap(contentType.getName(), contentType.getValue()));
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK);
        JSONObject responseJSON = new JSONObject(response.getContentAsString());
        
        System.out.println(responseJSON.get("nodeRef"));
        
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        entity.writeTo(stream);
//        System.out.println("Content\n" +stream.toString());
        
        
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
