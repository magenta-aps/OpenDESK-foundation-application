/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class ResetDemoData extends JacksonBackedWebscript {
    public static final String BUDGETYEAR1_TITLE = "BudgetYearCurrent";
    public static final String BUDGETYEAR2_TITLE = "BudgetYearNext";
    private final List<String> companyNames = Arrays.asList(new String[]{"Fuglevennerne", "Fluefiskerforeningen", "Natteravnene", "Lones Kattehjem", "Fies Kattehjem", "Peters Kattehjem"});
    private final List<String> firstNames = Arrays.asList(new String[]{"Anders", "Anne", "Bjarne", "Børge", "Belinda", "Charlotte", "Casper", "Dorthe", "Mikkel", "Martin", "Mads", "Maja"});
    private final List<String> lastNames = Arrays.asList(new String[]{"Andersen", "Brandshøj", "Carlsen", "Svendsen", "Pedersen", "Sørensen", "Nielsen", "Fisker", "Smed"});
    private final List<String> streetName = Arrays.asList(new String[]{"Nørregade", "Søndergade", "Østergade", "Vestergade"});
    private final List<String> floors = Arrays.asList(new String[]{"", "1th", "1tv", "2tv", "10th"});
    private final String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce vitae iaculis mi. "
            + "Aenean enim lorem, fringilla eget convallis et, euismod eget nisi. Sed consectetur magna nisl, id congue orci tincidunt et. "
            + "Quisque id hendrerit lectus. Nullam porttitor massa nec enim rhoncus gravida. Aenean finibus quis augue id placerat. "
            + "Fusce enim nibh, elementum non viverra non, convallis id est. Morbi nunc leo, eleifend eu eleifend non, vehicula ac libero. "
            + "Donec justo sapien, convallis vitae erat et, pulvinar elementum velit.";

    private Random random = new Random();
    private FoundationBean foundationBean;
    private ServiceRegistry serviceRegistry;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected JSONObject doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        wipeData(serviceRegistry);
        
        createData();
        
        
        return new JSONObject().append("status", "ok");
    }
    
   
    
    public void createData() throws Exception{
        //Create branches
        NodeRef central = createBranch("Central");      
        NodeRef local1 = createBranch("København");       
        NodeRef local2 = createBranch("Randers");
        NodeRef local3 = createBranch("Aarhus");
        
        //Create budgetYears
        Instant date1 = Instant.now().minus(Duration.ofDays(1));
        Instant date2 = date1.plus(1, ChronoUnit.YEARS);
        Instant date3 = date2.plus(2, ChronoUnit.YEARS);
        NodeRef budgetYearCurrent1 = createBudgetYear(BUDGETYEAR1_TITLE, Date.from(date1), Date.from(date2));
        NodeRef budgetYearCurrent2 = createBudgetYear(BUDGETYEAR2_TITLE, Date.from(date2), Date.from(date3));
        
        //Create budgets
        NodeRef budgetCentral = createBudget(budgetYearCurrent1, "Central", 5000000l);
        NodeRef budgetLocal1 = createBudget(budgetYearCurrent1, "København", 70000l);
        NodeRef budgetLocal2 = createBudget(budgetYearCurrent1, "Randers", 30000l);
        NodeRef budgetLocal3 = createBudget(budgetYearCurrent1, "Aarhus", 50000l);
        NodeRef budgetSharedTotal = createBudget(budgetYearCurrent1, "Lokalprojekter", 100000l);
        NodeRef budgetSharedJutland = createBudget(budgetYearCurrent1, "Lokalprojekter Jylland", 50000l);
        
        
        NodeRef budgetNextYear = createBudget(budgetYearCurrent2, "Central", 5500000l);
        
        foundationBean.addBranchBudget(central, budgetCentral);
        foundationBean.addBranchBudget(local1, budgetLocal1);
        foundationBean.addBranchBudget(local2, budgetLocal2);
        foundationBean.addBranchBudget(local3, budgetLocal3);
        foundationBean.addBranchBudget(local2, budgetSharedJutland);
        foundationBean.addBranchBudget(local3, budgetSharedJutland);
        foundationBean.addBranchBudget(local1, budgetSharedTotal);
        foundationBean.addBranchBudget(local2, budgetSharedTotal);
        foundationBean.addBranchBudget(local3, budgetSharedTotal); 
        
        //Create Workflows
        NodeRef centralWorkflow = createWorkflow("Central");
        NodeRef localWorkflow = createWorkflow("Lokal");
        NodeRef unusedWorkflow = createWorkflow("Unused");
        
        NodeRef cHandleApplication = createWorkflowState("Gennemgå Ansøgning", centralWorkflow, true);
        NodeRef cMeeting = createWorkflowState("Bestyrelsesmøde", centralWorkflow, false);
        NodeRef cContactApplicant = createWorkflowState("Ansøgerkontakt", centralWorkflow, false);
        NodeRef cProjectRoom = createWorkflowState("Projektafvikling", centralWorkflow, false);
        NodeRef cProjectClosed = createWorkflowState("Projekt lukket", centralWorkflow, false);        
        NodeRef cProjectRejected = createWorkflowState("Afvist", centralWorkflow, false);
        
        createWorkflowStateTransitions(cHandleApplication, cMeeting, cProjectRejected);
        createWorkflowStateTransitions(cMeeting, cContactApplicant, cProjectRejected);
        createWorkflowStateTransitions(cContactApplicant, cProjectRoom);
        createWorkflowStateTransitions(cProjectRoom, cProjectClosed);
        
        NodeRef lReview = createWorkflowState("Sekretær/Direktør gennemgang", localWorkflow, true);
        NodeRef lMeeting = createWorkflowState("Møde Bankråd", localWorkflow, false);
        NodeRef lCSReview = createWorkflowState("Gennemgå ansøgning", localWorkflow, false);
        NodeRef lPayout = createWorkflowState("Udbetaling", localWorkflow, false);
        NodeRef lClosed = createWorkflowState("Afsluttet", localWorkflow, false);
        NodeRef lRejected = createWorkflowState("Afvist", localWorkflow, false);
        
        createWorkflowStateTransitions(lReview, lMeeting, lRejected);
        createWorkflowStateTransitions(lMeeting, lCSReview, lRejected);
        createWorkflowStateTransitions(lCSReview, lPayout);
        createWorkflowStateTransitions(lPayout, lClosed);
        
        foundationBean.addBranchWorkflow(central, centralWorkflow);
        foundationBean.addBranchWorkflow(local1, localWorkflow);
        foundationBean.addBranchWorkflow(local2, localWorkflow);
        foundationBean.addBranchWorkflow(local3, localWorkflow);
        
        //Create Applications
        NodeRef appc1 = createApplication(cHandleApplication, budgetCentral, central, "Ansøgning central 1", 60000);
        NodeRef appc2 = createApplication(cContactApplicant, budgetCentral, central, "Ansøgning central 2", 50000);
        NodeRef appc3 = createApplication(cMeeting, budgetCentral, central, "Ansøgning central 3", 100000);
        
        NodeRef appl1 = createApplication(lReview, budgetLocal1, local1, "Ansøgning KBH 1", 5000);
        NodeRef appl2 = createApplication(lMeeting, budgetLocal1, local1, "Ansøgning KBH 2", 10000);
        
        NodeRef appl3 = createApplication(lReview, budgetLocal2, local2, "Ansøgning Randers 1", 3000);
        NodeRef appl4 = createApplication(lRejected, budgetLocal2, local2, "Ansøgning Randers 2", 40000);
        NodeRef appl5 = createApplication(lRejected, budgetLocal2, local2, "Ansøgning Randers 3", 20000);
        
        NodeRef appl6 = createApplication(lReview, budgetLocal3, local3, "Ansøgning Aarhus 1", 10000);
        NodeRef appl7 = createApplication(lMeeting, budgetLocal3, local3, "Ansøgning Aarhus 2", 7000);
        NodeRef appl8 = createApplication(lPayout, budgetLocal3, local3, "Ansøgning Aarhus 3", 6000);
        NodeRef appl9 = createApplication(lClosed, budgetLocal3, local3, "Ansøgning Aarhus 4", 4000);
        
        NodeRef appnone1 = createApplication(null, null, null, "Ansøgning Ny 1", 2000);
        NodeRef appnone2 = createApplication(null, budgetSharedTotal, null, "Ansøgning Ny 2", 100000);
        NodeRef appnone3 = createApplication(null, budgetSharedJutland, null, "Ansøgning Ny 3", 5000);
        NodeRef appnone4 = createApplication(null, budgetSharedJutland, null, "Ansøgning Ny 4", 25000);
        
    }
    
    public NodeRef createApplication(NodeRef state, NodeRef budget, NodeRef branch, String name, long requiredAmont) throws Exception{
        String recipient = random(companyNames);
        String firstName = random(firstNames);
        String lastName = random(lastNames);
        String steetName = random(streetName);
        String floor = random(floors);
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now().plus(Duration.ofDays(random.nextInt(50)+1)));
        NodeRef app =  foundationBean.addNewApplication(branch, budget, "TestApplication-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name, "Category1", recipient, steetName, random.nextInt(40), floor, numberString(4), firstName, lastName, firstName+"@mail.dk", phoneNumber(), lorem, startDate, endDate, requiredAmont, numberString(4), "000"+numberString(5));
        
        if(state != null){
            foundationBean.setApplicationState(app, state);
        }
        return app;
    }
    
    public NodeRef createBranch(String name) throws Exception{
        return foundationBean.addNewBranch("TestBranch-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name);
    }
    
    public NodeRef createBudgetYear(String name, Date startDate, Date endDate) throws Exception{
        return foundationBean.addNewBudgetYear(name, "TestBudgetYear"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), startDate, endDate);
    }
    
    public NodeRef createBudget(NodeRef budgetYear, String name, Long amount) throws Exception{
        return foundationBean.addNewBudget(budgetYear, "TestBudget-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name, amount);
    }
    
    public NodeRef createWorkflow(String name) throws Exception{
        return foundationBean.addNewWorkflow("TestWorkflow-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name);
    }
    
    public NodeRef createWorkflowState(String name, NodeRef workflowRef, boolean isEntry) throws Exception{
        NodeRef stateRef = foundationBean.addNewWorkflowState(workflowRef, "TestState-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name);
        if(isEntry){
            foundationBean.setWorkflowEntryPoint(workflowRef, stateRef);
        }
        return stateRef;
    }
    
    public void createWorkflowStateTransitions(NodeRef from, NodeRef... to) throws Exception{
        for(NodeRef toRef : to){
            foundationBean.createWorkflowTransition(from, toRef);
        }
    } 
    
    

    public static void wipeData(ServiceRegistry serviceRegistry) throws Exception {
        NodeService nodeService = serviceRegistry.getNodeService();

        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);
        NodeRef dataRef = foundationBean.getDataHome();

        for (NodeRef workflow : foundationBean.getWorkflows()) {
            nodeService.removeChild(dataRef, workflow);
        }

        for (NodeRef budget : foundationBean.getBudgetYearRefs()) {
            nodeService.removeChild(dataRef, budget);
        }

        for (NodeRef branch : foundationBean.getBranches()) {
            nodeService.removeChild(dataRef, branch);
        }

        for (ApplicationSummary application : foundationBean.getApplicationSummaries()) {
            nodeService.removeChild(dataRef, application.asNodeRef());
        }
    }
    
    
    public <T> T random(List<T> collection){
        return collection.get(random.nextInt(collection.size()));
    }
    
    
    public String phoneNumber(){
        StringBuilder pn = new StringBuilder();
        if(random.nextInt(10)>8){
            pn.append("+").append(random.nextInt(9)).append(random.nextInt(9));
        }
        pn.append(numberString(8));
        return pn.toString();
    }
    
    public String numberString(int count){
        StringBuilder number = new StringBuilder();
        
        
        for(int i = 0 ; i<count ; i++){
            number.append(random.nextInt(9));
        }
        return number.toString();
    }

}
