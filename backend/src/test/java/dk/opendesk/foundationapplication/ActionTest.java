package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.*;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static dk.opendesk.foundationapplication.Utilities.*;

public class ActionTest extends AbstractTestClass {

    public ActionTest() {
        super("/foundation/action");
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


    public void testGetActions() throws IOException {

        List<FoundationAction> actions = get(List.class, FoundationAction.class, "");

        assertEquals(4, actions.size());
        assertEquals(ACTION_NAME_EMAIL, actions.get(0).getName());
        assertEquals(9,actions.get(0).getParams().size());
    }


    public void testSaveAction() throws Exception {

        JSONObject data = new JSONObject();
        data.put("stateRef", TestUtils.stateRecievedRef);
        data.put("aspect", ASPECT_ON_CREATE);
        data.put("cc", "test@test.dk"); //TODO make sure only real email adresses can be set as parameters
        post(data, ACTION_NAME_EMAIL);

        List<Action> actions = getServiceRegistry().getActionService().getActions(TestUtils.stateRecievedRef);
        for (Action act : actions) {
            if (act.getActionDefinitionName().equals(ACTION_NAME_EMAIL)) {

                //testing the aspect is set on the action
                Set<QName> aspects = getServiceRegistry().getNodeService().getAspects(act.getNodeRef());
                assertTrue(aspects.contains(Utilities.getODFName(ASPECT_ON_CREATE)));

                //testing the parameter is set on the action
                assertEquals(act.getParameterValue("cc"), "test@test.dk");
            }
        }

        //testing missing state NodeRef
        data.remove("stateRef");
        post(data, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        //testing wrong aspect name
        data.put("stateRef", TestUtils.stateRecievedRef);
        data.put("aspect", "totallyWrongAspect");
        post(data, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);
    }




    public void testActionExecutingOnStateChange() throws Exception {

        //saving actions to state 'received'
        JSONObject dataForEnterAction = new JSONObject();
        dataForEnterAction.put("stateRef", TestUtils.stateAccessRef);
        dataForEnterAction.put("aspect", ASPECT_ON_CREATE);
        dataForEnterAction.put("executionMessage", "enterAction executed");

        JSONObject dataForExitAction = new JSONObject();
        dataForExitAction.put("stateRef", TestUtils.stateAccessRef);
        dataForExitAction.put("aspect", ASPECT_BEFORE_DELETE);
        dataForExitAction.put("executionMessage", "exitAction executed");

        post(dataForEnterAction,"test");
        post(dataForExitAction,"test");

        //changing the application into the assessment state
        NodeRef appRef = TestUtils.application1;
        Application change = new Application();
        change.parseRef(appRef);
        StateReference ref = new StateReference();
        ref.parseRef(TestUtils.stateAccessRef);
        change.setState(ref);
        getApplicationBean().updateApplication(change);
        Application app = getApplicationBean().getApplication(appRef);

        //testing that enterStateAction got executed
        assertEquals("enterAction executed",app.emailTo().getValue());

        //changing the application out of the assessment state
        ref.parseRef(TestUtils.stateAcceptedRef);
        change.setState(ref);
        getApplicationBean().updateApplication(change);
        app = getApplicationBean().getApplication(appRef);

        //testing that enterStateAction got executed
        assertEquals("exitAction executed",app.emailTo().getValue());
    }

}
