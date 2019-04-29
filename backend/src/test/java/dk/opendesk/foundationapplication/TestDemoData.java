/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import com.benfante.jslideshare.App;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationField;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.StateSummary;
import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_PDF;


/**
 *
 * @author martin
 */
public class TestDemoData extends AbstractTestClass{

    public TestDemoData() {
        super("/foundation/demodata");
    }

    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(getServiceRegistry());
        TestUtils.setupSimpleFlow(getServiceRegistry());
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
    }
    
    public void testSetupDemoData() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"));
        
        assertEquals(4, getBranchBean().getBranchSummaries().size());
        assertEquals(2, getBudgetBean().getBudgetYearSummaries().size());
        for(BudgetYearSummary budgetYear : getBudgetBean().getBudgetYearSummaries()){
            if(budgetYear.getTitle().equals(ResetDemoData.BUDGETYEAR1_TITLE)){
                assertEquals(6, getBudgetBean().getBudgetSummaries(budgetYear));
            }else if (budgetYear.getTitle().equals(ResetDemoData.BUDGETYEAR2_TITLE)){
                assertEquals(0, getBudgetBean().getBudgetSummaries(budgetYear));
            }
        }
        
        assertEquals(3, getWorkflowBean().getWorkflowSummaries().size());
        assertEquals(16, getApplicationBean().getApplicationSummaries().size());
    }
    
    
    
    public void testSetupDemoDataDanva() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"), "danva");
        
        assertEquals(1, getBranchBean().getBranchSummaries().size());
        assertEquals(0, getBudgetBean().getBudgetYearSummaries().size());
        
        
        assertEquals(1, getWorkflowBean().getWorkflowSummaries().size());
        assertEquals(0, getApplicationBean().getApplicationSummaries().size());

    }
    
    
    public void testSetupDemoDataDanvaDataAlreadyExists() throws Exception {
        try{
            
        }catch(RuntimeException ex){
            post(new JSONObject().append("doesnt", "matter"), "danva");
            fail("Did not throw Exception");
        }
    }


    public void testAddBlocksActionExecutingOnDanvaData() throws Exception {
        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"), "danva");

        BranchSummary branchSum = getBranchBean().getBranchSummaries().get(0);
        NodeRef branchRef = branchSum.asNodeRef();
        Pair<NodeRef, NodeRef> stateRefs = getStatRefs(branchRef);
        NodeRef meeting1 = stateRefs.getFirst();
        NodeRef expanded = stateRefs.getSecond();

        //adding application
        assertEquals(0, getApplicationBean().getApplicationSummaries().size());
        Application appRef = makeTestApplication();
        getApplicationBean().addNewApplication(appRef);
        assertEquals(1, getApplicationBean().getApplicationSummaries().size());

        //move application to branch
        Application change = new Application();
        change.parseRef(appRef.asNodeRef());
        change.setBranchSummary(branchSum);

        getApplicationBean().updateApplication(change);

        //move application to meeting1
        change = new Application();
        change.parseRef(appRef.asNodeRef());
        StateReference state = getWorkflowBean().getStateReference(meeting1);
        change.setState(state);

        getApplicationBean().updateApplication(change);

        int noBlocks = getApplicationBean().getApplication(appRef.asNodeRef()).getBlocks().size();

        //move application to expanded
        change = new Application();
        change.parseRef(appRef.asNodeRef());
        state = getWorkflowBean().getStateReference(expanded);
        change.setState(state);

        getApplicationBean().updateApplication(change);

        //asserting blocks and fields got created
        List<ApplicationBlock> blocks = getApplicationBean().getApplication(appRef.asNodeRef()).getBlocks();
        assertEquals(noBlocks + 2, blocks.size());

        ApplicationBlock infoBlock = getApplicationBean().getBlockByID("additional_info", blocks);
        assertNotNull( infoBlock);
        assertEquals(7, infoBlock.getFields().size());

        ApplicationBlock fileBlock = getApplicationBean().getBlockByID("files", blocks);
        assertNotNull( fileBlock);
        assertEquals(12, fileBlock.getFields().size());

        ApplicationFieldValue headerField = getApplicationBean().getFieldByID("header1", fileBlock.getFields());
        assertNotNull(headerField);
        assertEquals("Obligatoriske bilag", headerField.getSingleValue());


    }

    public void testDanvaModifications() throws Exception {

        TestUtils.wipeData(getServiceRegistry());
        post(new JSONObject().append("doesnt", "matter"), "danva");

        BranchSummary branchSum = getBranchBean().getBranchSummaries().get(0);
        NodeRef branchRef = branchSum.asNodeRef();
        Pair<NodeRef, NodeRef> stateRefs = getStatRefs(branchRef);
        NodeRef meeting1 = stateRefs.getFirst();
        NodeRef expanded = stateRefs.getSecond();

        //adding application
        assertEquals(0, getApplicationBean().getApplicationSummaries().size());
        Application application = makeTestApplication();
        getApplicationBean().addNewApplication(application);
        assertEquals(1, getApplicationBean().getApplicationSummaries().size());

        //move application to branch
        Application change = new Application();
        change.parseRef(application.asNodeRef());
        change.setBranchSummary(branchSum);

        getApplicationBean().updateApplication(change);

        //move application to meeting1
        change = new Application();
        change.parseRef(application.asNodeRef());
        StateReference state = getWorkflowBean().getStateReference(meeting1);
        change.setState(state);

        getApplicationBean().updateApplication(change);

        //move application to expanded
        change = new Application();
        change.parseRef(application.asNodeRef());
        state = getWorkflowBean().getStateReference(expanded);
        change.setState(state);

        getApplicationBean().updateApplication(change);

        //asserting the modifications
        Application updatedApplication = getApplicationBean().getApplication(application.asNodeRef());
        List<ApplicationBlock> blocks = updatedApplication.getBlocks();

        //did the blocks move to the end
        int noBlocks = blocks.size();
        assertEquals("accountability", blocks.get(noBlocks-1).getId());
        assertEquals("budget", blocks.get(noBlocks-2).getId());
        assertEquals("project_period", blocks.get(noBlocks-3).getId());

        //did allowed mime types get on the fields
        ApplicationBlock fileBlock = getApplicationBean().getBlockByID("files", blocks);
        ApplicationFieldValue fileField = getApplicationBean().getFieldByID("file_budget", fileBlock.getFields());
        assertNotNull(getServiceRegistry().getNodeService().getProperty(fileField.asNodeRef(), Utilities.getODFName("allowedMimeTypes")));
        List<String> mimeTypes = (List<String>) getServiceRegistry().getNodeService().getProperty(fileField.asNodeRef(), Utilities.getODFName("allowedMimeTypes"));
        assertNotNull(mimeTypes);
        assertTrue(mimeTypes.contains(MIMETYPE_PDF));
    }

    public Pair<NodeRef, NodeRef> getStatRefs(NodeRef branchRef) throws Exception {

        NodeRef workflowRef = getBranchBean().getBranchWorkflow(branchRef);
        Workflow workflow = getWorkflowBean().getWorkflow(workflowRef);
        List<StateSummary> stateList = workflow.getStates();
        NodeRef meeting1 = null;
        NodeRef expanded = null;
        for (StateSummary stateSummary : stateList) {
            if (stateSummary.getTitle().equals("Bestyrelsesmøde 1")) {
                meeting1 = stateSummary.asNodeRef();
            }
            if (stateSummary.getTitle().equals("Udvidet ansøgning")) {
                expanded = stateSummary.asNodeRef();
            }
        }

        return new Pair<>(meeting1, expanded);
    }

    public Application makeTestApplication() throws IOException {
        String jsonApp = "{\"nodeID\":null,\"storeID\":null,\"title\":\"Martin A/S\",\"id\":\"18\",\"isSeen\":null,\"branchSummary\":null,\"blocks\":[{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"applicant\",\"label\":\"Oplysninger om ansøger\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"company_name\",\"component\":\"text\",\"label\":\"Virksomhedens navn\",\"value\":\"Martin A/S\",\"layout\":\"inline\",\"describes\":\"applicant_name\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"address\",\"component\":\"text\",\"label\":\"Adresse\",\"value\":\"Testgade 2\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"zip_code\",\"component\":\"number\",\"label\":\"Postnummer\",\"value\":\"4455\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"city\",\"component\":\"text\",\"label\":\"By\",\"value\":\"Testby\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"cvr_no\",\"component\":\"number\",\"label\":\"CVR nummer\",\"value\":\"11223344\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"contact_first_name\",\"component\":\"text\",\"label\":\"Navn på projektleder/kontaktperson (fornavn)\",\"value\":\"Martin\",\"layout\":\"inline\",\"describes\":\"first_name\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"contact_last_name\",\"component\":\"text\",\"label\":\"Navn på projektleder/kontaktperson (efternavn)\",\"layout\":\"inline\",\"describes\":\"last_name\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"contact_email\",\"component\":\"text\",\"label\":\"Kontaktpersons e-mail\",\"value\":\"mnn@magenta.dk\",\"layout\":\"inline\",\"describes\":\"email_to\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"contact_phone\",\"component\":\"number\",\"label\":\"Kontaktpersons tlf. nr.\",\"value\":\"11559977\",\"layout\":\"inline\",\"describes\":\"phone_number\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"}],\"nodeRef\":\"workspace://SpacesStore/null\"},{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"partners_0\",\"label\":\"Projektpartnere_0\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"cvr_no\",\"component\":\"number\",\"label\":\"CVR nummer\",\"value\":\"12345678\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_role\",\"component\":\"text\",\"label\":\"Rolle under projektet\",\"value\":\"Vigtige ting\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"contact_person\",\"component\":\"text\",\"label\":\"Kontaktperson\",\"value\":\"Martin\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"company_name\",\"component\":\"text\",\"label\":\"Virksomhedens navn\",\"value\":\"Test A/S\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"}],\"nodeRef\":\"workspace://SpacesStore/null\"},{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"partners_1\",\"label\":\"Projektpartnere_1\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"cvr_no\",\"component\":\"number\",\"label\":\"CVR nummer\",\"value\":\"99557788\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_role\",\"component\":\"text\",\"label\":\"Rolle under projektet\",\"value\":\"Vigtigere ting\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"contact_person\",\"component\":\"text\",\"label\":\"Kontaktperson\",\"value\":\"Mikkel\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"company_name\",\"component\":\"text\",\"label\":\"Virksomhedens navn\",\"value\":\"DobbeltTest A/S\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"}],\"nodeRef\":\"workspace://SpacesStore/null\"},{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"project\",\"label\":\"Projektet\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"subject\",\"component\":\"checkboxes\",\"label\":\"Projektkategori\",\"value\":[false,true,true],\"layout\":\"inline\",\"describes\":\"project_category\",\"wrapper\":\"block\",\"options\":[\"Spildevand\",\"Drikkevand\",\"Klimatilpasning\"],\"type\":\"java.lang.Boolean\"},{\"id\":\"project_title\",\"component\":\"textarea\",\"label\":\"Projektets titel\",\"value\":\"<br/><br/>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut feugiat aliquet accumsan. Nullam ullamcorper dui sed libero tempus, sit amet tincidunt ex aliquet. In pharetra arcu purus. Vivamus risus nisi, sollicitudin suscipit venenatis et, convallis eget arcu. Proin non imperdiet tortor. Curabitur in urna ac dui maximus vulputate. Vestibulum id egestas turpis. Nunc lectus urna, dictum sit amet nibh vitae, condimentum scelerisque ex. Mauris id ipsum quis mauris blandit eleifend quis in leo.<br/><br/>Aliquam eget tempus dolor. Curabitur lacinia dictum eros nec scelerisque. Donec tincidunt sapien sed augue convallis dignissim. Ut quis ante lacus. Cras feugiat leo et erat ullamcorper, id tincidunt nunc semper. Suspendisse aliquet quis massa ac euismod. Morbi eget mi nulla. Phasellus at tortor non mauris elementum. \",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_description\",\"component\":\"textarea\",\"label\":\"Projektbeskrivelse\",\"value\":\"<br/><br/>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi laoreet magna sit amet mattis ullamcorper. Maecenas quis erat venenatis, auctor leo et, fringilla mi. In sodales massa magna, nec molestie dolor vulputate sit amet. Duis eu mi nec est congue pellentesque. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed convallis lectus ac nisl pulvinar dictum. Praesent non tellus ac tortor molestie ultrices sit amet et mi. Donec aliquam ex id nulla fermentum porta. Suspendisse imperdiet mi at fringilla pulvinar. Suspendisse elit nunc, ornare vitae ultricies ut, tincidunt sit amet turpis. Suspendisse ultrices sed dui ac pharetra. Etiam eget velit imperdiet, interdum lacus ac, vestibulum urna.<br/><br/>Nam et porttitor mauris, nec vehicula eros. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam et laoreet nunc. Cras porttitor enim non gravida tempor. Nullam mollis lacus non viverra egestas. Nunc nunc nibh, mattis at enim ullamcorper, porttitor porta turpis. Morbi ut felis in diam ultrices commodo. Ut laoreet mauris ut dolor ultrices, in interdum leo condimentum. Aenean tincidunt purus tincidunt lacus maximus, eget bibendum purus convallis. Cras lorem velit, consequat at libero sit amet, tristique venenatis sem. Cras quis dui vulputate, interdum libero eget, sollicitudin risus. Nullam eget eleifend elit, in fringilla turpis. Curabitur ac rutrum augue, eu tempus nisl. Aliquam erat volutpat. Maecenas id lobortis elit. Aenean euismod sed lacus sed semper.<br/><br/>Pellentesque mollis ornare porta. Donec sed augue lacus. Ut tristique mattis pulvinar. Maecenas lacinia neque sed euismod pharetra. Nunc mollis, ligula vel consequat tempus, odio libero sagittis ante, eget viverra purus felis nec est. Curabitur non egestas velit. Aliquam ut justo non sapien tincidunt finibus. Nullam at nisi fringilla, ullamcorper turpis id, suscipit lorem. Curabitur in auctor ipsum. Proin et turpis mauris. Phasellus fermentum est mauris, vitae aliquet justo mollis id. Sed sed magna odio. Curabitur bibendum. \",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_argument\",\"component\":\"textarea\",\"label\":\"Hvorfor dette projekt?\",\"value\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi laoreet magna sit amet mattis ullamcorper. Maecenas quis erat venenatis, auctor leo et, fringilla mi. In sodales massa magna, nec molestie dolor vulputate sit amet. Duis eu mi nec est congue pellentesque. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed convallis lectus ac nisl pulvinar dictum. \",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_output\",\"component\":\"textarea\",\"label\":\"Output fra projektet\",\"value\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi laoreet magna sit amet mattis ullamcorper. Maecenas quis erat venenatis, auctor leo et, fringilla mi. In sodales massa magna, nec molestie dolor vulputate sit amet. Duis eu mi nec est congue pellentesque. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed convallis lectus ac nisl pulvinar dictum.\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_newsworthy\",\"component\":\"textarea\",\"label\":\"Nyhedsværdi\",\"value\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi laoreet magna sit amet mattis ullamcorper. Maecenas quis erat venenatis, auctor leo et, fringilla mi. In sodales massa magna, nec molestie dolor vulputate sit amet. Duis eu mi nec est congue pellentesque. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed convallis lectus ac nisl pulvinar dictum. Praesent non tellus ac tortor molestie ultrices sit amet et mi. Donec aliquam ex id nulla fermentum porta. Suspendisse imperdiet mi at fringilla pulvinar. Suspendisse elit nunc, ornare vitae ultricies ut, tincidunt sit amet turpis. Suspendisse ultrices sed dui ac pharetra. Etiam eget velit imperdiet, interdum lacus ac, vestibulum urna. \",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_benefit\",\"component\":\"textarea\",\"label\":\"Nytteværdi\",\"value\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi laoreet magna sit amet mattis ullamcorper. Maecenas quis erat venenatis, auctor leo et, fringilla mi. In sodales massa magna, nec molestie dolor vulputate sit amet. Duis eu mi nec est congue pellentesque. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed convallis lectus ac nisl pulvinar dictum.\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_sustainability\",\"component\":\"textarea\",\"label\":\"Effektivisering og bæredygtighed\",\"value\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi laoreet magna sit amet mattis ullamcorper. Maecenas quis erat venenatis, auctor leo et, fringilla mi. In sodales massa magna, nec molestie dolor vulputate sit amet. Duis eu mi nec est congue pellentesque. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed convallis lectus ac nisl pulvinar dictum. Praesent non tellus ac tortor molestie ultrices sit amet et mi. Donec aliquam ex id nulla fermentum porta. Suspendisse imperdiet mi at fringilla pulvinar. Suspendisse elit nunc, ornare vitae ultricies ut, tincidunt sit amet turpis.\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"}],\"nodeRef\":\"workspace://SpacesStore/null\"},{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"project_period\",\"label\":\"Start- og slutdato\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"start_date\",\"label\":\"Forventet projektstart\",\"layout\":\"inline\",\"wrapper\":\"block\"},{\"id\":\"end_date\",\"label\":\"Forventet projektslut\",\"layout\":\"inline\",\"wrapper\":\"block\"}],\"nodeRef\":\"workspace://SpacesStore/null\"},{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"budget\",\"label\":\"Budget\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"applied_amount\",\"component\":\"number\",\"label\":\"Ansøgt støtte\",\"value\":1200000,\"layout\":\"inline\",\"describes\":\"applied_amount\",\"wrapper\":\"block\",\"type\":\"java.lang.Integer\"},{\"id\":\"total_budget\",\"component\":\"number\",\"label\":\"Projektets budgetsum\",\"value\":2000000,\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.Integer\"}],\"nodeRef\":\"workspace://SpacesStore/null\"},{\"nodeID\":null,\"storeID\":null,\"title\":null,\"id\":\"accountability\",\"label\":\"Ansvarlige personer hos hovedansøger\",\"layout\":\"inline\",\"icon\":null,\"collapsible\":null,\"repeatable\":null,\"fields\":[{\"id\":\"project_manager_name\",\"component\":\"text\",\"label\":\"Projektleders/kontaktpersons navn\",\"value\":\"Børge Testeroson\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"project_manager_title\",\"component\":\"text\",\"label\":\"Projektleders/kontaktpersons stilling\",\"value\":\"Vigtig mand\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"responsible_finance_name\",\"component\":\"text\",\"label\":\"Økonomiske/juridiske ansvarliges navn\",\"value\":\"Fup MCunderslæb\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"},{\"id\":\"responsible_finance_title\",\"component\":\"text\",\"label\":\"Økonomiske/juridiske ansvarliges stilling\",\"value\":\"Vigtig vigtig person\",\"layout\":\"inline\",\"wrapper\":\"block\",\"type\":\"java.lang.String\"}],\"nodeRef\":\"workspace://SpacesStore/null\"}],\"state\":null,\"workflow\":null,\"budget\":null,\"nodeRef\":\"workspace://SpacesStore/null\"}";

        ObjectMapper mapper = Utilities.getMapper();
        return mapper.readValue(jsonApp, Application.class);

    }

}
