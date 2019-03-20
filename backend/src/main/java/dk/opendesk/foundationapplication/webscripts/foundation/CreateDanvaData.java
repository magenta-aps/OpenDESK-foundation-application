/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.enums.StateCategory;
import static dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData.buildValue;
import static dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData.numberString;
import static dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData.phoneNumber;
import static dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData.random;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 *
 * @author martin
 */
public class CreateDanvaData extends ResetDemoData {

    @Override
    public void createData() throws Exception {
        NodeRef central = createBranch("Central");
        
        NodeRef centralWorkflow = createWorkflow("Central");
        
        NodeRef premeeting1 = createWorkflowState("Udvælg bedømmelser", centralWorkflow, true, null);
        NodeRef meeting1 = createWorkflowState("Bestyrelsesmøde 1", centralWorkflow, false, null);
        NodeRef expanded = createWorkflowState("Udvidet ansøgning", centralWorkflow, false, StateCategory.NOMINATED);
        NodeRef meeting2 = createWorkflowState("Møde2", centralWorkflow, false, StateCategory.ACCEPTED);
        NodeRef approved = createWorkflowState("Godkendt", centralWorkflow, false, StateCategory.CLOSED);        
        NodeRef rejected = createWorkflowState("Afvist", centralWorkflow, false, StateCategory.REJECTED);
        
        createWorkflowStateTransitions(premeeting1, meeting1, rejected);
        createWorkflowStateTransitions(meeting1, expanded, rejected);
        createWorkflowStateTransitions(expanded, meeting2);
        createWorkflowStateTransitions(meeting2, approved, rejected);
        
        getBranchBean().setBranchWorkflow(central, centralWorkflow);
        
        NodeRef app1 = createApplication(premeeting1, null, central, "Ansøgning 1", 60000);
        NodeRef app2 = createApplication(premeeting1, null, central, "Ansøgning 2", 120000);
    }
    
