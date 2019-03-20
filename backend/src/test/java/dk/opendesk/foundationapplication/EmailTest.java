package dk.opendesk.foundationapplication;

import com.github.sleroy.fakesmtp.core.ServerConfiguration;
import com.github.sleroy.junit.mail.server.MailServer;
import dk.opendesk.foundationapplication.DAO.*;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static dk.opendesk.foundationapplication.Utilities.*;
import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.action.executer.MailActionExecuter.*;

public class EmailTest extends AbstractTestClass {

    MailServer mailServer;

    public EmailTest() {
        super("/foundation");
    }

    private static String TEST_TEMPLATE_NAME = "email.html.ftl";
    private static String TEST_ADDRESSEE = "astrid@testmail.dk";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mailServer = new MailServer(ServerConfiguration.create().port(2525).charset("UTF-8").relayDomains("testmail.dk"));
        mailServer.start();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());

        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());

        Application change = Utilities.buildChange(getApplicationBean().getApplication(TestUtils.application1)).changeField("8").setValue(TEST_ADDRESSEE).done().build();
        getApplicationBean().updateApplication(change);
    }

    @Override
    protected void tearDown() throws Exception {
        if (mailServer != null) {
            mailServer.close();
        }
        mailServer = null;

        TestUtils.wipeData(getServiceRegistry());
    }

    //todo this test is not finished, currently only works on astrid@localhost
    public void testEmailAction() throws Exception {
        assertTrue(mailServer.getMails().isEmpty());
        Action action = getServiceRegistry().getActionService().createAction(ACTION_NAME_EMAIL);
        action.setParameterValue(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        action.setParameterValue(PARAM_SUBJECT, "hallo hallo");
        getServiceRegistry().getActionService().executeAction(action, TestUtils.application1);
        assertEquals(1, mailServer.getMails().size());
        assertEquals("hallo hallo", mailServer.getMails().get(0).getSubject());
    }

    //todo this test is not finished, currently only works on astrid@localhost
    public void testEmailCopying() throws Exception {
        assertTrue(mailServer.getMails().isEmpty());
        Action action = getServiceRegistry().getActionService().createAction(ACTION_NAME_EMAIL);
        action.setParameterValue(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        action.setParameterValue(PARAM_SUBJECT, "hallo hallo");
        action.setParameterValue(PARAM_FROM, TEST_ADDRESSEE);

        //sending the email
        getServiceRegistry().getActionService().executeAction(action, TestUtils.application1);
        assertEquals(1, mailServer.getMails().size());
        assertEquals("hallo hallo", mailServer.getMails().get(0).getSubject());

        //getting the email folder from the application
        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName("emailFolder"), null);
        assertEquals(1, childAssociationRefs.size()); //there should only be one folder
        NodeRef emailFolderRef = childAssociationRefs.get(0).getChildRef();

        //getting the contents of the email folder
        List<ChildAssociationRef> childrenRefs = getServiceRegistry().getNodeService().getChildAssocs(emailFolderRef);
        assertEquals(1, childrenRefs.size()); //there should be one email in the folder

        ContentReader reader = getServiceRegistry().getFileFolderService().getReader(childrenRefs.get(0).getChildRef());
        String[] lines = reader.getContentString().split("\n");
        for (String s : lines) {
            if (s.startsWith("From:")) {
                assertEquals("From: " + TEST_ADDRESSEE, s);
            }
        }
        assertEquals("<html>", lines[9]);

        //sending one more email
        Thread.sleep(2000); //avoid duplicate filenames
        getServiceRegistry().getActionService().executeAction(action, TestUtils.application1);

        //getting the email folder from the application
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName("emailFolder"), null);
        assertEquals(1, childAssociationRefs.size()); //there should still only be one folder
        emailFolderRef = childAssociationRefs.get(0).getChildRef();

        //getting the contents of the email folder
        childrenRefs = getServiceRegistry().getNodeService().getChildAssocs(emailFolderRef);
        assertEquals(2, childrenRefs.size()); //there should now be two emails in the folder

        reader = getServiceRegistry().getFileFolderService().getReader(childrenRefs.get(1).getChildRef());
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
        assertEquals(0, mailServer.getMails().size());
        //sending two emails
        Action action = getServiceRegistry().getActionService().createAction(ACTION_NAME_EMAIL);
        action.setParameterValue(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        action.setParameterValue(PARAM_SUBJECT, "hallo hallo");
        getServiceRegistry().getActionService().executeAction(action, TestUtils.application1);
        assertEquals(1, mailServer.getMails().size());
        assertEquals("hallo hallo", mailServer.getMails().get(0).getSubject());
        Thread.sleep(2000);
        action = getServiceRegistry().getActionService().createAction(ACTION_NAME_EMAIL);
        action.setParameterValue(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        action.setParameterValue(PARAM_SUBJECT, "hallo hallo");
        getServiceRegistry().getActionService().executeAction(action, TestUtils.application1);
        assertEquals(2, mailServer.getMails().size());
        assertEquals("hallo hallo", mailServer.getMails().get(1).getSubject());

        //testing that the emails.get script returns two elements
        List<String> emailRefs = get(List.class, String.class, "/application/" + TestUtils.application1.getId() + "/emails");
        assertEquals(2, emailRefs.size());

        //testing that the email.get script returns an email when given one of the elements from above
        String email = get(String.class, "/application/" + TestUtils.application1.getId() + "/email/" + emailRefs.get(0));

        String[] lines = email.split("\n");
        for (String s : lines) {
            if (s.startsWith("Subject:")) {
                assertEquals("Subject: " + "hallo hallo", s);
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
        getActionBean().saveEmailCopy(mimeMessage, TestUtils.application1);

        //testing that there is one email on the application
        assertEquals(1, getApplicationBean().getApplicationEmails(TestUtils.application1).size());

        //testing the content of the email
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)");
        String expContent = "Date: " + sdf.format(date) + "\n\ntest message\n";

        List<String> emailIds = get(List.class, String.class, "/application/" + TestUtils.application1.getId() + "/emails");
        String emailContent = get(String.class, "/application/" + TestUtils.application1.getId() + "/email/" + emailIds.get(0));
        assertEquals(expContent, emailContent);
    }

    public void testGetOrCreateEmailFolder() throws Exception {
        //no email folder
        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);
        assertEquals(0, childAssociationRefs.size());

        //one email folder
        getActionBean().getOrCreateEmailFolder(TestUtils.application1);
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);
        assertEquals(1, childAssociationRefs.size());

        //still one email folder
        getActionBean().getOrCreateEmailFolder(TestUtils.application1);
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);
        assertEquals(1, childAssociationRefs.size());

    }

    public void testGetApplicationEmails() throws Exception {

        //No emails
        List<NodeRef> emails = getApplicationBean().getApplicationEmails(TestUtils.application1);
        assertEquals(0, emails.size());

        //one email
        NodeRef emailFolderRef = getActionBean().getOrCreateEmailFolder(TestUtils.application1);
        getServiceRegistry().getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("test"), TYPE_CONTENT).getChildRef();
        emails = getApplicationBean().getApplicationEmails(TestUtils.application1);
        assertEquals(1, emails.size());

        //two emails
        getServiceRegistry().getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("test"), TYPE_CONTENT).getChildRef();
        emails = getApplicationBean().getApplicationEmails(TestUtils.application1);
        assertEquals(2, emails.size());

        //testing the webscript
        List<String> emailIds = get(List.class, String.class, "/application/" + TestUtils.application1.getId() + "/emails");
        assertEquals(2, emailIds.size());
    }

    public void testGetEmail() throws Exception {

        //writing a test email
        NodeRef emailFolderRef = getActionBean().getOrCreateEmailFolder(TestUtils.application1);
        NodeRef emailRef = getServiceRegistry().getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("test"), TYPE_CONTENT).getChildRef();
        ContentWriter writer = getServiceRegistry().getFileFolderService().getWriter(emailRef);
        writer.putContent("test email content");

        //getting the email out with method
        String email = getActionBean().getEmail(TestUtils.application1, emailRef);
        assertEquals("test email content", email);

        //getting the email out with webscript
        email = get(String.class, "/application/" + TestUtils.application1.getId() + "/email/" + emailRef.getId());
        assertEquals("test email content", email);

    }
    
    public void testEmailFolderPresent(){
        NodeRef emailTemplateFolder = Utilities.getEmailTemplateDir(getServiceRegistry());
        assertTrue(getServiceRegistry().getNodeService().exists(emailTemplateFolder));
    }


    public void testEmailSavedToHistory() throws Exception {

        //saving action
        JSONObject data = new JSONObject();
        data.put("stateRef", TestUtils.w1StateAccessRef);
        data.put("aspect", ASPECT_ON_CREATE);
        data.put("subject", "testEmailSavedToHistory");
        data.put(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        post(data, "/action/" + ACTION_NAME_EMAIL);

        //updating application
        Application change = new Application();
        change.parseRef(TestUtils.application1);
        StateReference stateRef = new StateReference();
        stateRef.parseRef(TestUtils.w1StateAccessRef);
        change.setState(stateRef);
        getApplicationBean().updateApplication(change);

        //last ApplicationChange is a send email
        List<ApplicationChange> changes = getApplicationBean().getApplicationHistory(TestUtils.application1);
        assertEquals(APPLICATION_CHANGE_UPDATE_EMAIL, changes.get(changes.size() - 1).getChangeType());
    }
}
