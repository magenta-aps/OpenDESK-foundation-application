/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.State;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

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
    
    
}
