/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class WorkflowTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public WorkflowTest() {
        super("/foundation/workflow");
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
    
    public List<WorkflowSummary> testGetWorkflows() throws Exception{
        List<WorkflowSummary> beanSummaries = foundationBean.getWorkflowSummaries();
        assertEquals(1, beanSummaries.size());
        assertEquals(TestUtils.WORKFLOW_NAME+TestUtils.TITLE_POSTFIX, beanSummaries.get(0).getTitle());
        
        List<WorkflowSummary> restSummaries = get(List.class, WorkflowSummary.class);
        
        containsSameElements(beanSummaries, restSummaries);
        
        return restSummaries;
        
    }
    
    public void testGetWorkflow() throws Exception {
        List<WorkflowSummary> summaries = testGetWorkflows();
        WorkflowSummary testSummary = summaries.get(0);
        Workflow beanWorkflow = foundationBean.getWorkflow(testSummary.asNodeRef());
        assertEquals(testSummary.getNodeRef(), beanWorkflow.getNodeRef());
        assertEquals(testSummary.getTitle(), beanWorkflow.getTitle());

        Workflow restWorkflow = get(Workflow.class, beanWorkflow.getNodeID());
        assertEquals(beanWorkflow, restWorkflow);
        
    }
}
