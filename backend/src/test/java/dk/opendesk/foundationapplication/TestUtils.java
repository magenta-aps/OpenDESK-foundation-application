/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.beans.AuthorityBean;
import dk.opendesk.foundationapplication.beans.BranchBean;
import dk.opendesk.foundationapplication.beans.BudgetBean;
import dk.opendesk.foundationapplication.beans.WorkflowBean;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import dk.opendesk.foundationapplication.enums.StateCategory;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public final class TestUtils {

    private static final Logger LOGGER = Logger.getLogger(TestUtils.class);

    public static final String USER_ALL_PERMISSIONS = "allperms";
    public static final String USER_ALL_READ_PERMISSIONS = "allreadperms";
    public static final String USER_BRANCH_READ = "branchread";
    public static final String USER_BRANCH_WRITE = "branchwrite";
    public static final String USER_BRANCH_WRITE_ALL_READ = "branchwriteallread";
    public static final String USER_WORKFLOW_READ = "workflowread";
    public static final String USER_WORKFLOW_WRITE = "workflowwrite";
    public static final String USER_BUDGET_READ = "budgetread";
    public static final String USER_BUDGET_WRITE = "budgetwrite";
    public static final String USER_BUDGETYEAR_READ = "budgetyearread";
    public static final String USER_BUDGETYEAR_WRITE = "budgetyearwrite";
    public static final String USER_SINGLE_APPLICATION_WRITE = "singleapplication";
    public static final String USER_NO_RIGHTS = "goaway";
    
    public static final String ADMIN_USER = "admin";
    
    public static final String SHARED_WORKFLOW_NAME = "sharedWorkFlow";
    public static final String ONE_USE_WORKFLOW_NAME = "oneUseWorkFlow";
    public static final String UNUSED_WORKFLOW = "unusedWorkflow";

    public static final String BRANCH_NAME1 = "w1Branch1";
    public static final String BRANCH_NAME2 = "w1Branch2";
    public static final String BRANCH_NAME3 = "w2Branch1";
    public static final String BRANCH_NAME4 = "NonAsignedBranch";

    public static final String APPLICATION1_NAME = "defaultApplication1";
    public static final Long APPLICATION1_AMOUNT = 100000l;
    public static final String APPLICATION2_NAME = "defaultApplication2";
    public static final Long APPLICATION2_AMOUNT = 200000l;
    public static final String APPLICATION3_NAME = "defaultNewApplication";
    public static final Long APPLICATION3_AMOUNT = 5l;

    public static final String STATE_RECIEVED_NAME = "recieved";
    public static final String STATE_ASSESS_NAME = "assesment";
    public static final String STATE_EXECUTE_NAME = "execute";
    public static final String STATE_DENIED_NAME = "denied";
    public static final String STATE_ACCEPTED_NAME = "accepted";

    public static final String BUDGETYEAR1_NAME = "CurrentYear";
    public static final String BUDGETYEAR2_NAME = "NextYear";
    public static final String BUDGET1_NAME = "defaultBudget";
    public static final Long BUDGET1_AMOUNT = 200000000l;
    public static final String BUDGET2_NAME = "smallBudget1";
    public static final Long BUDGET2_AMOUNT = 100000l;
    public static final String BUDGET3_NAME = "smallBudget2";
    public static final Long BUDGET3_AMOUNT = 80000l;
    public static final String BUDGET4_NAME = "nextYearBudget";
    public static final Long BUDGET4_AMOUNT = 100000000l;
    public static final String BUDGET5_NAME = "nextYearSmallBudget";
    public static final Long BUDGET5_AMOUNT = 10000l;

    public static final String TITLE_POSTFIX = "(Title)";
    
    public static final Date START_DATE = Date.from(Instant.now().minus(Duration.ofDays(1)));
    public static final Date END_DATE = Date.from(Instant.now().plus(300, ChronoUnit.DAYS));
    public static final Date NEXT_YEAR_END_DATE = Date.from(END_DATE.toInstant().plus(300, ChronoUnit.DAYS));

    public static NodeRef workFlowRef1;
    public static NodeRef workFlowRef2;
    public static NodeRef workFlowRef3;

    public static NodeRef w1StateRecievedRef;
    public static NodeRef w1StateAccessRef;
    public static NodeRef w1StateDeniedRef;
    public static NodeRef w1StateAcceptedRef;
    
    public static NodeRef w2StateRecievedRef;
    public static NodeRef w2StateAccessRef;
    public static NodeRef w2StateExecuteRef;
    public static NodeRef w2StateDeniedRef;
    public static NodeRef w2StateAcceptedRef;
    
    public static NodeRef w3StateRecievedRef;
    public static NodeRef w3StateAccessRef;
    public static NodeRef w3StateExecuteRef;
    public static NodeRef w3StateAcceptedRef;

    public static NodeRef branchRef1;
    public static NodeRef branchRef2;
    public static NodeRef branchRef3;
    public static NodeRef branchRef4;

    public static NodeRef budgetYearRef1;
    public static NodeRef budgetYearRef2;
    public static NodeRef budgetRef1;
    public static NodeRef budgetRef2;
    public static NodeRef budgetRef3;
    public static NodeRef budgetRef4;
    public static NodeRef budgetRef5;

    public static NodeRef application1;
    public static NodeRef application2;
    public static NodeRef application3;

    public static NodeRef user_all_permission;
    public static NodeRef user_all_read_permission;
    public static NodeRef user_branch_read;
    public static NodeRef user_branch_write;
    public static NodeRef user_branch_write_all_read;
    public static NodeRef user_workflow_read;
    public static NodeRef user_workflow_write;
    public static NodeRef user_budget_read;
    public static NodeRef user_budget_write;
    public static NodeRef user_budgetYear_read;
    public static NodeRef user_budgetYear_write;
    public static NodeRef user_single_application_read;
    public static NodeRef user_no_rights;
    
    private static boolean isInitiated = false;

    private TestUtils() {
    }

    public synchronized static void wipeData(ServiceRegistry serviceRegistry) throws Exception {
        Utilities.wipeData(serviceRegistry);
        
        
        
        if(serviceRegistry.getPersonService().personExists(USER_ALL_PERMISSIONS)){
            serviceRegistry.getPersonService().deletePerson(USER_ALL_PERMISSIONS);
        }
        if(serviceRegistry.getPersonService().personExists(USER_ALL_READ_PERMISSIONS)){
            serviceRegistry.getPersonService().deletePerson(USER_ALL_READ_PERMISSIONS);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BRANCH_READ)){
            serviceRegistry.getPersonService().deletePerson(USER_BRANCH_READ);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BRANCH_WRITE)){
            serviceRegistry.getPersonService().deletePerson(USER_BRANCH_WRITE);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BRANCH_WRITE_ALL_READ)){
            serviceRegistry.getPersonService().deletePerson(USER_BRANCH_WRITE_ALL_READ);
        }
        if(serviceRegistry.getPersonService().personExists(USER_WORKFLOW_READ)){
            serviceRegistry.getPersonService().deletePerson(USER_WORKFLOW_READ);
        }
        if(serviceRegistry.getPersonService().personExists(USER_WORKFLOW_WRITE)){
            serviceRegistry.getPersonService().deletePerson(USER_WORKFLOW_WRITE);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BUDGET_READ)){
            serviceRegistry.getPersonService().deletePerson(USER_BUDGET_READ);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BUDGET_WRITE)){
            serviceRegistry.getPersonService().deletePerson(USER_BUDGET_WRITE);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BUDGETYEAR_READ)){
            serviceRegistry.getPersonService().deletePerson(USER_BUDGETYEAR_READ);
        }
        if(serviceRegistry.getPersonService().personExists(USER_BUDGETYEAR_WRITE)){
            serviceRegistry.getPersonService().deletePerson(USER_BUDGETYEAR_WRITE);
        }
        if(serviceRegistry.getPersonService().personExists(USER_SINGLE_APPLICATION_WRITE)){
            serviceRegistry.getPersonService().deletePerson(USER_SINGLE_APPLICATION_WRITE);
        }
        if(serviceRegistry.getPersonService().personExists(USER_NO_RIGHTS)){
            serviceRegistry.getPersonService().deletePerson(USER_NO_RIGHTS);
        }
        
    workFlowRef1 = null;
    workFlowRef2 = null;
    workFlowRef3 = null;

    w1StateRecievedRef = null;
    w1StateAccessRef = null;
    w1StateDeniedRef = null;
    w1StateAcceptedRef = null;
    
    w2StateRecievedRef = null;
    w2StateAccessRef = null;
    w2StateExecuteRef = null;
    w2StateDeniedRef = null;
    w2StateAcceptedRef = null;
    
    w3StateRecievedRef = null;
    w3StateAccessRef = null;
    w3StateExecuteRef = null;
    w3StateAcceptedRef = null;

    branchRef1 = null;
    branchRef2 = null;
    branchRef3 = null;
    branchRef4 = null;

    budgetYearRef1 = null;
    budgetYearRef2 = null;
    budgetRef1 = null;
    budgetRef2 = null;
    budgetRef3 = null;
    budgetRef4 = null;
    budgetRef5 = null;

    application1 = null;
    application2 = null;
    application3 = null;

    user_all_permission = null;
    user_all_read_permission = null;
    user_branch_read = null;
    user_branch_write = null;
    user_branch_write_all_read = null;
    user_workflow_read = null;
    user_workflow_write = null;
    user_budget_read = null;
    user_budget_write = null;
    user_budgetYear_read = null;
    user_budgetYear_write = null;
    user_single_application_read = null;
    user_no_rights = null;
        
        
    isInitiated = false;

    }

    public synchronized static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception {
        //When we are using static properties, we need to make sure that tests aren't accidentaly run in parallel
        if (isInitiated) {
            throw new RuntimeException("Test has already been initiated. Did you remember to call WipeData?");
        }
        long startTime = System.currentTimeMillis();
        ActionBean actionBean = new ActionBean();
        actionBean.setServiceRegistry(serviceRegistry);
        ApplicationBean applicationBean = new ApplicationBean();
        applicationBean.setServiceRegistry(serviceRegistry);
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);

        actionBean.setApplicationBean(applicationBean);

        applicationBean.setActionBean(actionBean);
        applicationBean.setAuthBean(authBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);

        branchBean.setApplicationBean(applicationBean);
        branchBean.setAuthBean(authBean);
        branchBean.setBudgetBean(budgetBean);
        branchBean.setWorkflowBean(workflowBean);

        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setAuthBean(authBean);
        budgetBean.setWorkflowBean(workflowBean);

        workflowBean.setApplicationBean(applicationBean);
        workflowBean.setAuthBean(authBean);

        //Create test user
        user_all_permission = createUser(USER_ALL_PERMISSIONS, serviceRegistry);
        user_all_read_permission = createUser(USER_ALL_READ_PERMISSIONS, serviceRegistry);
        user_no_rights = createUser(USER_NO_RIGHTS, serviceRegistry);
        

        setupBranch1(branchBean, workflowBean, budgetBean);


        

        ArrayList<ApplicationPropertyValue> fields;
        Application app1 = new Application();
        app1.setBranchSummary(branchBean.getBranchSummary(branchRef1));
        app1.setBudget(budgetBean.getBudgetReference(budgetRef1));
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
        fields.add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, Functional.first_name(), "Lars"));
        fields.add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, Functional.last_name(), "Larsen"));
        fields.add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), "lars@larsen.org"));
        fields.add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, Functional.phone_number(), "004512345678"));
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
        application1 = applicationBean.addNewApplication(app1).asNodeRef();

        //application1 = foundationBean.addNewApplication(branchRef, budgetRef1, APPLICATION1_NAME, APPLICATION1_NAME + TITLE_POSTFIX,, "", "", , "", "", "", "", "", "", "", , , , "", "");
        Application app2 = new Application();
        app2.setBranchSummary(branchBean.getBranchSummary(branchRef1));
        app2.setBudget(budgetBean.getBudgetReference(budgetRef1));
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
        fields.add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, Functional.first_name(), "Lars"));
        fields.add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, Functional.last_name(), "Larsen"));
        fields.add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), "lars@larsen.org"));
        fields.add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, Functional.phone_number(), "004512345678"));
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
        application2 = applicationBean.addNewApplication(app2).asNodeRef();

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
        fields.add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, Functional.first_name(), "Lars"));
        fields.add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, Functional.last_name(), "Larsen"));
        fields.add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), "lars@larsen.org"));
        fields.add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, Functional.phone_number(), "004512345678"));
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
        application3 = applicationBean.addNewApplication(app3).asNodeRef();
        //application3 = foundationBean.addNewApplication(null, null, APPLICATION3_NAME, APPLICATION3_NAME + TITLE_POSTFIX, "", "", "", , "", "", "", "", "", "", "", , , , "", "");


        authBean.addUserGroup(USER_ALL_PERMISSIONS, authBean.getGroup(PermissionGroup.SUPER, true));
        authBean.addUserGroup(USER_ALL_READ_PERMISSIONS, authBean.getGroup(PermissionGroup.SUPER, false));
        
        LOGGER.info("Created simple flow in "+(System.currentTimeMillis()-startTime));
        isInitiated = true;
    }
    
    public static void setupExtendedFlow(ServiceRegistry serviceRegistry) throws Exception{
        setupSimpleFlow(serviceRegistry);
        long startTime = System.currentTimeMillis();
        
        ActionBean actionBean = new ActionBean();
        actionBean.setServiceRegistry(serviceRegistry);
        ApplicationBean applicationBean = new ApplicationBean();
        applicationBean.setServiceRegistry(serviceRegistry);
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);

        actionBean.setApplicationBean(applicationBean);

        applicationBean.setActionBean(actionBean);
        applicationBean.setAuthBean(authBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);

        branchBean.setApplicationBean(applicationBean);
        branchBean.setAuthBean(authBean);
        branchBean.setBudgetBean(budgetBean);
        branchBean.setWorkflowBean(workflowBean);

        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setAuthBean(authBean);
        budgetBean.setWorkflowBean(workflowBean);

        workflowBean.setApplicationBean(applicationBean);
        workflowBean.setAuthBean(authBean);
        
        setupBranch2(branchBean, workflowBean, budgetBean);
        setupBranch3(branchBean, workflowBean, budgetBean);
        LOGGER.info("Created extended flow in "+(System.currentTimeMillis()-startTime));
    }
    
    public static void setupFullFlow(ServiceRegistry serviceRegistry) throws Exception{
        setupExtendedFlow(serviceRegistry);
        long startTime = System.currentTimeMillis();
        ActionBean actionBean = new ActionBean();
        actionBean.setServiceRegistry(serviceRegistry);
        ApplicationBean applicationBean = new ApplicationBean();
        applicationBean.setServiceRegistry(serviceRegistry);
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);

        actionBean.setApplicationBean(applicationBean);

        applicationBean.setActionBean(actionBean);
        applicationBean.setAuthBean(authBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);

        branchBean.setApplicationBean(applicationBean);
        branchBean.setAuthBean(authBean);
        branchBean.setBudgetBean(budgetBean);
        branchBean.setWorkflowBean(workflowBean);

        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setAuthBean(authBean);
        budgetBean.setWorkflowBean(workflowBean);

        workflowBean.setApplicationBean(applicationBean);
        workflowBean.setAuthBean(authBean);
        
        setupBranch4(branchBean, workflowBean, budgetBean);
        LOGGER.info("Created full flow in "+(System.currentTimeMillis()-startTime));
    }
    
    public static void setupFullTestUsers(ServiceRegistry serviceRegistry) throws Exception{
        setupFullFlow(serviceRegistry);
        ActionBean actionBean = new ActionBean();
        actionBean.setServiceRegistry(serviceRegistry);
        ApplicationBean applicationBean = new ApplicationBean();
        applicationBean.setServiceRegistry(serviceRegistry);
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);

        actionBean.setApplicationBean(applicationBean);

        applicationBean.setActionBean(actionBean);
        applicationBean.setAuthBean(authBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);

        branchBean.setApplicationBean(applicationBean);
        branchBean.setAuthBean(authBean);
        branchBean.setBudgetBean(budgetBean);
        branchBean.setWorkflowBean(workflowBean);

        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setAuthBean(authBean);
        budgetBean.setWorkflowBean(workflowBean);

        workflowBean.setApplicationBean(applicationBean);
        workflowBean.setAuthBean(authBean);
        
        user_branch_read = createUser(USER_BRANCH_READ, serviceRegistry);
        user_branch_write = createUser(USER_BRANCH_WRITE, serviceRegistry);
        user_branch_write_all_read = createUser(USER_BRANCH_WRITE_ALL_READ, serviceRegistry);
        user_workflow_read = createUser(USER_WORKFLOW_READ, serviceRegistry);
        user_workflow_write = createUser(USER_WORKFLOW_WRITE, serviceRegistry);
        user_budget_read = createUser(USER_BUDGET_READ, serviceRegistry);
        user_budget_write = createUser(USER_BUDGET_WRITE, serviceRegistry);
        user_budgetYear_read = createUser(USER_BUDGETYEAR_READ, serviceRegistry);
        user_budgetYear_write = createUser(USER_BUDGETYEAR_WRITE, serviceRegistry);
        user_single_application_read = createUser(USER_SINGLE_APPLICATION_WRITE, serviceRegistry);
        
        authBean.addUserGroup(USER_BRANCH_READ, authBean.getGroup(PermissionGroup.BRANCH, branchRef1, false));
        authBean.addUserGroup(USER_BRANCH_WRITE, authBean.getGroup(PermissionGroup.BRANCH, branchRef1, true));
        
        authBean.addUserGroup(USER_BRANCH_WRITE_ALL_READ, authBean.getGroup(PermissionGroup.BRANCH, branchRef1, true));
        authBean.addUserGroup(USER_BRANCH_WRITE_ALL_READ, authBean.getGroup(PermissionGroup.SUPER, false));
        
        authBean.addUserGroup(USER_WORKFLOW_READ, authBean.getGroup(PermissionGroup.WORKFLOW, workFlowRef1, false));      
        authBean.addUserGroup(USER_WORKFLOW_WRITE, authBean.getGroup(PermissionGroup.WORKFLOW, workFlowRef1, true));
        
        authBean.addUserGroup(USER_BUDGET_READ, authBean.getGroup(PermissionGroup.BUDGET, budgetRef1, false));
        authBean.addUserGroup(USER_BUDGET_WRITE, authBean.getGroup(PermissionGroup.BUDGET, budgetRef1, true));
        
        authBean.addUserGroup(USER_BUDGETYEAR_READ, authBean.getGroup(PermissionGroup.BUDGET_YEAR, budgetYearRef1, false));
        authBean.addUserGroup(USER_BUDGETYEAR_WRITE, authBean.getGroup(PermissionGroup.BUDGET_YEAR, budgetYearRef1, true));
        

        authBean.addReadPermission(application3, USER_SINGLE_APPLICATION_WRITE);
        authBean.addWritePermission(application3, USER_SINGLE_APPLICATION_WRITE);
        
        
        
    }
    
    public static NodeRef setupBranch1(BranchBean branchBean, WorkflowBean workflowBean, BudgetBean budgetBean) throws Exception {
        if (branchRef1 == null) {
            branchRef1 = branchBean.addNewBranch(BRANCH_NAME1, BRANCH_NAME1 + TITLE_POSTFIX);
            branchBean.setBranchWorkflow(branchRef1, setupWorkflow1(workflowBean));
            branchBean.addBranchBudget(branchRef1, setupBudget1(budgetBean));
            branchBean.addBranchBudget(branchRef1, setupBudget2(budgetBean));
        }
        return branchRef1;
    }
    
    public static NodeRef setupBranch2(BranchBean branchBean, WorkflowBean workflowBean, BudgetBean budgetBean) throws Exception {
        if (branchRef2 == null) {
            branchRef2 = branchBean.addNewBranch(BRANCH_NAME2, BRANCH_NAME2 + TITLE_POSTFIX);
            branchBean.setBranchWorkflow(branchRef2, setupWorkflow1(workflowBean));
            branchBean.addBranchBudget(branchRef2, setupBudget1(budgetBean));
            branchBean.addBranchBudget(branchRef2, setupBudget2(budgetBean));
        }
        return branchRef2;
    }

    public static NodeRef setupBranch3(BranchBean branchBean, WorkflowBean workflowBean, BudgetBean budgetBean) throws Exception {
        if (branchRef3 == null) {
            branchRef3 = branchBean.addNewBranch(BRANCH_NAME3, BRANCH_NAME3 + TITLE_POSTFIX);
            branchBean.setBranchWorkflow(branchRef3, setupWorkflow2(workflowBean));
            branchBean.addBranchBudget(branchRef3, setupBudget2(budgetBean));
            branchBean.addBranchBudget(branchRef3, setupBudget3(budgetBean));
        }
        return branchRef3;
    }

    public static NodeRef setupBranch4(BranchBean branchBean, WorkflowBean workflowBean, BudgetBean budgetBean) throws Exception {
        if (branchRef4 == null) {
            branchRef4 = branchBean.addNewBranch(BRANCH_NAME4, BRANCH_NAME4 + TITLE_POSTFIX);
            branchBean.addBranchBudget(branchRef4, setupBudget2(budgetBean));
        }
        return branchRef4;
    }

    public static NodeRef setupWorkflow1(WorkflowBean workflowBean) throws Exception {
        if (workFlowRef1 == null) {
            workFlowRef1 = workflowBean.addNewWorkflow(SHARED_WORKFLOW_NAME, SHARED_WORKFLOW_NAME + TITLE_POSTFIX);
            w1StateRecievedRef = workflowBean.addNewWorkflowState(workFlowRef1, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME + TITLE_POSTFIX, StateCategory.NOMINATED);
            w1StateAccessRef = workflowBean.addNewWorkflowState(workFlowRef1, STATE_ASSESS_NAME, STATE_ASSESS_NAME + TITLE_POSTFIX, StateCategory.ACCEPTED);
            w1StateDeniedRef = workflowBean.addNewWorkflowState(workFlowRef1, STATE_DENIED_NAME, STATE_DENIED_NAME + TITLE_POSTFIX, StateCategory.REJECTED);
            w1StateAcceptedRef = workflowBean.addNewWorkflowState(workFlowRef1, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME + TITLE_POSTFIX, StateCategory.CLOSED);
            workflowBean.setWorkflowEntryPoint(workFlowRef1, w1StateRecievedRef);

            workflowBean.createWorkflowTransition(w1StateRecievedRef, w1StateAccessRef);
            workflowBean.createWorkflowTransition(w1StateRecievedRef, w1StateDeniedRef);

            workflowBean.createWorkflowTransition(w1StateAccessRef, w1StateAcceptedRef);
            workflowBean.createWorkflowTransition(w1StateAccessRef, w1StateDeniedRef);
        }
        return workFlowRef1;
    }

    public static NodeRef setupWorkflow2(WorkflowBean workflowBean) throws Exception {
        if (workFlowRef2 == null) {
            workFlowRef2 = workflowBean.addNewWorkflow(ONE_USE_WORKFLOW_NAME, ONE_USE_WORKFLOW_NAME + TITLE_POSTFIX);
            w2StateRecievedRef = workflowBean.addNewWorkflowState(workFlowRef2, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME + TITLE_POSTFIX, null);
            w2StateAccessRef = workflowBean.addNewWorkflowState(workFlowRef2, STATE_ASSESS_NAME, STATE_ASSESS_NAME + TITLE_POSTFIX, StateCategory.NOMINATED);
            w2StateExecuteRef = workflowBean.addNewWorkflowState(workFlowRef2, STATE_EXECUTE_NAME, STATE_EXECUTE_NAME + TITLE_POSTFIX, StateCategory.ACCEPTED);
            w2StateDeniedRef = workflowBean.addNewWorkflowState(workFlowRef2, STATE_DENIED_NAME, STATE_DENIED_NAME + TITLE_POSTFIX, StateCategory.REJECTED);
            w2StateAcceptedRef = workflowBean.addNewWorkflowState(workFlowRef2, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME + TITLE_POSTFIX, StateCategory.CLOSED);
            workflowBean.setWorkflowEntryPoint(workFlowRef2, w2StateRecievedRef);

            workflowBean.createWorkflowTransition(w2StateRecievedRef, w2StateAccessRef);
            workflowBean.createWorkflowTransition(w2StateRecievedRef, w2StateDeniedRef);

            workflowBean.createWorkflowTransition(w2StateAccessRef, w2StateExecuteRef);

            workflowBean.createWorkflowTransition(w2StateExecuteRef, w2StateAcceptedRef);
            workflowBean.createWorkflowTransition(w2StateExecuteRef, w2StateDeniedRef);
        }
        return workFlowRef2;
    }

    public static NodeRef setupWorkflow3(WorkflowBean workflowBean) throws Exception {
        if (workFlowRef3 == null) {
            workFlowRef3 = workflowBean.addNewWorkflow(UNUSED_WORKFLOW, UNUSED_WORKFLOW + TITLE_POSTFIX);
            w3StateRecievedRef = workflowBean.addNewWorkflowState(workFlowRef3, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME + TITLE_POSTFIX, null);
            w3StateAccessRef = workflowBean.addNewWorkflowState(workFlowRef3, STATE_ASSESS_NAME, STATE_ASSESS_NAME + TITLE_POSTFIX, null);
            w3StateExecuteRef = workflowBean.addNewWorkflowState(workFlowRef3, STATE_EXECUTE_NAME, STATE_EXECUTE_NAME + TITLE_POSTFIX, StateCategory.NOMINATED);
            w3StateAcceptedRef = workflowBean.addNewWorkflowState(workFlowRef3, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME + TITLE_POSTFIX, StateCategory.CLOSED);
            workflowBean.setWorkflowEntryPoint(workFlowRef3, w3StateRecievedRef);

            workflowBean.createWorkflowTransition(w3StateRecievedRef, w3StateAccessRef);
            workflowBean.createWorkflowTransition(w3StateAccessRef, w3StateExecuteRef);
            workflowBean.createWorkflowTransition(w3StateExecuteRef, w3StateAcceptedRef);
        }
        return workFlowRef3;
    }

    static NodeRef setupBudget1(BudgetBean budgetBean) throws Exception {
        if (budgetRef1 == null) {
            budgetRef1 = budgetBean.addNewBudget(setupBudgetYear1(budgetBean), BUDGET1_NAME, BUDGET1_NAME + TITLE_POSTFIX, BUDGET1_AMOUNT);
        }
        return budgetRef1;
    }

    static NodeRef setupBudget2(BudgetBean budgetBean) throws Exception {
        if (budgetRef2 == null) {
            budgetRef2 = budgetBean.addNewBudget(setupBudgetYear1(budgetBean), BUDGET2_NAME, BUDGET2_NAME + TITLE_POSTFIX, BUDGET2_AMOUNT);
        }
        return budgetRef2;
    }

    static NodeRef setupBudget3(BudgetBean budgetBean) throws Exception {
        if (budgetRef3 == null) {
            budgetRef3 = budgetBean.addNewBudget(setupBudgetYear1(budgetBean), BUDGET3_NAME, BUDGET3_NAME + TITLE_POSTFIX, BUDGET3_AMOUNT);
        }
        return budgetRef3;
    }

    static NodeRef setupBudget4(BudgetBean budgetBean) throws Exception {
        if (budgetRef4 == null) {
            budgetRef4 = budgetBean.addNewBudget(setupBudgetYear2(budgetBean), BUDGET4_NAME, BUDGET4_NAME + TITLE_POSTFIX, BUDGET4_AMOUNT);
        }
        return budgetRef4;
    }

    static NodeRef setupBudget5(BudgetBean budgetBean) throws Exception {
        if (budgetRef5 == null) {
            budgetRef5 = budgetBean.addNewBudget(setupBudgetYear2(budgetBean), BUDGET5_NAME, BUDGET5_NAME + TITLE_POSTFIX, BUDGET5_AMOUNT);
        }
        return budgetRef5;
    }

    static NodeRef setupBudgetYear1(BudgetBean budgetBean) throws Exception {
        if (budgetYearRef1 == null) {
            budgetYearRef1 = budgetBean.addNewBudgetYear(BUDGETYEAR1_NAME, BUDGETYEAR1_NAME + TITLE_POSTFIX, START_DATE, END_DATE);
        }
        return budgetYearRef1;
    }
    
    static NodeRef setupBudgetYear2(BudgetBean budgetBean) throws Exception {
        if (budgetYearRef2 == null) {
            budgetYearRef2 = budgetBean.addNewBudgetYear(BUDGETYEAR2_NAME, BUDGETYEAR2_NAME + TITLE_POSTFIX, END_DATE, NEXT_YEAR_END_DATE);
        }
        return budgetYearRef2;
    }


    private static NodeRef createUser(String username, ServiceRegistry serviceRegistry){
        String password = "testpass";
        serviceRegistry.getAuthenticationService().createAuthentication(username, password.toCharArray());

        Map user = new HashMap();
        user.put(ContentModel.PROP_USERNAME, username);
        user.put(ContentModel.PROP_FIRSTNAME, username);
        user.put(ContentModel.PROP_LASTNAME, "Testuser");
        user.put(ContentModel.PROP_EMAIL, username + "@example.com");
        user.put(ContentModel.PROP_JOBTITLE, "Peon½");

        return serviceRegistry.getPersonService().createPerson(user);
    }
}
