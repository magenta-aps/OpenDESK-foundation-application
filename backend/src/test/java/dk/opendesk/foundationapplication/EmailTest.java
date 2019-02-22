package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.actions.EmailAction;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import junit.framework.Test;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.poi.ss.formula.functions.T;
import thredds.wcs.v1_1_0.WcsException;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static dk.opendesk.foundationapplication.Utilities.APPLICATION_EMAILFOLDER;
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.action.executer.MailActionExecuter.*;


public class EmailTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public EmailTest() {
        super("/foundation");
    }

    private static String TEST_TEMPLATE_NAME = "email.html.ftl";
    private static String TEST_ADDRESSEE = "astrid@localhost";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);

        Application application = new Application();
        application.setContactEmail(TEST_ADDRESSEE);
        application.parseRef(TestUtils.application1);
        foundationBean.updateApplication(application);
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(serviceRegistry);
    }

    //todo this test is not finished, currently only works on astrid@localhost
    public void testEmailAction() throws Exception {
        Action action = foundationBean.configureEmailAction(TEST_TEMPLATE_NAME , "Subject of test mail", TEST_ADDRESSEE);
        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);
    }


    //todo this test is not finished, currently only works on astrid@localhost
    public void testEmailCopying() throws Exception {

        Action action = foundationBean.configureEmailAction(TEST_TEMPLATE_NAME , "Subject of test mail", TEST_ADDRESSEE);

        //sending the email
        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);

        //getting the email folder from the application
        List<ChildAssociationRef> childAssociationRefs = serviceRegistry.getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName("emailFolder"), null);
        assertEquals(1, childAssociationRefs.size()); //there should only be one folder
        NodeRef emailFolderRef = childAssociationRefs.get(0).getChildRef();

        //getting the contents of the email folder
        List<ChildAssociationRef> childrenRefs = serviceRegistry.getNodeService().getChildAssocs(emailFolderRef);
        assertEquals(1, childrenRefs.size()); //there should be one email in the folder

        ContentReader reader = serviceRegistry.getFileFolderService().getReader(childrenRefs.get(0).getChildRef());
        String[] lines = reader.getContentString().split("\n");
        for (String s : lines) {
            if (s.startsWith("From:")) {
                assertEquals("From: " + TEST_ADDRESSEE, s);
            }
        }
        assertEquals("<html>", lines[9]);


        //sending one more email
        Thread.sleep(2000); //avoid duplicate filenames
        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);

        //getting the email folder from the application
        childAssociationRefs = serviceRegistry.getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName("emailFolder"), null);
        assertEquals(1, childAssociationRefs.size()); //there should still only be one folder
        emailFolderRef = childAssociationRefs.get(0).getChildRef();

        //getting the contents of the email folder
        childrenRefs = serviceRegistry.getNodeService().getChildAssocs(emailFolderRef);
        assertEquals(2, childrenRefs.size()); //there should now be two emails in the folder

        reader = serviceRegistry.getFileFolderService().getReader(childrenRefs.get(1).getChildRef());
        lines = reader.getContentString().split("\n");
        for (String s : lines) {
            if (s.startsWith("From:")) {
                assertEquals("From: " + TEST_ADDRESSEE, s);
            }
        }
        assertEquals("<html>", lines[9]);

    }

    //todo this test is not finished, currently only works on astrid@localhost
    public void testSendEmail() throws Exception {

        //sending two emails
        Action action = foundationBean.configureEmailAction(TEST_TEMPLATE_NAME, "TestSubject1", TEST_ADDRESSEE);
        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);
        Thread.sleep(2000);
        action = foundationBean.configureEmailAction(TEST_TEMPLATE_NAME , "TestSubject2", TEST_ADDRESSEE);
        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);

        //testing that the emails.get script returns two elements
        List<String> emailRefs = get(List.class, String.class, "/application/" + TestUtils.application1.getId() + "/emails");
        assertEquals(2,emailRefs.size());

        //testing that the email.get script returns an email when given one of the elements from above
        String email =  get(String.class, "/application/" + TestUtils.application1.getId() + "/email/" + emailRefs.get(0));

        String[] lines = email.split("\n");
        for (String s : lines) {
            if (s.startsWith("From:")) {
                assertEquals("From: " + TEST_ADDRESSEE, s);
            }
        }
        assertEquals("<html>", lines[9]);
    }


    public void testSaveEmailCopy() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
        mimeMessage.setContent("test message", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        Date date = new Date();
        mimeMessage.setSentDate(date);

        //saving the email
        foundationBean.saveEmailCopy(mimeMessage, TestUtils.application1);

        //testing that there is one email on the application
        assertEquals(1, foundationBean.getApplicationEmails(TestUtils.application1).size());

        //testing the content of the email
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)");
        String expContent = "Date: " + sdf.format(date) + "\n\ntest message\n";

        List<String> emailIds = get(List.class, String.class, "/application/" + TestUtils.application1.getId() + "/emails");
        String emailContent = get(String.class, "/application/" + TestUtils.application1.getId() + "/email/" + emailIds.get(0));
        assertEquals(expContent, emailContent);
    }

    public void testGetOrCreateEmailFolder() throws Exception {
        //no email folder
        List<ChildAssociationRef> childAssociationRefs = serviceRegistry.getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);
        assertEquals(0,childAssociationRefs.size());

        //one email folder
        foundationBean.getOrCreateEmailFolder(TestUtils.application1);
        childAssociationRefs = serviceRegistry.getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);
        assertEquals(1,childAssociationRefs.size());

        //still one email folder
        foundationBean.getOrCreateEmailFolder(TestUtils.application1);
        childAssociationRefs = serviceRegistry.getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);
        assertEquals(1,childAssociationRefs.size());

    }

    public void testGetApplicationEmails() throws Exception {

        //No emails
        List<NodeRef> emails = foundationBean.getApplicationEmails(TestUtils.application1);
        assertEquals(0, emails.size());

        //one email
        NodeRef emailFolderRef = foundationBean.getOrCreateEmailFolder(TestUtils.application1);
        serviceRegistry.getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("test"), TYPE_CONTENT).getChildRef();
        emails = foundationBean.getApplicationEmails(TestUtils.application1);
        assertEquals(1, emails.size());

        //two emails
        serviceRegistry.getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("test"), TYPE_CONTENT).getChildRef();
        emails = foundationBean.getApplicationEmails(TestUtils.application1);
        assertEquals(2, emails.size());

        //testing the webscript
        List<String> emailIds = get(List.class, String.class, "/application/" + TestUtils.application1.getId() + "/emails");
        assertEquals(2, emailIds.size());
    }

    public void testGetEmail() throws Exception {

        //writing a test email
        NodeRef emailFolderRef = foundationBean.getOrCreateEmailFolder(TestUtils.application1);
        NodeRef emailRef = serviceRegistry.getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("test"), TYPE_CONTENT).getChildRef();
        ContentWriter writer = serviceRegistry.getFileFolderService().getWriter(emailRef);
        writer.putContent("test email content");

        //getting the email out with method
        String email = foundationBean.getEmail(TestUtils.application1, emailRef);
        assertEquals("test email content", email);

        //getting the email out with webscript
        email = get(String.class, "/application/" + TestUtils.application1.getId() + "/email/" + emailRef.getId());
        assertEquals("test email content", email);

    }

    public void testConfigureEmailAction() throws Exception {
        Action action1 = foundationBean.configureEmailAction(TEST_TEMPLATE_NAME, "subject 1", TEST_ADDRESSEE);
        assertNotNull(action1.getParameterValue(PARAM_TEMPLATE));
        assertEquals(action1.getParameterValue(PARAM_SUBJECT), "subject 1");
        assertEquals(action1.getParameterValue(PARAM_FROM), TEST_ADDRESSEE);

        //todo jeg kan ikke få denne test til at virke:
        /*
        Action action2 = foundationBean.configureEmailAction("non-existing template name", "subject 2", TEST_ADDRESSEE);
        try{
            action2.getParameterValue(PARAM_TEMPLATE);
            fail("Expected exception not thrown");
        } catch (Exception e){
        }
        */

        Action action3 = foundationBean.configureEmailAction(TEST_TEMPLATE_NAME, null, TEST_ADDRESSEE);
        try {
            serviceRegistry.getActionService().executeAction(action3,TestUtils.application1);
            fail("Expected exception not thrown");
        } catch (Exception e) {
        }

    }
    /*
    public void testEmail() {
        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");
        MailActionExecuter executer = new MailActionExecuter();
        executer.sendTestMessage();
    }
    */







}
