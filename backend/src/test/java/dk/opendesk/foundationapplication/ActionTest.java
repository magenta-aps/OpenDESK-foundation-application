package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.Test;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dk.opendesk.foundationapplication.Utilities.*;

public class ActionTest extends AbstractTestClass {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public ActionTest() {
        super("/foundation/actions");
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("\n - set up - ");
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("\n - tear down - ");
        TestUtils.wipeData(serviceRegistry);
    }


    public void testGetActions() throws IOException {
        List actions = get(List.class, String.class, "");
        System.out.println("--- actions: " + actions + " ---");

        assertEquals(1, actions.size());
        assertEquals("MailActionExecuter.mail", actions.get(0));
    }

    public void testGetParameters() throws IOException {
        Map actParams = get(Map.class, String.class, String.class, "mail/parameters");
        List<ParameterDefinition> expParams = serviceRegistry.getActionService().getActionDefinition("mail").getParameterDefinitions();

        assertEquals(expParams.size(), actParams.size());

        for (ParameterDefinition expParam : expParams) {
            assertTrue(actParams.containsKey(expParam.getName()));
            assertEquals(expParam.getType().getPrefixString(), actParams.get(expParam.getName()));
        }
    }

    public void testSaveAction() throws Exception {
        String actionName = "mail";

        JSONObject data = new JSONObject();
        data.put("stateRef", TestUtils.stateRecievedRef);
        data.put("aspect", ASPECT_ON_CREATE);
        data.put("cc", "test@test.dk"); //TODO make sure only real email adresses can be set as parameters
        post(data, actionName);
        System.out.println(data);

        List<Action> actions = serviceRegistry.getActionService().getActions(TestUtils.stateRecievedRef);
        for (Action act : actions) {
            if (act.getActionDefinitionName().equals(actionName)) {

                //testing the aspect is set on the action
                Set<QName> aspects = serviceRegistry.getNodeService().getAspects(act.getNodeRef());
                assertTrue(aspects.contains(Utilities.getODFName(ASPECT_ON_CREATE)));

                //testing the parameter is set on the action
                assertEquals(act.getParameterValue("cc"), "test@test.dk");
            }
        }

        //testing missing state NodeRef
        data.remove("stateRef");
        post(data, actionName, Status.STATUS_BAD_REQUEST);

        //testing wrong aspect name
        data.put("stateRef", TestUtils.stateRecievedRef);
        data.put("aspect", "totallyWrongAspect");
        post(data, actionName, Status.STATUS_BAD_REQUEST);
    }




    public void testActionExecutingOnStateChange() throws Exception {

        System.out.println("\n\n\n ----- RUNNING testActionExecutingOnStateChange -----");

        //todo: mit problem er at application 1 allerede er associeret med staten received!

        //creating actions
        //Action enterStateAction = serviceRegistry.getActionService().createAction("test");
        //Action exitStateAction = serviceRegistry.getActionService().createAction("test");

        //setting the actions to not have been executed
        //enterStateAction.setParameterValue("executed", false);
        //exitStateAction.setParameterValue("executed", false);

        //saving actions to state 'received'
        System.out.println("\n\tsaving actions to state 'received'");
        JSONObject dataForEnterAction = new JSONObject();
        dataForEnterAction.put("stateRef", TestUtils.stateRecievedRef);
        dataForEnterAction.put("aspect", ASPECT_ON_CREATE);
        dataForEnterAction.put("executed", "false");

        JSONObject dataForExitAction = new JSONObject();
        dataForExitAction.put("stateRef", TestUtils.stateRecievedRef);
        dataForExitAction.put("aspect", ASPECT_BEFORE_DELETE);
        dataForExitAction.put("executed", "false");

        System.out.println("\n\tposting");
        post(dataForEnterAction,"test");
        post(dataForExitAction,"test");

        System.out.println("\n\tgetting the actions out from the state");
        //getting the actions out from the state
        List<Action> actions = serviceRegistry.getActionService().getActions(TestUtils.stateRecievedRef);
        //System.out.println("# actions on the state = " + actions.size());

        //System.out.println("Aspect on first  action: " + serviceRegistry.getNodeService().getAspects(actions.get(0).getNodeRef()));
        //System.out.println("Aspect on second action: " + serviceRegistry.getNodeService().getAspects(actions.get(1).getNodeRef()));

        boolean sameOrder = serviceRegistry.getNodeService().getAspects(actions.get(0).getNodeRef()).contains(getODFName(ASPECT_ON_CREATE));
        //System.out.println("same order? = " + sameOrder);

        Action enterStateAction = sameOrder ? actions.get(0) : actions.get(1);
        Action exitStateAction = sameOrder ? actions.get(1) : actions.get(0);

        //System.out.println("enterStateAction has " + serviceRegistry.getNodeService().getAspects(enterStateAction.getNodeRef()));
        //System.out.println("exitStateAction has " + serviceRegistry.getNodeService().getAspects(exitStateAction.getNodeRef()));

        System.out.println("\n\ttesting");
        //testing that enterStateAction got executed, while exit state action did not
        assertEquals("false",enterStateAction.getParameterValue("executed"));
        assertEquals("false",exitStateAction.getParameterValue("executed"));

        System.out.println("\n\tfinished testing");


        System.out.println("\n\n\n --------------------------------------------------\n");

        //setting application to move on to state 'rejected'

        //testing that exitStateAction got executed
        //assertEquals(true,exitStateAction.getParameterValue("executed"));


        //






        //Application application = new Application();
        //application.parseRef(TestUtils.application1);
        //application.setState();

        //State receicedState = new State();

        //receicedState.setApplications();
    }

}
