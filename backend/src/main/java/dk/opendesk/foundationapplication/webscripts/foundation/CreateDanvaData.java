/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationField;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.enums.StateCategory;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.json.JsonObjectConverter;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import static dk.opendesk.foundationapplication.Utilities.ACTION_NAME_ADD_BLOCKS;
import static dk.opendesk.foundationapplication.Utilities.ACTION_NAME_DANVA_MODS;
import static dk.opendesk.foundationapplication.Utilities.ASPECT_ON_CREATE;
import static dk.opendesk.foundationapplication.actions.AddBlocksToApplicationAction.PARAM_BLOCKS;

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
        
        //NodeRef app1 = createApplication(premeeting1, null, central, "Ansøgning 1", 60000);
        //NodeRef app2 = createApplication(premeeting1, null, central, "Ansøgning 2", 120000);
        
        addCreateNewBlockAction(expanded);
        makeDanvaSpecificModifications(expanded);
    }


    private void addCreateNewBlockAction(NodeRef stateRef) throws Exception {

        // Block with info fields:

        ApplicationBlock infoBlock = new ApplicationBlock();
        infoBlock.setId("additional_info");
        infoBlock.setLabel("Eksta information");

        ApplicationFieldValue field2 = new ApplicationFieldValue();
        field2.setId("quality_improvement");
        field2.setLabel("Kvalitetsforbedring");
        field2.setHint("Beskriv hvordan projektet bidrager til kvalitetsforbedring af f.eks. drikkevand, spildevand, ressourcekvalitet, håndtering af differentieret kvalitet og tilhørende teknologi, kvalitet af processerne - fra strategi over planlægning til drift i forsyningen, upcycling og serviceteknologier, dokumentation af teknologi og procedurer, osv. Ansøger bør forholde sig til, om der skabes et nyt problem andetsteds ved at løse et, f.eks. om der kan opstå forhøjelse af uønskede stoffer i recipienten, når proceseffektiviteten forbedres i renseanlægget");

        ApplicationFieldValue field3 = new ApplicationFieldValue();
        field3.setId("environment_climate_potentiale");
        field3.setLabel("Miljø- og klimaforbedringspotentiale");
        field3.setHint("Beskriv projektets miljø- og klimaforbedringspotentiale f.eks. i udledning til recipienter, emissionsopgørelser, mindre forbrug af kemikalier osv. Ansøger bør forholde sig til, om der skabes afledte effekter ved at løse et problem et sted, f.eks. at der kan opstå forhøjelse af klimagasemissioner, når proceseffektiviteten forbedres i renseanlægget.");

        ApplicationFieldValue field4 = new ApplicationFieldValue();
        field4.setId("security_of_supply");
        field4.setLabel("Forsyningssikkerhed");
        field4.setHint("Beskriv om projektet tilfører bedre forsyningssikkerhed eller mindsker den.");

        ApplicationFieldValue field5 = new ApplicationFieldValue();
        field5.setId("implementation_after_project_end");
        field5.setLabel("Implementering efter projektafslutning");
        field5.setHint("Beskriv desuden hvad der sker efter projektet – hvordan implementeres projektresultaterne i branchen, markeds- og eksportmulighederne, eksportmulighederne f.eks. Hvordan sikres en opskalering til praktisk anvendelse i vandsektoren.");

        ApplicationFieldValue field6 = new ApplicationFieldValue();
        field6.setId("project_completion_risk");
        field6.setLabel("Risici for projektgennemførsel");
        field6.setHint("Beskriv risici for projektets gennemførelse og tekniske aspekter. Det kan f.eks. være på fordeling af mandskabsressourcer pga. arbejdspres, usikkerheder omkring processer og effekter af ændringer, tekniske usikkerheder eller tidsplansforskydninger.");

        ApplicationFieldValue field7 = new ApplicationFieldValue();
        field7.setId("presentation");
        field7.setLabel("Formidling");
        field7.setHint("Beskriv hvordan projektet vil blive formidlet. Beskriv strategien for formidling under og efter projektets afslutning");

        ApplicationFieldValue field8 = new ApplicationFieldValue();
        field8.setId("cooperation");
        field8.setLabel("Samarbejde");
        field8.setHint("Beskriv samarbejdet (grundlaget for samarbejde, valg af samarbejdspartnere, fordele ved samarbejde etc.)");


        List<ApplicationFieldValue> infoFields = Arrays.asList(field2,field3,field4,field5,field6,field7,field8);
        for (ApplicationFieldValue infoField : infoFields) {
            infoField.setType(String.class.getCanonicalName());
            infoField.setComponent("textarea");
            infoField.setLayout("block");
            infoField.setWrapper("block");
            infoField.setValidation("{\"max_words\":\"150\"}");
            infoField.setValue(null);
        }
        infoBlock.setFields(infoFields);


        // Block with file fields:

        ApplicationBlock fileBlock = new ApplicationBlock();
        fileBlock.setId("files");
        fileBlock.setLabel("Upload af bilag");

        ApplicationFieldValue headerField1 = new ApplicationFieldValue();
        headerField1.setId("header1");
        headerField1.setComponent("heading");
        headerField1.setSingleValue("Obligatoriske bilag");

        ApplicationFieldValue field9 = new ApplicationFieldValue();
        field9.setId("file_budget");
        field9.setLabel("Budget");
        field9.setHint(" – budgetskabelon anvendes");
        field9.setValidation("{\"required\":\"true\"}");
        field9.setComponent("file");

        ApplicationFieldValue field10 = new ApplicationFieldValue();
        field10.setId("file_organization_diagram");
        field10.setLabel("Organisationsdiagram for partnerne");
        field10.setValidation("{\"required\":\"true\"}");
        field10.setComponent("file");

        ApplicationFieldValue field11 = new ApplicationFieldValue();
        field11.setId("file_mini_cv");
        field11.setLabel("Mini CV’er for nøglepersoner");
        field11.setHint(" – en samlet pdf (max 2 sider pr. person)");
        field11.setValidation("{\"required\":\"true\"}");
        field11.setComponent("file");

        ApplicationFieldValue field13 = new ApplicationFieldValue();
        field13.setId("file_annual_rapport");
        field13.setLabel("Seneste årsrapport for alle partnere");
        field13.setValidation("{\"required\":\"true\"}");
        field13.setComponent("file");

        ApplicationFieldValue field14 = new ApplicationFieldValue();
        field14.setId("file_time_plan_diagram");
        field14.setLabel("Diagram for tidsplan");
        field14.setHint(" – GANTT, Excel eller lignende");
        field14.setValidation("{\"required\":\"true\"}");
        field14.setComponent("file");

        ApplicationFieldValue field15 = new ApplicationFieldValue();
        field15.setId("file_plan_of_presentation");
        field15.setLabel("Formidlingsplan");
        field15.setHint(" – hvilke medier og hvornår");
        field15.setValidation("{\"required\":\"true\"}");
        field15.setComponent("file");

        ApplicationFieldValue field16 = new ApplicationFieldValue();
        field16.setId("file_cooperation_statement");
        field16.setLabel("Samarbejdserklæring");
        field16.setValidation("{\"required\":\"true\"}");
        field16.setComponent("file");

        ApplicationFieldValue field17 = new ApplicationFieldValue();
        field17.setId("file_plan_for_implementation");
        field17.setLabel("Implementeringsplan");
        field17.setValidation("{\"required\":\"true\"}");
        field17.setComponent("file");

        ApplicationFieldValue headerField2 = new ApplicationFieldValue();
        headerField2.setId("header2");
        headerField2.setComponent("heading");
        headerField2.setSingleValue("Valgfrie bilag");

        ApplicationFieldValue field18 = new ApplicationFieldValue();
        field18.setId("file_statement_on_rights_and_patents");
        field18.setLabel("Erklæring om rettigheder/patenter");
        field18.setComponent("file");

        ApplicationFieldValue field19 = new ApplicationFieldValue();
        field19.setId("file_illustrations");
        field19.setLabel("Tegninger, illustrationer etc.");
        field19.setComponent("file");

        List<ApplicationFieldValue> fileFields = Arrays.asList(headerField1,field9,field10,field11,field13,field14,field15,field16,field17,headerField2,field18,field19);
        for (ApplicationFieldValue fileField : fileFields) {
            fileField.setType(String.class.getCanonicalName());
            fileField.setLayout("block");
            fileField.setWrapper("block");
        }
        fileBlock.setFields(fileFields);


        //adding the action

        List<ApplicationBlock> blocks = Arrays.asList(infoBlock, fileBlock);

        List<FoundationActionParameterValue> params = new ArrayList<>();
        FoundationActionParameterDefinition<String> blockParam = new FoundationActionParameterDefinition<>(PARAM_BLOCKS, DataTypeDefinition.ANY, String.class, false, null);
        params.add(new FoundationActionParameterValue<>(blockParam, Utilities.getMapper().writeValueAsString(blocks)));

        QName aspect = Utilities.getODFName(ASPECT_ON_CREATE);
        getActionBean().saveAction(ACTION_NAME_ADD_BLOCKS,stateRef,aspect,params);

    }

    private void makeDanvaSpecificModifications(NodeRef stateRef) throws Exception {
        Map<String, Serializable> params = new HashMap<>();
        QName aspect = Utilities.getODFName(ASPECT_ON_CREATE);
        getActionBean().saveAction(ACTION_NAME_DANVA_MODS, stateRef, aspect, params);

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
