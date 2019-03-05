/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.enums.StateCategory;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final String BUDGETYEAR1_NAME = "CurrentYear";
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

    public static NodeRef budgetYearRef1;
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

        for (NodeRef budgetYear : foundationBean.getBudgetYearRefs()) {
            nodeService.removeChild(dataRef, budgetYear);
        }

        for (NodeRef branch : foundationBean.getBranches()) {
            nodeService.removeChild(dataRef, branch);
        }

        for (ApplicationSummary application : foundationBean.getApplicationSummaries()) {
            nodeService.removeChild(dataRef, application.asNodeRef());
        }

        for (ApplicationSummary application : foundationBean.getDeletedApplicationSummaries()) {
            nodeService.removeChild(dataRef, application.asNodeRef());
        }

        isInitiated = false;

    }

    public synchronized static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception {
        //When we are using static properties, we need to make sure that tests aren't accidentaly run in parallel
        if (isInitiated) {
            throw new RuntimeException("Test has already been initiated. Did you remember to call WipeData?");
        }
        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);

        //Create workflow
        workFlowRef = foundationBean.addNewWorkflow(WORKFLOW_NAME, WORKFLOW_NAME + TITLE_POSTFIX);

        //Create workflow states
        stateRecievedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME + TITLE_POSTFIX, StateCategory.NOMINATED);
        stateAccessRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ASSESS_NAME, STATE_ASSESS_NAME + TITLE_POSTFIX, StateCategory.ACCEPTED);
        stateDeniedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_DENIED_NAME, STATE_DENIED_NAME + TITLE_POSTFIX, StateCategory.REJECTED);
        stateAcceptedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME + TITLE_POSTFIX, StateCategory.CLOSED);
        foundationBean.setWorkflowEntryPoint(workFlowRef, stateRecievedRef);

        //Create associations
        foundationBean.createWorkflowTransition(stateRecievedRef, stateAccessRef);
        foundationBean.createWorkflowTransition(stateRecievedRef, stateDeniedRef);

        foundationBean.createWorkflowTransition(stateAccessRef, stateAcceptedRef);
        foundationBean.createWorkflowTransition(stateAccessRef, stateDeniedRef);

        //Create branch and associate it with the workflow
        branchRef = foundationBean.addNewBranch(BRANCH_NAME, BRANCH_NAME + TITLE_POSTFIX);
        foundationBean.addBranchWorkflow(branchRef, workFlowRef);

        //Create budgets and associate it with a branch
        Date startDate = Date.from(Instant.now().minus(Duration.ofDays(1)));
        Date endDate = Date.from(Instant.now().plus(300, ChronoUnit.DAYS));

        budgetYearRef1 = foundationBean.addNewBudgetYear(BUDGETYEAR1_NAME, BUDGETYEAR1_NAME + TITLE_POSTFIX, startDate, endDate);

        budgetRef1 = foundationBean.addNewBudget(budgetYearRef1, BUDGET1_NAME, BUDGET1_NAME + TITLE_POSTFIX, BUDGET1_AMOUNT);
        budgetRef2 = foundationBean.addNewBudget(budgetYearRef1, BUDGET2_NAME, BUDGET2_NAME + TITLE_POSTFIX, BUDGET2_AMOUNT);
        foundationBean.addBranchBudget(branchRef, budgetRef1);
        foundationBean.addBranchBudget(branchRef, budgetRef2);

        ArrayList<ApplicationPropertyValue> fields;
        Application app1 = new Application();
        app1.setBranchSummary(foundationBean.getBranchSummary(branchRef));
        app1.setBudget(foundationBean.getBudgetReference(budgetRef1));
        app1.setTitle(APPLICATION1_NAME);
        ApplicationPropertiesContainer app1blockRecipient = new ApplicationPropertiesContainer();
        app1blockRecipient.setId("1");
        app1blockRecipient.setLabel("Recipient");
        ApplicationPropertiesContainer app1blockOverview = new ApplicationPropertiesContainer();
        app1blockOverview.setId("2");
        app1blockOverview.setLabel("Overview");
        ApplicationPropertiesContainer app1Details = new ApplicationPropertiesContainer();
        app1Details.setId("3");
        app1Details.setLabel("Details");

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("1", "Recipient", "display:block;", "text", String.class, null, "Lars Larsen INC"));
        fields.add(ResetDemoData.buildValue("2", "Road", "display:block;", "text", String.class, null, "Tværstrede"));
        fields.add(ResetDemoData.buildValue("3", "Number", "display:block;", "Integer", Integer.class, null, 9));
        fields.add(ResetDemoData.buildValue("4", "Floor", "display:block;", "text", String.class, null, "2 th"));
        fields.add(ResetDemoData.buildValue("5", "Postal code", "display:block;", "text", String.class, null, "1234"));
        fields.add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, null, "Lars"));
        fields.add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, null, "Larsen"));
        fields.add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), "lars@larsen.org"));
        fields.add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, null, "004512345678"));
        app1blockRecipient.setFields(fields);

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("10", "Category", "display:block;", "text", String.class, null, "Category1"));
        fields.add(ResetDemoData.buildValue("11", "Short Description", "display:block;", "text", String.class, null, "Give me money"));
        fields.add(ResetDemoData.buildValue("12", "Start Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now())));
        fields.add(ResetDemoData.buildValue("13", "End Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now().plus(Duration.ofDays(2)))));
        app1blockOverview.setFields(fields);

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("14", "Applied Amount", "display:block;", "Long", Long.class, Functional.amount(), APPLICATION1_AMOUNT));
        fields.add(ResetDemoData.buildValue("15", "Registration Number", "display:block;", "Long", String.class, null, "4321"));
        fields.add(ResetDemoData.buildValue("16", "Account Number", "display:block;", "Long", String.class, null, "00035254"));
        app1Details.setFields(fields);
        app1.setBlocks(Arrays.asList(new ApplicationPropertiesContainer[]{app1blockRecipient, app1blockOverview, app1Details}));
        application1 = foundationBean.addNewApplication(app1).asNodeRef();

        //application1 = foundationBean.addNewApplication(branchRef, budgetRef1, APPLICATION1_NAME, APPLICATION1_NAME + TITLE_POSTFIX,, "", "", , "", "", "", "", "", "", "", , , , "", "");
        Application app2 = new Application();
        app2.setBranchSummary(foundationBean.getBranchSummary(branchRef));
        app2.setBudget(foundationBean.getBudgetReference(budgetRef1));
        app2.setTitle(APPLICATION2_NAME);
        ApplicationPropertiesContainer app2blockRecipient = new ApplicationPropertiesContainer();
        app2blockRecipient.setId("1");
        app2blockRecipient.setLabel("Recipient");
        ApplicationPropertiesContainer app2blockOverview = new ApplicationPropertiesContainer();
        app2blockOverview.setId("2");
        app2blockOverview.setLabel("Overview");
        ApplicationPropertiesContainer app2details = new ApplicationPropertiesContainer();
        app2details.setId("3");
        app2details.setLabel("Details");

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("1", "Recipient", "display:block;", "text", String.class, null, "Lars Larsen INC"));
        fields.add(ResetDemoData.buildValue("2", "Road", "display:block;", "text", String.class, null, "Tværstrede"));
        fields.add(ResetDemoData.buildValue("3", "Number", "display:block;", "Integer", Integer.class, null, 9));
        fields.add(ResetDemoData.buildValue("4", "Floor", "display:block;", "text", String.class, null, "2"));
        fields.add(ResetDemoData.buildValue("5", "Postal code", "display:block;", "text", String.class, null, "1234"));
        fields.add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, null, "Lars"));
        fields.add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, null, "Larsen"));
        fields.add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), "lars@larsen.org"));
        fields.add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, null, "004512345678"));
        app2blockRecipient.setFields(fields);

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("10", "Category", "display:block;", "text", String.class, null, "Category2"));
        fields.add(ResetDemoData.buildValue("11", "Short Description", "display:block;", "text", String.class, null, "Give me more money"));
        fields.add(ResetDemoData.buildValue("12", "Start Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now())));
        fields.add(ResetDemoData.buildValue("13", "End Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now().plus(Duration.ofDays(4)))));
        app2blockOverview.setFields(fields);

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("14", "Applied Amount", "display:block;", "Long", Long.class, Functional.amount(), APPLICATION2_AMOUNT));
        fields.add(ResetDemoData.buildValue("15", "Registration Number", "display:block;", "Long", String.class, null, "4321"));
        fields.add(ResetDemoData.buildValue("16", "Account Number", "display:block;", "Long", String.class, null, "00035254"));
        app2details.setFields(fields);

        app2.setBlocks(Arrays.asList(new ApplicationPropertiesContainer[]{app2blockRecipient, app2blockOverview, app2details}));
        application2 = foundationBean.addNewApplication(app2).asNodeRef();

        //application2 = foundationBean.addNewApplication(branchRef, budgetRef1, APPLICATION2_NAME, APPLICATION2_NAME + TITLE_POSTFIX, "", "", "", , "", "", "", "", "", "", "", , , , "", "");
        Application app3 = new Application();
        app3.setTitle(APPLICATION3_NAME);
        ApplicationPropertiesContainer app3blockRecipient = new ApplicationPropertiesContainer();
        app3blockRecipient.setLabel("Recipient");
        app3blockRecipient.setId("1");
        ApplicationPropertiesContainer app3blockOverview = new ApplicationPropertiesContainer();
        app3blockOverview.setLabel("Overview");
        app3blockOverview.setId("2");
        ApplicationPropertiesContainer app3details = new ApplicationPropertiesContainer();
        app3details.setLabel("Details");
        app3details.setId("3");
        
        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("1", "Recipient", "display:block;", "text", String.class, null, "Lars Larsen INC"));
        fields.add(ResetDemoData.buildValue("2", "Road", "display:block;", "text", String.class, null, "Tværstrede"));
        fields.add(ResetDemoData.buildValue("3", "Number", "display:block;", "Integer", Integer.class, null, 9));
        fields.add(ResetDemoData.buildValue("4", "Floor", "display:block;", "text", String.class, null, "2"));
        fields.add(ResetDemoData.buildValue("5", "Postal code", "display:block;", "text", String.class, null, "1234"));
        fields.add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, null, "Lars"));
        fields.add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, null, "Larsen"));
        fields.add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), "lars@larsen.org"));
        fields.add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, null, "004512345678"));
        app3blockRecipient.setFields(fields);

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("10", "Category", "display:block;", "text", String.class, null, "Category3"));
        fields.add(ResetDemoData.buildValue("11", "Short Description", "display:block;", "text", String.class, null, "Give me even more money"));
        fields.add(ResetDemoData.buildValue("12", "Start Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now())));
        fields.add(ResetDemoData.buildValue("13", "End Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now().plus(Duration.ofDays(4)))));
        app3blockOverview.setFields(fields);

        fields = new ArrayList<>();
        fields.add(ResetDemoData.buildValue("14", "Applied Amount", "display:block;", "Long", Long.class, Functional.amount(), APPLICATION3_AMOUNT));
        fields.add(ResetDemoData.buildValue("15", "Registration Number", "display:block;", "Long", String.class, null, "4321"));
        fields.add(ResetDemoData.buildValue("16", "Account Number", "display:block;", "Long", String.class, null, "00035254"));
        app3details.setFields(fields);

        app3.setBlocks(Arrays.asList(new ApplicationPropertiesContainer[]{app3blockRecipient, app3blockOverview, app3details}));
        application3 = foundationBean.addNewApplication(app3).asNodeRef();
        //application3 = foundationBean.addNewApplication(null, null, APPLICATION3_NAME, APPLICATION3_NAME + TITLE_POSTFIX, "", "", "", , "", "", "", "", "", "", "", , , , "", "");
        isInitiated = true;
    }
}
