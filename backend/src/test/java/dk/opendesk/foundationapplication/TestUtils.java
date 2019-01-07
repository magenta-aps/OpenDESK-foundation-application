/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public final class TestUtils {
    private static final Logger LOGGER = Logger.getLogger(TestUtils.class);
    
    public static final String ADMIN_USER = "admin";
    
    public static final String WORKFLOW_NAME = "defaultWorkFlow";
    
    public static final String BRANCH_NAME = "defaultBranch";
    
    public static final String APPLICATION1_NAME = "defaultApplication1";
    public static final Long APPLICATION1_AMOUNT = 100000l;
    public static final String APPLICATION2_NAME = "defaultApplication2";
    public static final Long APPLICATION2_AMOUNT = 200000l;
    public static final String APPLICATION3_NAME = "defaultNewApplication";
    public static final Long APPLICATION3_AMOUNT = 5l;
    
    public static final String STATE_RECIEVED_NAME = "recieved";
    public static final String STATE_ASSESS_NAME = "assesment";
    public static final String STATE_DENIED_NAME = "denied";
    public static final String STATE_ACCEPTED_NAME = "accepted";
    
    public static final String BUDGET_NAME = "defaultBudget";
    public static final Long BUDGET_AMOUNT = 1000000000000000l;
    
    public static final String TITLE_POSTFIX = "(Title)";
    
    private TestUtils(){};
    
    public static void wipeData(ServiceRegistry serviceRegistry) throws Exception{
        NodeService nodeService = serviceRegistry.getNodeService();
        
        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);
        NodeRef dataRef = foundationBean.getDataHome();
        
        
        for(NodeRef workflow : foundationBean.getWorkflows()){
            nodeService.removeChild(dataRef, workflow);
        }
        
        for(NodeRef budget : foundationBean.getBudgetRefs()){
            nodeService.removeChild(dataRef, budget);
        }
        
        for(NodeRef branch : foundationBean.getBranches()){
            nodeService.removeChild(dataRef, branch);
        }
        
        for(ApplicationSummary application : foundationBean.getApplicationSummaries()){
            nodeService.removeChild(dataRef, application.asNodeRef());
        }
        
        
        
    }
    
    public static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception{
        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);
        
        
        //Create workflow
        NodeRef workFlowRef = foundationBean.addNewWorkflow(WORKFLOW_NAME, WORKFLOW_NAME+TITLE_POSTFIX);
        
        //Create workflow states
        NodeRef stateRecievedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME+TITLE_POSTFIX);
        NodeRef stateAccessRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ASSESS_NAME, STATE_ASSESS_NAME+TITLE_POSTFIX);
        NodeRef stateDeniedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_DENIED_NAME, STATE_DENIED_NAME+TITLE_POSTFIX);
        NodeRef stateAcceptedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME+TITLE_POSTFIX);
        foundationBean.setWorkflowEntryPoint(workFlowRef, stateRecievedRef);
        
        //Create associations
        foundationBean.createWorkflowTransition(stateRecievedRef, stateAccessRef);
        foundationBean.createWorkflowTransition(stateRecievedRef, stateDeniedRef);
        
        foundationBean.createWorkflowTransition(stateAccessRef, stateAcceptedRef);
        foundationBean.createWorkflowTransition(stateAccessRef, stateDeniedRef);
    
        //Create branch and associate it with the workflow
        NodeRef branchRef = foundationBean.addNewBranch(BRANCH_NAME, BRANCH_NAME+TITLE_POSTFIX);
        foundationBean.setBranchWorkflow(branchRef, workFlowRef);
        
        //Create budget and associate it with a branch
        NodeRef budgetRef = foundationBean.addNewBudget(BUDGET_NAME, BUDGET_NAME+TITLE_POSTFIX, BUDGET_AMOUNT);
        foundationBean.setBranchBudget(branchRef, budgetRef);
        
        NodeRef application1 = foundationBean.addNewApplication(branchRef, budgetRef, APPLICATION1_NAME, APPLICATION1_NAME+TITLE_POSTFIX, "Category1", "Lars Larsen INC", "Tværstrede", 9, "2", "1234", "Lars", "Larsen", "lars@larsen.org", "004512345678", "Give me money", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(2))), APPLICATION1_AMOUNT, "4321", "00035254");
        NodeRef application2 = foundationBean.addNewApplication(branchRef, budgetRef, APPLICATION2_NAME, APPLICATION2_NAME+TITLE_POSTFIX, "Category2", "Lars Larsen INC", "Tværstrede", 9, "2", "1234", "Lars", "Larsen", "lars@larsen.org", "004512345678", "Give me more money", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(4))), APPLICATION2_AMOUNT, "4321", "00035254");
        NodeRef application3 = foundationBean.addNewApplication(null, null, APPLICATION3_NAME, APPLICATION3_NAME+TITLE_POSTFIX, "Category3", "Lars Larsen INC", "Tværstrede", 9, "2", "1234", "Lars", "Larsen", "lars@larsen.org", "004512345678", "Give me more money", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(4))), APPLICATION3_AMOUNT, "4321", "00035254");
        
    }
    
    
    
    

    
}