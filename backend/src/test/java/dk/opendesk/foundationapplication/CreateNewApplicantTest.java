package dk.opendesk.foundationapplication;

import com.github.sleroy.fakesmtp.core.ServerConfiguration;
import com.github.sleroy.junit.mail.server.MailServer;
import dk.opendesk.foundationapplication.DAO.Application;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

import static dk.opendesk.foundationapplication.EmailTest.TEST_ADDRESSEE;
import static dk.opendesk.foundationapplication.EmailTest.TEST_TEMPLATE_NAME;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_SUBJECT;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE;

public class CreateNewApplicantTest extends AbstractTestClass{

    MailServer mailServer;

    public CreateNewApplicantTest() {
        super("");
    }

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

    public void testCreateNewApplicant() throws Exception {
        NodeRef appRef = TestUtils.application1;
        Application app = getApplicationBean().getApplication(appRef);

        Application change = Utilities.buildChange(getApplicationBean().getApplication(TestUtils.application1)).changeField("8").setValue(TEST_ADDRESSEE).done().build();
        getApplicationBean().updateApplication(change);

        Action action = getServiceRegistry().getActionService().createAction("createApplicant");
        action.setParameterValue(PARAM_SUBJECT, "testSubject");
        action.setParameterValue(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        getServiceRegistry().getActionService().executeAction(action, appRef);

        //todo make emailAction user parameter template model
        //todo make a test-template that needs username and password

        //PagingResults<PersonInfo> results = getServiceRegistry().getPersonService().getPeople("",null,null, null);

        //assertEquals(1, results.getPage().size());

       // assertEquals(app.firstName().getValue(), results.getPage().get(0).getFirstName());

        //System.out.println(results.getPage().get(0).getUserName());
    }
}
