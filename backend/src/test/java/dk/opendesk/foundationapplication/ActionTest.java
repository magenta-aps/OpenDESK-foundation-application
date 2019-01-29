package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.Test;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dk.opendesk.foundationapplication.Utilities.ASPECT_ON_CREATE;
import static dk.opendesk.foundationapplication.Utilities.getODFName;

public class ActionTest extends AbstractTestClass {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public ActionTest() {
        super("/foundation/actions");
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
}
