/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.JSONAction;
import dk.opendesk.foundationapplication.DAO.State;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class StateTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public StateTest() {
        super("/foundation/state");
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
    
    public void testState() throws Exception {
        for(WorkflowSummary workflow : foundationBean.getWorkflowSummaries()){
            List<StateReference> states = workflow.getStates();
            assertEquals(4, states.size());
            for(StateReference stateRef : states){
                State state = get(State.class, stateRef.getNodeID());
                assertEquals(stateRef.getTitle(), state.getTitle());
            }
        }
    }

    public void testGetStateActions() throws Exception {
        NodeRef stateRef = TestUtils.stateAcceptedRef;
        QName aspect = Utilities.getODFName(Utilities.ASPECT_ON_CREATE);
        Map<String, Serializable> params = new HashMap<>();
        params.put("cc","te@st.com");
        params.put("ignore_send_failure", true);
        foundationBean.saveAction("mail", stateRef, aspect, params);

        List<JSONAction> actions = foundationBean.getActions(stateRef);
        assertEquals(1,actions.size());

        JSONAction action = actions.get(0);
        String name = action.getName();
        assertEquals("mail", name);

        Map<String, Serializable> actParams = action.getParameters();
        assertEquals(2, actParams.size());
        assertEquals(params,actParams);
    }
    
}
