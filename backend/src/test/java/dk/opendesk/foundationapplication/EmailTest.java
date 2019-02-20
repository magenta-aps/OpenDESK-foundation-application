package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;


public class EmailTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public EmailTest() {
        super("");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(serviceRegistry);
    }

    //todo this test is not finished, currently only works on astrid@localhost
    public void testEmailAction() throws Exception {

        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");

        Application application = new Application();
        application.setContactEmail("astrid@localhost");
        application.parseRef(TestUtils.application1);
        foundationBean.updateApplication(application);

        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);
    }


    public void testEmailCopying() throws Exception {
        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");

        Application application = new Application();
        application.setContactEmail("astrid@localhost");
        application.parseRef(TestUtils.application1);
        foundationBean.updateApplication(application);

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
                assertEquals("From: astrid@localhost", s);
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
                assertEquals("From: astrid@localhost", s);
            }
        }
        assertEquals("<html>", lines[9]);

    }

    /*
    public void testEmail() {
        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");
        MailActionExecuter executer = new MailActionExecuter();
        executer.sendTestMessage();
    }
    */







}
