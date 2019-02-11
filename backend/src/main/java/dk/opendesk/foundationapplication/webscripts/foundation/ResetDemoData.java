/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.enums.StateCategory;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
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
    public static final AtomicInteger COUNTER = new AtomicInteger();
    public static final String BUDGETYEAR1_TITLE = "BudgetYearCurrent";
    public static final String BUDGETYEAR2_TITLE = "BudgetYearNext";
    private static final List<String> COMPANYNAMES = Arrays.asList(new String[]{"Fuglevennerne", "Fluefiskerforeningen", "Natteravnene", "Lones Kattehjem", "Fies Kattehjem", "Peters Kattehjem"});
    private static final List<String> FIRSTNAMES = Arrays.asList(new String[]{"Anders", "Anne", "Bjarne", "Børge", "Belinda", "Charlotte", "Casper", "Dorthe", "Mikkel", "Martin", "Mads", "Maja"});
    private static final List<String> LASTNAMES = Arrays.asList(new String[]{"Andersen", "Brandshøj", "Carlsen", "Svendsen", "Pedersen", "Sørensen", "Nielsen", "Fisker", "Smed"});
    private static final List<String> STREETNAMES = Arrays.asList(new String[]{"Nørregade", "Søndergade", "Østergade", "Vestergade"});
    private static final List<String> FLOORS = Arrays.asList(new String[]{"", "1th", "1tv", "2tv", "10th"});
    private static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce vitae iaculis mi. "
            + "Aenean enim lorem, fringilla eget convallis et, euismod eget nisi. Sed consectetur magna nisl, id congue orci tincidunt et. "
            + "Quisque id hendrerit lectus. Nullam porttitor massa nec enim rhoncus gravida. Aenean finibus quis augue id placerat. "
            + "Fusce enim nibh, elementum non viverra non, convallis id est. Morbi nunc leo, eleifend eu eleifend non, vehicula ac libero. "
            + "Donec justo sapien, convallis vitae erat et, pulvinar elementum velit.";

    private static final Random RANDOM = new Random();
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
        Instant date2 = date1.plus(300, ChronoUnit.DAYS);
        Instant date3 = date2.plus(600, ChronoUnit.DAYS);
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
        
        NodeRef cHandleApplication = createWorkflowState("Gennemgå Ansøgning", centralWorkflow, true, null);
        NodeRef cMeeting = createWorkflowState("Bestyrelsesmøde", centralWorkflow, false, null);
        NodeRef cContactApplicant = createWorkflowState("Ansøgerkontakt", centralWorkflow, false, StateCategory.NOMINATED);
        NodeRef cProjectRoom = createWorkflowState("Projektafvikling", centralWorkflow, false, StateCategory.ACCEPTED);
        NodeRef cProjectClosed = createWorkflowState("Projekt lukket", centralWorkflow, false, StateCategory.CLOSED);        
        NodeRef cProjectRejected = createWorkflowState("Afvist", centralWorkflow, false, StateCategory.REJECTED);
        
        createWorkflowStateTransitions(cHandleApplication, cMeeting, cProjectRejected);
        createWorkflowStateTransitions(cMeeting, cContactApplicant, cProjectRejected);
        createWorkflowStateTransitions(cContactApplicant, cProjectRoom);
        createWorkflowStateTransitions(cProjectRoom, cProjectClosed);
        
        NodeRef lReview = createWorkflowState("Sekretær/Direktør gennemgang", localWorkflow, true, null);
        NodeRef lMeeting = createWorkflowState("Møde Bankråd", localWorkflow, false, null);
        NodeRef lCSReview = createWorkflowState("Gennemgå ansøgning", localWorkflow, false, StateCategory.NOMINATED);
        NodeRef lPayout = createWorkflowState("Udbetaling", localWorkflow, false, StateCategory.ACCEPTED);
        NodeRef lClosed = createWorkflowState("Afsluttet", localWorkflow, false, StateCategory.CLOSED);
        NodeRef lRejected = createWorkflowState("Afvist", localWorkflow, false, StateCategory.REJECTED);
        
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
    
    
    
    public NodeRef createApplication(NodeRef state, NodeRef budget, NodeRef branch, String name, long requiredAmount) throws Exception{
        
        
        
        ApplicationReference app =  foundationBean.addNewApplication(buildApplication(state, budget, branch, name, requiredAmount));
        
//        if(state != null){
//            foundationBean.setApplicationState(app.asNodeRef(), state);
//        }
        return app.asNodeRef();
    }
    
    public static Application buildApplication(NodeRef state, NodeRef budget, NodeRef branch, String name, long requiredAmount) {
        String recipient = random(COMPANYNAMES);
        String firstName = random(FIRSTNAMES);
        String lastName = random(LASTNAMES);
        String steetName = random(STREETNAMES);
        String floor = random(FLOORS);
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now().plus(Duration.ofDays(RANDOM.nextInt(50) + 1)));
        ApplicationPropertiesContainer block1 = new ApplicationPropertiesContainer();
        block1.setId("block1");
        block1.setLabel("Information");
        block1.setLayout("display:block;");
        List<ApplicationPropertyValue> fields = new ArrayList<>();
        fields.add(buildValue("1", "Kategori", "display:block;", "text", String.class, null, "My new Category"));
        fields.add(buildValue("2", "Modtager", "display:block;", "text", String.class, null, recipient));
        fields.add(buildValue("3", "Vejnavn", "display:block;", "text", String.class, null, steetName));
        fields.add(buildValue("4", "Etage", "display:block;", "text", String.class, null, floor));
        fields.add(buildValue("5", "Postnr", "display:block;", "text", String.class, null, numberString(4)));
        fields.add(buildValue("6", "Fornavn", "display:block;", "text", String.class, null, firstName));
        fields.add(buildValue("7", "Efternavn", "display:block;", "text", String.class, null, lastName));
        fields.add(buildValue("8", "Email", "display:block;", "text", String.class, null, firstName + "@mail.dk"));
        fields.add(buildValue("9", "Telefonnummer", "display:block;", "text", String.class, null, phoneNumber()));
        fields.add(buildValue("10", "Kort beskrivelse", "display:block;", "text", String.class, null, LOREM));
        fields.add(buildValue("11", "Startdato", "display:block;", "text", Date.class, null, startDate));
        fields.add(buildValue("12", "EndDate", "display:block;", "text", Date.class, null, endDate));
        fields.add(buildValue("13", "Beløb", "display:block;", "text", Long.class, Functional.amount(), requiredAmount));
        fields.add(buildValue("14", "Registreringsnummer", "display:block;", "text", String.class, Functional.amount(), numberString(4)));
        fields.add(buildValue("15", "Kontonummer", "display:block;", "text", String.class, Functional.amount(), "000" + numberString(5)));

        block1.setFields(fields);

        Application app = new Application();
        app.setTitle(name);
        app.setBlocks(Collections.singletonList(block1));
        if (branch != null) {
            BranchReference branchRef = new BranchReference();
            branchRef.parseRef(branch);
            app.setBranchRef(branchRef);
        }
        if (budget != null) {
            BudgetReference budgetRef = new BudgetReference();
            budgetRef.parseRef(budget);
            app.setBudget(budgetRef);
        }
        if (state != null) {
            StateReference stateRef = new StateReference();
            stateRef.parseRef(state);
            app.setState(stateRef);
        }

        return app;
    }
    
    public static <E> ApplicationPropertyValue<E> buildValue(String id, String label, String layout, String type, Class<E> javaType, Functional function, E value){
        ApplicationPropertyValue valueField = new ApplicationPropertyValue();
        valueField.setId(id);
        valueField.setLabel(label);
        valueField.setLayout(layout);
        valueField.setType(type);
        valueField.setJavaType(javaType);
        if(function != null){
            valueField.setFunction(function.getFriendlyName());
        }
        valueField.setValue(value);
        
        return valueField;
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
    
    public NodeRef createWorkflowState(String name, NodeRef workflowRef, boolean isEntry, StateCategory category) throws Exception{
        NodeRef stateRef = foundationBean.addNewWorkflowState(workflowRef, "TestState-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name, category);
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
    
    
    public static <T> T random(List<T> collection){
        return collection.get(RANDOM.nextInt(collection.size()));
    }
    
    
    public static String phoneNumber(){
        StringBuilder pn = new StringBuilder();
        if(RANDOM.nextInt(10)>8){
            pn.append("+").append(RANDOM.nextInt(9)).append(RANDOM.nextInt(9));
        }
        pn.append(numberString(8));
        return pn.toString();
    }
    
    public static String numberString(int count){
        StringBuilder number = new StringBuilder();
        
        
        for(int i = 0 ; i<count ; i++){
            number.append(RANDOM.nextInt(9));
        }
        return number.toString();
    }

}
