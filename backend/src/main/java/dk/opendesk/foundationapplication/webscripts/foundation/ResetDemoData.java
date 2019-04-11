/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.ListBuilder;
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
import org.alfresco.service.cmr.repository.NodeRef;
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
    public static final List<String> COMPANYNAMES = Arrays.asList(new String[]{"Fuglevennerne", "Fluefiskerforeningen", "Natteravnene", "Lones Kattehjem", "Fies Kattehjem", "Peters Kattehjem"});
    public static final List<String> FIRSTNAMES = Arrays.asList(new String[]{"Anders", "Anne", "Bjarne", "Børge", "Belinda", "Charlotte", "Casper", "Dorthe", "Mikkel", "Martin", "Mads", "Maja"});
    public static final List<String> LASTNAMES = Arrays.asList(new String[]{"Andersen", "Brandshøj", "Carlsen", "Svendsen", "Pedersen", "Sørensen", "Nielsen", "Fisker", "Smed"});
    public static final List<String> STREETNAMES = Arrays.asList(new String[]{"Nørregade", "Søndergade", "Østergade", "Vestergade"});
    public static final List<String> FLOORS = Arrays.asList(new String[]{"", "1th", "1tv", "2tv", "10th"});
    public static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam efficitur nunc quis lectus venenatis condimentum."
            + " Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Aenean lobortis massa ac magna vestibulum lobortis. "
            + "Nulla ante enim, tempor ut gravida quis, iaculis eget nibh. Aenean in eros posuere, tempus nibh nec, malesuada erat. Pellentesque ullamcorper tellus id efficitur elementum. "
            + "Praesent a sollicitudin mauris. Vivamus enim lorem, cursus vestibulum magna id, vehicula cursus augue. Ut ullamcorper posuere odio a tincidunt. "
            + "Curabitur vestibulum nunc sed elementum tempor. Integer non purus purus. Pellentesque pharetra mi accumsan metus eleifend, vel semper dui efficitur. "
            + "Ut at placerat mi. Integer hendrerit viverra maximus.\n" 
            + "Sed at gravida erat, ut suscipit eros. Nullam porta id mi vel mattis. Pellentesque venenatis cursus erat vel efficitur. "
            + "Mauris fermentum, nibh nec vestibulum sollicitudin, enim dui semper dolor, nec efficitur nisi nisi nec ligula. Ut eu iaculis ipsum. In ornare turpis. ";

    public static final Random RANDOM = new Random();

    @Override
    protected JSONObject doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        if(getBranchBean().getBranchSummaries().size() > 0){
            throw new RuntimeException("Workflows are already initialized");
        }
        
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
        
        getBranchBean().addBranchBudget(central, budgetCentral);
        getBranchBean().addBranchBudget(local1, budgetLocal1);
        getBranchBean().addBranchBudget(local2, budgetLocal2);
        getBranchBean().addBranchBudget(local3, budgetLocal3);
        getBranchBean().addBranchBudget(local2, budgetSharedJutland);
        getBranchBean().addBranchBudget(local3, budgetSharedJutland);
        getBranchBean().addBranchBudget(local1, budgetSharedTotal);
        getBranchBean().addBranchBudget(local2, budgetSharedTotal);
        getBranchBean().addBranchBudget(local3, budgetSharedTotal); 
        
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
        
        getBranchBean().setBranchWorkflow(central, centralWorkflow);
        getBranchBean().setBranchWorkflow(local1, localWorkflow);
        getBranchBean().setBranchWorkflow(local2, localWorkflow);
        getBranchBean().setBranchWorkflow(local3, localWorkflow);
        
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
        
        
        
        ApplicationReference app =  getApplicationBean().addNewApplication(buildApplication(state, budget, branch, name, requiredAmount));
        
