package dk.opendesk.foundationapplication;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.actions.EmailAction;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.repo.model.OpenDeskModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;

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
        super(""); //Hvilket path?
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

    public void testEmailAction() throws Exception {

        Action action = serviceRegistry.getActionService().createAction("foundationMail");
        //Map<String, Serializable> params = new HashMap<>();
        //params.put("query","/home/astrid/OpenDESK-foundation-application/backend/src/main/amp/config/alfresco/module/foundationapplication/bootstrap/files/cerberus-fluid.html");
        //NodeRef templateRef = serviceRegistry.getNodeLocatorService().getNode("xpath",null, params);

//        NodeRef templateRef = serviceRegistry.getFileFolderService().resolveNamePath();

        String query = "PATH:\"" + OpenDeskModel.TEMPLATE_OD_FOLDER + "cm:" + "email.html.ftl" + "\"";
        ResultSet resultSet = serviceRegistry.getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
                SearchService.LANGUAGE_LUCENE, query);
        NodeRef templateRef = resultSet.getNodeRef(0);

        System.out.println("templateRef = " + templateRef);
        action.setParameterValue(PARAM_TEMPLATE, templateRef);
        action.setParameterValue(PARAM_SUBJECT, "Subject of testMail");
        action.setParameterValue(PARAM_FROM, "astrid@localhost");

        Application application = new Application();
        //application.setContactFirstName("Lars");
        //application.setContactLastName("Lokke");
        application.setContactEmail("astrid@localhost");

        application.parseRef(TestUtils.application1);
        foundationBean.updateApplication(application);

        //System.out.println("applicationRef = " + application.asNodeRef());

        //Application app = TestUtils.application1;



        serviceRegistry.getActionService().executeAction(action,TestUtils.application1);
        //ActionImpl
    }





}