    @Override
    public Application buildApplication(NodeRef state, NodeRef budget, NodeRef branch, String name, long requiredAmount) throws Exception {
        String recipient = random(COMPANYNAMES);
        String partner1 = random(COMPANYNAMES);
        String partner2 = random(COMPANYNAMES);
        String firstName = random(FIRSTNAMES);
        String lastName = random(LASTNAMES);
        String steetName = random(STREETNAMES);
        String floor = random(FLOORS);
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now().plus(Duration.ofDays(RANDOM.nextInt(50) + 1)));
        List<ApplicationPropertyValue> fields;
        ApplicationPropertiesContainer applicant = new ApplicationPropertiesContainer();
        applicant.setId("applicant");
        applicant.setLabel("Oplysninger om ansøger");
        applicant.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("1", "Hovedansøger", "display:block;", "text", String.class, null, recipient));
        fields.add(buildValue("2", "Adresse", "display:block;", "text", String.class, null, steetName));
        fields.add(buildValue("3", "Postnummer", "display:block;", "text", String.class, null, numberString(4)));
        fields.add(buildValue("4", "By", "display:block;", "text", String.class, null, floor));
        fields.add(buildValue("5", "CVR", "display:block;", "text", String.class, null, numberString(8)));
        fields.add(buildValue("6a", "Projektleder fornavn", "display:block;", "text", String.class, null, firstName));
        fields.add(buildValue("6b", "Projektleder efternavn", "display:block;", "text", String.class, null, lastName));
        fields.add(buildValue("7", "Projektleder email", "display:block;", "text", String.class, Functional.email_to(), firstName + "@testmail.dk"));
        fields.add(buildValue("8", "Projektleder telefon", "display:block;", "text", String.class, Functional.phone_number(), phoneNumber()));
        applicant.setFields(fields);
        
        
        ApplicationPropertiesContainer projektPartner1 = new ApplicationPropertiesContainer();
        projektPartner1.setId("pp1");
        projektPartner1.setLabel(partner1);
        projektPartner1.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("9", "Viksomhedens navn", "display:block;", "text", String.class, null, partner1));
        fields.add(buildValue("10", "CVR nummer", "display:block;", "text", String.class, null, numberString(8)));
        fields.add(buildValue("11", "Kontaktperson", "display:block;", "text", String.class, null, random(FIRSTNAMES)+ "" +random(LASTNAMES)));
        fields.add(buildValue("12", "Rolle under projektet", "display:block;", "text", String.class, null, lorem(6)));
        projektPartner1.setFields(fields);
        
        ApplicationPropertiesContainer projektPartner2 = new ApplicationPropertiesContainer();
        projektPartner2.setId("pp2");
        projektPartner2.setLabel(partner2);
        projektPartner2.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("13", "Viksomhedens navn", "display:block;", "text", String.class, null, partner2));
        fields.add(buildValue("14", "CVR nummer", "display:block;", "text", String.class, null, numberString(8)));
        fields.add(buildValue("15", "Kontaktperson", "display:block;", "text", String.class, null, random(FIRSTNAMES)+ "" +random(LASTNAMES)));
        fields.add(buildValue("16", "Rolle under projektet", "display:block;", "text", String.class, null, lorem(6)));
        projektPartner2.setFields(fields);
        
        ApplicationPropertiesContainer project = new ApplicationPropertiesContainer();
        project.setId("projekt");
        project.setLabel("Projektet");
        project.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("17", "Titel", "display:block;", "text", String.class, null, name));
        fields.add(buildValue("18", "Kategori", "display:block;", "text", String.class, null, "Spildevand"));
        fields.add(buildValue("19", "Kategori", "display:block;", "text", String.class, null, "Klimatilpasning"));
        fields.add(buildValue("20", "Beskrivelse", "display:block;", "text", String.class, null, LOREM));
        fields.add(buildValue("21", "Hvorfor dette projekt?", "display:block;", "text", String.class, null, LOREM));
        fields.add(buildValue("22", "Output fra projektet", "display:block;", "text", String.class, null, LOREM));
        fields.add(buildValue("23", "Nyhedsværdi", "display:block;", "text", String.class, null, lorem(140)));
        fields.add(buildValue("24", "Nytteværdi", "display:block;", "text", String.class, null, lorem(70)));
        fields.add(buildValue("25", "Effektivisering og bæredygtighed", "display:block;", "text", String.class, null, lorem(140)));
        project.setFields(fields);
        
        ApplicationPropertiesContainer dateBlock = new ApplicationPropertiesContainer();
        dateBlock.setId("datesbudget");
        dateBlock.setLabel("Dato og finansiering");
        dateBlock.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("26", "Startdato", "display:block;", "datepicker", Date.class, null, startDate));
        fields.add(buildValue("27", "Slutdato", "display:block;", "datepicker", Date.class, null, endDate));
        fields.add(buildValue("28", "Budgetsum", "display:block;", "number", Double.class, null, Double.valueOf(requiredAmount*2)));
        fields.add(buildValue("29", "Ansøgt beløb", "display:block;", "number", Double.class, Functional.amount(), Double.valueOf(requiredAmount)));
        dateBlock.setFields(fields);   
                
        ApplicationPropertiesContainer contact = new ApplicationPropertiesContainer();
        contact.setId("contact");
        contact.setLabel("Ansvarlige personer hos hovedansøger");
        contact.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("30", "Projektleders/kontaktpersons navn", "display:block;", "text", String.class, null, random(FIRSTNAMES)+ " "+random(LASTNAMES)));
        fields.add(buildValue("31", "Projektleders/kontaktpersons stilling", "display:block;", "text", String.class, null, "Direktør"));
        fields.add(buildValue("32", "Økonomiske/juridiske ansvarliges navn", "display:block;", "text", String.class, null, random(FIRSTNAMES)+ " "+random(LASTNAMES)));
        fields.add(buildValue("33", "Økonomiske/juridiske ansvarliges stilling", "display:block;", "text", String.class, null, "Bogholder"));
        contact.setFields(fields);   

        
        
        

        Application app = new Application();
        app.setTitle(name);
        List<ApplicationPropertiesContainer> containers = Arrays.asList(new ApplicationPropertiesContainer[]{applicant, projektPartner1, projektPartner2, project, dateBlock, contact});
        app.setBlocks(containers);
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
    
    
    
}
