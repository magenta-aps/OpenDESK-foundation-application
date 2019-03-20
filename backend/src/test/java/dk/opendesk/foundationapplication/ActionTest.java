package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.*;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static dk.opendesk.foundationapplication.Utilities.*;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_CC;

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

        System.out.println(actions);
        assertEquals(4, actions.size());
        assertEquals(ACTION_NAME_EMAIL, actions.get(0).getName());
        assertEquals(9,actions.get(0).getParams().size());
    }


    public void testSaveAction() throws Exception {
        FoundationActionParameterDefinition<String> stateIdParam = new FoundationActionParameterDefinition<>(ACTION_PARAM_STATE, DataTypeDefinition.TEXT, String.class, true, null);
        FoundationActionParameterDefinition<String> aspectParam = new FoundationActionParameterDefinition<>(ACTION_PARAM_ASPECT, DataTypeDefinition.TEXT, String.class, true, null);
        FoundationActionParameterDefinition<String> ccParam = new FoundationActionParameterDefinition<>(PARAM_CC, DataTypeDefinition.TEXT, String.class, false, null);

        FoundationActionParameterValue stateIdParamVal = new FoundationActionParameterValue<>(stateIdParam, TestUtils.stateRecievedRef.getId());
        FoundationActionParameterValue aspectParamVal = new FoundationActionParameterValue<>(aspectParam, ASPECT_ON_CREATE);

        List<FoundationActionParameterValue> params = new ArrayList<>();
        params.add(new FoundationActionParameterValue<>(ccParam, "test@test.dk"));

        FoundationActionValue foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, ACTION_NAME_EMAIL); //todo dobbelt konfekt at man skal skrive navnet

        List<Action> actions = getServiceRegistry().getActionService().getActions(TestUtils.stateRecievedRef);
        Action savedEmailAction = null;
        for (Action act : actions) {
            if (act.getActionDefinitionName().equals(ACTION_NAME_EMAIL)) {
                savedEmailAction = act;
            }
        }

        //testing the aspect is set on the action
        assertNotNull(savedEmailAction);
        Set<QName> aspects = getServiceRegistry().getNodeService().getAspects(savedEmailAction.getNodeRef());
        assertTrue(aspects.contains(Utilities.getODFName(ASPECT_ON_CREATE)));

        //testing the parameter is set on the action
        assertEquals(savedEmailAction.getParameterValue("cc"), "test@test.dk");


        //testing wrong/missing state NodeRef
        stateIdParamVal = new FoundationActionParameterValue(stateIdParam, "wrong state id");
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        stateIdParamVal = new FoundationActionParameterValue(stateIdParam, null);
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        //testing wrong/missing aspect name
        stateIdParamVal = new FoundationActionParameterValue(stateIdParam, TestUtils.stateRecievedRef.getId());
        aspectParamVal = new FoundationActionParameterValue(aspectParam, "wrong aspect");
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        aspectParamVal = new FoundationActionParameterValue(aspectParam, null);
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);
    }




    public void testActionExecutingOnStateChange() throws Exception {

        //saving on-create-action to state 'assess'
        FoundationActionParameterDefinition<String> stateIdParam = new FoundationActionParameterDefinition<>(ACTION_PARAM_STATE, DataTypeDefinition.TEXT, String.class, true, null);
        FoundationActionParameterDefinition<String> aspectParam = new FoundationActionParameterDefinition<>(ACTION_PARAM_ASPECT, DataTypeDefinition.TEXT, String.class, true, null);
        FoundationActionParameterDefinition<String> msgParam = new FoundationActionParameterDefinition<>("executionMessage", DataTypeDefinition.TEXT, String.class, false, null);

        FoundationActionParameterValue stateIdParamVal = new FoundationActionParameterValue<>(stateIdParam, TestUtils.stateAccessRef.getId());
        FoundationActionParameterValue aspectParamVal = new FoundationActionParameterValue<>(aspectParam, ASPECT_ON_CREATE);

        List<FoundationActionParameterValue> params = new ArrayList<>();
        params.add(new FoundationActionParameterValue<>(msgParam, "enterAction executed"));

        FoundationActionValue foundationActionValue = new FoundationActionValue("test", stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, "test");


        //saving before-delete-action to state 'assess'
        aspectParamVal = new FoundationActionParameterValue<>(aspectParam, ASPECT_BEFORE_DELETE);

        params = new ArrayList<>();
        params.add(new FoundationActionParameterValue<>(msgParam, "exitAction executed"));

        foundationActionValue = new FoundationActionValue("test", stateIdParamVal, aspectParamVal, params);
        post(foundationActionValue, "test");


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
