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

    public static final String BUDGET1_NAME = "defaultBudget";
    public static final Long BUDGET1_AMOUNT = 1000000000000000l;
    public static final String BUDGET2_NAME = "unusedBudget";
    public static final Long BUDGET2_AMOUNT = 1000000l;

    public static final String TITLE_POSTFIX = "(Title)";

    public static NodeRef workFlowRef;

    public static NodeRef stateRecievedRef;
    public static NodeRef stateAccessRef;
    public static NodeRef stateDeniedRef;
    public static NodeRef stateAcceptedRef;

    public static NodeRef branchRef;

    public static NodeRef budgetRef1;
    public static NodeRef budgetRef2;

    public static NodeRef application1;
    public static NodeRef application2;
    public static NodeRef application3;

    private static boolean isInitiated = false;

    private TestUtils() {
    }

    ;
    
    public synchronized static void wipeData(ServiceRegistry serviceRegistry) throws Exception {
        NodeService nodeService = serviceRegistry.getNodeService();

        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);
        NodeRef dataRef = foundationBean.getDataHome();

        for (NodeRef workflow : foundationBean.getWorkflows()) {
            nodeService.removeChild(dataRef, workflow);
        }

        for (NodeRef budget : foundationBean.getBudgetRefs()) {
            nodeService.removeChild(dataRef, budget);
        }

        for (NodeRef branch : foundationBean.getBranches()) {
            nodeService.removeChild(dataRef, branch);
        }

        for (ApplicationSummary application : foundationBean.getApplicationSummaries()) {
            nodeService.removeChild(dataRef, application.asNodeRef());
        }

        isInitiated = false;

    }

    public synchronized static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception {
        //When we are using static properties, we need to make sure that tests aren't accidentaly run in parallel
        if(isInitiated){
            throw new RuntimeException("Test has already been initiated. Did you remember to call WipeData?");
        }
        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);

        //Create workflow
        workFlowRef = foundationBean.addNewWorkflow(WORKFLOW_NAME, WORKFLOW_NAME + TITLE_POSTFIX);

        //Create workflow states
        stateRecievedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME + TITLE_POSTFIX);
        stateAccessRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ASSESS_NAME, STATE_ASSESS_NAME + TITLE_POSTFIX);
        stateDeniedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_DENIED_NAME, STATE_DENIED_NAME + TITLE_POSTFIX);
        stateAcceptedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME + TITLE_POSTFIX);
        foundationBean.setWorkflowEntryPoint(workFlowRef, stateRecievedRef);

        //Create associations
        foundationBean.createWorkflowTransition(stateRecievedRef, stateAccessRef);
        foundationBean.createWorkflowTransition(stateRecievedRef, stateDeniedRef);

        foundationBean.createWorkflowTransition(stateAccessRef, stateAcceptedRef);
        foundationBean.createWorkflowTransition(stateAccessRef, stateDeniedRef);

        //Create branch and associate it with the workflow
        branchRef = foundationBean.addNewBranch(BRANCH_NAME, BRANCH_NAME + TITLE_POSTFIX);
        foundationBean.addBranchWorkflow(branchRef, workFlowRef);

        //Create budget and associate it with a branch
        budgetRef1 = foundationBean.addNewBudget(BUDGET1_NAME, BUDGET1_NAME + TITLE_POSTFIX, BUDGET1_AMOUNT);
        budgetRef2 = foundationBean.addNewBudget(BUDGET2_NAME, BUDGET2_NAME + TITLE_POSTFIX, BUDGET2_AMOUNT);
        foundationBean.addBranchBudget(branchRef, budgetRef1);
        foundationBean.addBranchBudget(branchRef, budgetRef2);

        application1 = foundationBean.addNewApplication(branchRef, budgetRef1, APPLICATION1_NAME, APPLICATION1_NAME + TITLE_POSTFIX, "Category1", "Lars Larsen INC", "Tværstrede", 9, "2", "1234", "Lars", "Larsen", "lars@larsen.org", "004512345678", "Give me money", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(2))), APPLICATION1_AMOUNT, "4321", "00035254");
        application2 = foundationBean.addNewApplication(branchRef, budgetRef1, APPLICATION2_NAME, APPLICATION2_NAME + TITLE_POSTFIX, "Category2", "Lars Larsen INC", "Tværstrede", 9, "2", "1234", "Lars", "Larsen", "lars@larsen.org", "004512345678", "Give me more money", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(4))), APPLICATION2_AMOUNT, "4321", "00035254");
        application3 = foundationBean.addNewApplication(null, null, APPLICATION3_NAME, APPLICATION3_NAME + TITLE_POSTFIX, "Category3", "Lars Larsen INC", "Tværstrede", 9, "2", "1234", "Lars", "Larsen", "lars@larsen.org", "004512345678", "Give me more money", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(4))), APPLICATION3_AMOUNT, "4321", "00035254");
        isInitiated = true;
    }

}
