package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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

        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);
        //todo tirsdag : emailfolder er null :(

        NodeRef emailFolder = serviceRegistry.getNodeService().getChildByName(TestUtils.application1, Utilities.getODFName("emailFolder"), "cm:emailFolder");
        System.out.println(emailFolder + "<--");
        List<ChildAssociationRef> childrenRefs = serviceRegistry.getNodeService().getChildAssocs(emailFolder);
        //assertEquals(1, childrenRefs.size());
        //FileInfo fileInfo = serviceRegistry.getFileFolderService().getFileInfo(childrenRefs.get(0).getChildRef());
        //System.out.println(fileInfo.getContentData());
    }

    /*
    public void testEmail() {
        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");
        MailActionExecuter executer = new MailActionExecuter();
        executer.sendTestMessage();
    }
    */







}
