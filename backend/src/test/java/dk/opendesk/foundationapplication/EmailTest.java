package dk.opendesk.foundationapplication;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.actions.EmailAction;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.repo.model.OpenDeskModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Ignore;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.repo.action.executer.MailActionExecuter.*;

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

    //this test
    public void testEmailAction() throws Exception {

        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");

        Application application = new Application();
        application.setContactEmail("astrid@localhost");
        application.parseRef(TestUtils.application1);
        foundationBean.updateApplication(application);

        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);
    }

    public void testEmail() {
        Action action = foundationBean.configureEmailAction("email.html.ftl" , "Subject of test mail", "astrid@localhost");
        MailActionExecuter executer = new MailActionExecuter();
        executer.sendTestMessage();
    }







}
