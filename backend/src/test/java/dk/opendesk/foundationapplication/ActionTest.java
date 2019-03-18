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
        ParameterDefinition stateIdParamDef = new ParameterDefinitionImpl(ACTION_PARAM_STATE, DataTypeDefinition.TEXT, true, null);
        ParameterDefinition aspectParemDef = new ParameterDefinitionImpl(ACTION_PARAM_ASPECT, DataTypeDefinition.TEXT, true, null);
        ParameterDefinition ccParam = new ParameterDefinitionImpl(PARAM_CC, DataTypeDefinition.TEXT, false, null);

        FoundationActionParameterValue stateIdParam = new FoundationActionParameterValue(stateIdParamDef, TestUtils.stateRecievedRef.getId());
        FoundationActionParameterValue aspectParam = new FoundationActionParameterValue(aspectParemDef, ASPECT_ON_CREATE);

        List<FoundationActionParameterValue> params = new ArrayList<>();
        params.add(new FoundationActionParameterValue(ccParam, "test@test.dk"));

        FoundationActionValue foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParam, aspectParam, params);
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
        stateIdParam = new FoundationActionParameterValue(stateIdParamDef, "wrong state id");
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParam, aspectParam, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        stateIdParam = new FoundationActionParameterValue(stateIdParamDef, null);
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParam, aspectParam, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        //testing wrong/missing aspect name
        stateIdParam = new FoundationActionParameterValue(stateIdParamDef, TestUtils.stateRecievedRef.getId());
        aspectParam = new FoundationActionParameterValue(aspectParemDef, "wrong aspect");
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParam, aspectParam, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);

        aspectParam = new FoundationActionParameterValue(aspectParemDef, null);
        foundationActionValue = new FoundationActionValue(ACTION_NAME_EMAIL, stateIdParam, aspectParam, params);
        post(foundationActionValue, ACTION_NAME_EMAIL, Status.STATUS_BAD_REQUEST);
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
