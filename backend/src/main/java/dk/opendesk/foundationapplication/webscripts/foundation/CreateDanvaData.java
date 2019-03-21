/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.enums.StateCategory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
        
        getBranchBean().addBranchWorkflow(central, centralWorkflow);
        
        NodeRef app1 = createApplication(premeeting1, null, central, "Ansøgning 1", 60000);
        NodeRef app2 = createApplication(premeeting1, null, central, "Ansøgning 2", 120000);
    }
    
    @Override
    public Application buildApplication(NodeRef state, NodeRef budget, NodeRef branch, String name, long requiredAmount) {
        String recipient = random(COMPANYNAMES);
        String partner1 = random(COMPANYNAMES);
        String partner2 = random(COMPANYNAMES);
        String firstName = random(FIRSTNAMES);
        String lastName = random(LASTNAMES);
        String steetName = random(STREETNAMES);
        String floor = random(FLOORS);
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now().plus(Duration.ofDays(RANDOM.nextInt(50) + 1)));
        List<ApplicationFieldValue> fields;
        ApplicationBlock applicant = new ApplicationBlock();
        applicant.setId("applicant");
        applicant.setLabel("Oplysninger om ansøger");
        applicant.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("1", "Hovedansøger", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,recipient));
        fields.add(buildValue("2", "Adresse", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,steetName));
        fields.add(buildValue("3", "Postnummer", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,numberString(4)));
        fields.add(buildValue("4", "By", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,floor));
        fields.add(buildValue("5", "CVR", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,numberString(8)));
        fields.add(buildValue("6a", "Projektleder fornavn", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,firstName));
        fields.add(buildValue("6b", "Projektleder efternavn", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lastName));
        fields.add(buildValue("7", "Projektleder email", "display:block;", "text", String.class, Functional.email_to(),  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,firstName + "@testmail.dk"));
        fields.add(buildValue("8", "Projektleder telefon", "display:block;", "text", String.class, Functional.phone_number(),  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,phoneNumber()));
        applicant.setFields(fields);
        
        
        ApplicationBlock projektPartner1 = new ApplicationBlock();
        projektPartner1.setId("pp1");
        projektPartner1.setLabel(partner1);
        projektPartner1.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("9", "Viksomhedens navn", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,partner1));
        fields.add(buildValue("10", "CVR nummer", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,numberString(8)));
        fields.add(buildValue("11", "Kontaktperson", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,random(FIRSTNAMES)+ "" +random(LASTNAMES)));
        fields.add(buildValue("12", "Rolle under projektet", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lorem(6)));
        projektPartner1.setFields(fields);
        
        ApplicationBlock projektPartner2 = new ApplicationBlock();
        projektPartner2.setId("pp2");
        projektPartner2.setLabel(partner2);
        projektPartner2.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("13", "Viksomhedens navn", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,partner2));
        fields.add(buildValue("14", "CVR nummer", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,numberString(8)));
        fields.add(buildValue("15", "Kontaktperson", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,random(FIRSTNAMES)+ "" +random(LASTNAMES)));
        fields.add(buildValue("16", "Rolle under projektet", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lorem(6)));
        projektPartner2.setFields(fields);
        
        ApplicationBlock project = new ApplicationBlock();
        project.setId("projekt");
        project.setLabel("Projektet");
        project.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("17", "Titel", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,name));
        fields.add(buildValue("18", "Kategori", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Spildevand"));
        fields.add(buildValue("19", "Kategori", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Klimatilpasning"));
        fields.add(buildValue("20", "Beskrivelse", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,LOREM));
        fields.add(buildValue("21", "Hvorfor dette projekt?", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,LOREM));
        fields.add(buildValue("22", "Output fra projektet", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,LOREM));
        fields.add(buildValue("23", "Nyhedsværdi", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lorem(140)));
        fields.add(buildValue("24", "Nytteværdi", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lorem(70)));
        fields.add(buildValue("25", "Effektivisering og bæredygtighed", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,lorem(140)));
        project.setFields(fields);
        
        ApplicationBlock dateBlock = new ApplicationBlock();
        dateBlock.setId("datesbudget");
        dateBlock.setLabel("Dato og finansiering");
        dateBlock.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("26", "Startdato", "display:block;", "datepicker", Date.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,startDate));
        fields.add(buildValue("27", "Slutdato", "display:block;", "datepicker", Date.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,endDate));
        fields.add(buildValue("28", "Budgetsum", "display:block;", "number", Double.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,Double.valueOf(requiredAmount*2)));
        fields.add(buildValue("29", "Ansøgt beløb", "display:block;", "number", Double.class, Functional.amount(),  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,Double.valueOf(requiredAmount)));
        dateBlock.setFields(fields);   
                
        ApplicationBlock contact = new ApplicationBlock();
        contact.setId("contact");
        contact.setLabel("Ansvarlige personer hos hovedansøger");
        contact.setLayout("display:block;");
        fields = new ArrayList<>();
        fields.add(buildValue("30", "Projektleders/kontaktpersons navn", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,random(FIRSTNAMES)+ " "+random(LASTNAMES)));
        fields.add(buildValue("31", "Projektleders/kontaktpersons stilling", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Direktør"));
        fields.add(buildValue("32", "Økonomiske/juridiske ansvarliges navn", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,random(FIRSTNAMES)+ " "+random(LASTNAMES)));
        fields.add(buildValue("33", "Økonomiske/juridiske ansvarliges stilling", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Bogholder"));
        contact.setFields(fields);   

        
        
        

        Application app = new Application();
        app.setTitle(name);
        List<ApplicationBlock> containers = Arrays.asList(new ApplicationBlock[]{applicant, projektPartner1, projektPartner2, project, dateBlock, contact});
        app.setBlocks(containers);
        if (branch != null) {
            BranchSummary branchRef = new BranchSummary();
            branchRef.parseRef(branch);
            app.setBranchSummary(branchRef);
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
    
    
    
}