//        if(state != null){
//            foundationBean.setApplicationState(app.asNodeRef(), state);
//        }
        return app.asNodeRef();
    }
    
    public Application buildApplication(NodeRef state, NodeRef budget, NodeRef branch, String name, long requiredAmount) throws Exception {
        String recipient = random(COMPANYNAMES);
        String firstName = random(FIRSTNAMES);
        String lastName = random(LASTNAMES);
        String steetName = random(STREETNAMES);
        String floor = random(FLOORS);
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now().plus(Duration.ofDays(RANDOM.nextInt(50) + 1)));
        ApplicationBlock block1 = new ApplicationBlock();
        block1.setId("block1");
        block1.setLabel("Information");
        block1.setLayout("display:block;");
        List<ApplicationFieldValue> fields = new ArrayList<>();
        fields.add(buildValue("1", "Kategori", "display:block;", "text", String.class, null, null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"My new Category"));
        fields.add(buildValue("2", "Modtager", "display:block;", "text", String.class, null, null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, recipient));
        fields.add(buildValue("3", "Vejnavn", "display:block;", "text", String.class, null, null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, steetName));
        fields.add(buildValue("4", "Etage", "display:block;", "text", String.class, null,null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, floor));
        fields.add(buildValue("5", "Postnr", "display:block;", "text", String.class, null,null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, numberString(4)));
        fields.add(buildValue("6", "Fornavn", "display:block;", "text", String.class, Functional.first_name(),null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, firstName));
        fields.add(buildValue("7", "Efternavn", "display:block;", "text", String.class, Functional.last_name(),null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, lastName));
        fields.add(buildValue("8", "Email", "display:block;", "text", String.class, Functional.email_to(), null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, firstName + "@mail.dk"));
        fields.add(buildValue("9", "Telefonnummer", "display:block;", "text", String.class, Functional.phone_number(), null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, phoneNumber()));
        fields.add(buildValue("10", "Kort beskrivelse", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lorem(50)));
        fields.add(buildValue("11", "Startdato", "display:block;", "text", Date.class, null, null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, startDate));
        fields.add(buildValue("12", "EndDate", "display:block;", "text", Date.class, null, null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, endDate));
        fields.add(buildValue("13", "Beløb", "display:block;", "text", Long.class, Functional.amount(), null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, requiredAmount));
        fields.add(buildValue("14", "Registreringsnummer", "display:block;", "text", String.class, null, null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true, numberString(4)));
        fields.add(buildValue("15", "Kontonummer", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"000" + numberString(5)));

        block1.setFields(fields);

        Application app = new Application();
        app.setTitle(name);
        app.setBlocks(Collections.singletonList(block1));
        if (branch != null) {
            BranchSummary branchRef = getBranchBean().getBranchSummary(branch);
            app.setBranchSummary(branchRef);
        }
        if (budget != null) {
            BudgetReference budgetRef = getBudgetBean().getBudgetReference(budget);
            app.setBudget(budgetRef);
        }
        if (state != null) {
            StateReference stateRef = getWorkflowBean().getStateReference(state);
            app.setState(stateRef);
        }

        return app;
    }
    
    public static <E> ApplicationFieldValue<E> buildValue(
            String id,
            String label,
            String layout,
            String component,
            Class<E> type,
            Functional function,
            List<E> allowedValues,
            String hint,
            String wrapper,
            String validation,
            String permission,
            Boolean readOnly,
            ArrayList<E> value
    ) throws ClassNotFoundException{
        ApplicationFieldValue valueField = new ApplicationFieldValue();
        valueField.setId(id);
        valueField.setLabel(label);
        valueField.setLayout(layout);
        valueField.setComponent(component);
        if(function != null){
            valueField.setDescribes(function.getFriendlyName());
        }
        valueField.setType(type.getCanonicalName());
        valueField.setHint(hint);
        valueField.setWrapper(wrapper);
        valueField.setValidation(validation);
        valueField.setReadOnly(readOnly);
        valueField.setValue(value);
        
        return valueField;
    }
    
        
    public static <E> ApplicationFieldValue<E> buildValue(
            String id,
            String label,
            String layout,
            String component,
            Class<E> type,
            Functional function,
            List<E> allowedValues,
            String hint,
            String wrapper,
            String validation,
            String permission,
            Boolean readOnly,
            E value
    ) throws ClassNotFoundException{
        return buildValue(id, label, layout, component, type, function, allowedValues, hint, wrapper, validation, permission, readOnly, ListBuilder.listFrom(value));
    }
    
    public NodeRef createBranch(String name) throws Exception{
        return getBranchBean().addNewBranch("TestBranch-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name);
    }
    
    public NodeRef createBudgetYear(String name, Date startDate, Date endDate) throws Exception{
        return getBudgetBean().addNewBudgetYear(name, "TestBudgetYear"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), startDate, endDate);
    }
    
    public NodeRef createBudget(NodeRef budgetYear, String name, Long amount) throws Exception{
        return getBudgetBean().addNewBudget(budgetYear, "TestBudget-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name, amount);
    }
    
    public NodeRef createWorkflow(String name) throws Exception{
        return getWorkflowBean().addNewWorkflow("TestWorkflow-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name);
    }
    
    public NodeRef createWorkflowState(String name, NodeRef workflowRef, boolean isEntry, StateCategory category) throws Exception{
        NodeRef stateRef = getWorkflowBean().addNewWorkflowState(workflowRef, "TestState-"+DateTimeFormatter.ISO_INSTANT.format(Instant.now()), name, category);
        if(isEntry){
            getWorkflowBean().setWorkflowEntryPoint(workflowRef, stateRef);
        }
        return stateRef;
    }
    
    public void createWorkflowStateTransitions(NodeRef from, NodeRef... to) throws Exception{
        for(NodeRef toRef : to){
            getWorkflowBean().createWorkflowTransition(from, toRef);
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
    
    public static String lorem(int wordcount){
        int i = 0;
        int index = 0;
        while(i<wordcount && index < LOREM.length()){
            i++;
            int newIndex = LOREM.indexOf(" ", index+1);
            if(newIndex >= 0){
                index = newIndex;
            }else{
                return LOREM;
            }
            
        }
        return LOREM.substring(0, index);
    }

}
