package dk.opendesk.foundationapplication;

import com.github.sleroy.fakesmtp.core.ServerConfiguration;
import com.github.sleroy.junit.mail.server.MailServer;
import dk.opendesk.foundationapplication.DAO.Application;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static dk.opendesk.foundationapplication.EmailTest.TEST_ADDRESSEE;
import static dk.opendesk.foundationapplication.EmailTest.TEST_TEMPLATE_NAME;
import static dk.opendesk.foundationapplication.Utilities.ACTION_NAME_CREATE_APPLICANT;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_SUBJECT;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE_MODEL;

public class CreateNewApplicantTest extends AbstractTestClass{

    MailServer mailServer;
    private HashMap<String, Serializable> emptyStringModel = new HashMap<>();

    public CreateNewApplicantTest() {
        super("");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        emptyStringModel.put("subject","");
        emptyStringModel.put("userName", "");
        emptyStringModel.put("password","");

        mailServer = new MailServer(ServerConfiguration.create().port(2525).charset("UTF-8").relayDomains("testmail.dk"));
        mailServer.start();


        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());

        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());

        deleteUsers();

    }

    @Override
    protected void tearDown() throws Exception {
        if (mailServer != null) {
            mailServer.close();
        }
        mailServer = null;

        deleteUsers();

        TestUtils.wipeData(getServiceRegistry());
    }

    private void deleteUsers() {
        PersonService ps = getServiceRegistry().getPersonService();
        NodeService ns = getServiceRegistry().getNodeService();

        NodeRef peopleContainer = ps.getPeopleContainer();
        for (ChildAssociationRef child : ns.getChildAssocs(peopleContainer)) {
            String userName = ps.getPerson(child.getChildRef()).getUserName();
            if (!userName.equals("admin") && !userName.equals("guest")) {
                getServiceRegistry().getPersonService().deletePerson(getServiceRegistry().getPersonService().getPerson(child.getChildRef()).getUserName());
            }
        }
    }

    public void testCreateNewApplicant() throws Exception {

        //making a user for application1
        NodeRef appRef = TestUtils.application1;
        Application app = getApplicationBean().getApplication(appRef);

        Application change = Utilities.buildChange(app).changeField("8").setValue(TEST_ADDRESSEE).done().build();
        getApplicationBean().updateApplication(change);

        Action action = getServiceRegistry().getActionService().createAction(ACTION_NAME_CREATE_APPLICANT);
        action.setParameterValue(PARAM_SUBJECT, "testSubject");
        action.setParameterValue(PARAM_TEMPLATE, getActionBean().getEmailTemplate(TEST_TEMPLATE_NAME));
        action.setParameterValue(PARAM_TEMPLATE_MODEL, emptyStringModel);
        getServiceRegistry().getActionService().executeAction(action, appRef);


        //testing the mail and content
        assertEquals(1, mailServer.getMails().size());

        String[] emailLines = mailServer.getMails().get(0).getEmailStr().split("\n");
        String userNameLine = null;
        String passwordLine = null;
        for (String line : emailLines) {
            if (line.startsWith("userName")) {
                userNameLine = line;
            }
            if (line.startsWith("password")) {
                passwordLine = line;
            }
        }

        assertEquals("userName = " + "Lars_Larsen", userNameLine);
        assertNotNull(passwordLine);
        assertFalse(passwordLine.isEmpty());


        //making a user for application2 who is also called Lars Larsen
        appRef = TestUtils.application2;
        app = getApplicationBean().getApplication(appRef);

        change = Utilities.buildChange(app).changeField("8").setValue(TEST_ADDRESSEE).done().build();
        getApplicationBean().updateApplication(change);

        getServiceRegistry().getActionService().executeAction(action, appRef);


        //testing the mail and content and correct username
        assertEquals(2, mailServer.getMails().size());

        emailLines = mailServer.getMails().get(1).getEmailStr().split("\n");
        for (String line : emailLines) {
            if (line.startsWith("userName")) {
                userNameLine = line;
            }
        }
        assertEquals("userName = " + "Lars_Larsen_2", userNameLine);


        //testing the correct creation of persons
        PersonService ps = getServiceRegistry().getPersonService();
        NodeService ns = getServiceRegistry().getNodeService();

        List<ChildAssociationRef> childAssociationRefs = ns.getChildAssocs(ps.getPeopleContainer());

        assertEquals(4, childAssociationRefs.size());

        for (ChildAssociationRef child : childAssociationRefs) {
            String name = ps.getPerson(child.getChildRef()).getFirstName();
            if (name.equals("Administrator") || name.equals("Guest")) {
                continue;
            }
            assertEquals("Lars", name);

            name = ps.getPerson(child.getChildRef()).getLastName();
            assertEquals("Larsen", name);
        }

    }

}
