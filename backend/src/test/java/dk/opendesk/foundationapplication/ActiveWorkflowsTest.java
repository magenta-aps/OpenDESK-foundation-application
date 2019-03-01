/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class ActiveWorkflowsTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public ActiveWorkflowsTest() {
        super("/foundation/activeworkflow");
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
    
    public void testActiveWorkflows() throws Exception{
        List<WorkflowSummary> beanSummaries = foundationBean.getWorkflowSummaries();
        assertEquals(1, beanSummaries.size());
        assertEquals(TestUtils.WORKFLOW_NAME+TestUtils.TITLE_POSTFIX, beanSummaries.get(0).getTitle());
        
        List<WorkflowSummary> restSummaries = get(List.class, WorkflowSummary.class);
        assertEquals(1, restSummaries.size());
        assertEquals(beanSummaries.get(0).getTitle(), restSummaries.get(0).getTitle());
        
    }
}
