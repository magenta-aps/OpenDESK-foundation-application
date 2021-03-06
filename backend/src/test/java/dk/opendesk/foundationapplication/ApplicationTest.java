/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationField;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;

import static dk.opendesk.foundationapplication.TestUtils.w1StateRecievedRef;
import static dk.opendesk.foundationapplication.Utilities.APPLICATION_FOLDER_DOCUMENT;
import static dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData.RANDOM;
import static dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData.lorem;

import dk.opendesk.foundationapplication.DAO.StateSummary;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import static dk.opendesk.foundationapplication.TestUtils.w1StateAccessRef;
import org.json.JSONException;

/**
 *
 * @author martin
 */
public class ApplicationTest extends AbstractTestClass{

    public ApplicationTest() {
        super("/foundation/application");
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
    
    public void testAddApplication() throws Exception{
        assertEquals(3, getApplicationBean().getApplicationSummaries().size());
        
        String applicationTitle = "More cats for dogs";

        Application newApplication = new Application();
        newApplication.setTitle(applicationTitle);
        ApplicationBlock app1blockRecipient = new ApplicationBlock();
        app1blockRecipient.setId("1");
        app1blockRecipient.setLabel("Recipients");
        ApplicationBlock app1blockOverview = new ApplicationBlock();
        app1blockOverview.setId("2");
        app1blockOverview.setLabel("Overview");
        ApplicationBlock app1details = new ApplicationBlock();
        app1details.setId("3");
        app1details.setLabel("Details");
        app1blockRecipient.setFields(new ArrayList<>());
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("1", "Recipient", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Cats4Dogs"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("2", "Road", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Testgade"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("3", "Number", "display:block;", "Integer", Integer.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,1337));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("4", "Floor", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"2"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("5", "Postal code", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"9999"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Test"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Osteron"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"t@est.dk"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"12345678"));
        
        app1blockOverview.setFields(new ArrayList<>());
        app1blockOverview.getFields().add(ResetDemoData.buildValue("10", "Category", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"Category3"));
        app1blockOverview.getFields().add(ResetDemoData.buildValue("11", "Short Description", "display:block;", "text", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"We want to buy a cat for every dog"));
        app1blockOverview.getFields().add(ResetDemoData.buildValue("12", "Start Date", "display:block;", "datepicker", Date.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,Date.from(Instant.now())));
        app1blockOverview.getFields().add(ResetDemoData.buildValue("13", "End Date", "display:block;", "datepicker", Date.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,Date.from(Instant.now().plus(Duration.ofDays(30)))));
        
        app1details.setFields(new ArrayList<>());
        app1details.getFields().add(ResetDemoData.buildValue("14", "Applied Amount", "display:block;", "Long", Long.class, Functional.amount(),  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,10000l));
        app1details.getFields().add(ResetDemoData.buildValue("15", "Registration Number", "display:block;", "Long", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"1234"));
        app1details.getFields().add(ResetDemoData.buildValue("16", "Account Number", "display:block;", "Long", String.class, null,  null,lorem(RANDOM.nextInt(15)),null,"'v-validate': 'number|max:15'",null,true,"12345678"));
        newApplication.setBlocks(Arrays.asList(new ApplicationBlock[]{app1blockRecipient, app1blockOverview, app1details}));
        
        ApplicationReference reference = post(newApplication, ApplicationReference.class);
        assertNotNull(reference);
        assertEquals(applicationTitle, reference.getTitle());
        
        assertEquals(4, getApplicationBean().getApplicationSummaries().size());
    }
    
    public void testAddTestApplication() throws Exception{
        assertEquals(3, getApplicationBean().getApplicationSummaries().size());
        
        ApplicationReference input = new ApplicationReference();
        input.setTitle("Hello");
        input.setId("5");
        
        ApplicationReference reference = post(input, ApplicationReference.class);
        assertNotNull(reference);
        assertEquals("Hello", reference.getTitle());
        
        assertEquals(4, getApplicationBean().getApplicationSummaries().size());
    }
    
    public void testGetApplicationFromSummary() throws Exception{
        for(ApplicationSummary summary : getApplicationBean().getApplicationSummaries()){
            Application application = get(Application.class, summary.getNodeID());
            assertEquals(summary.getTitle(), application.getTitle());
        }
    }
    public void testGetApplicationState() throws Exception{
        Application application = getApplicationBean().getApplication(TestUtils.application1);
        assertEquals(TestUtils.w1StateRecievedRef,application.getState().asNodeRef());
        assertEquals(TestUtils.workFlowRef1,application.getWorkflow().asNodeRef());
    }
    
    
    public void testUpdateBudget() throws Exception{
        NodeRef currentBudgetRef = TestUtils.budgetRef1;
        NodeRef newBudgetRef = TestUtils.budgetRef2;
        NodeRef app2Ref = TestUtils.application2;
        
        Budget currentBudget = getBudgetBean().getBudget(currentBudgetRef);
        Budget newBudget = getBudgetBean().getBudget(newBudgetRef);
        
        Long expectedAmount = TestUtils.APPLICATION1_AMOUNT+TestUtils.APPLICATION2_AMOUNT;
        assertEquals(TestUtils.BUDGET1_AMOUNT, currentBudget.getAmountAvailable());
        assertEquals(TestUtils.BUDGET2_AMOUNT, newBudget.getAmountAvailable());
        assertEquals(expectedAmount, currentBudget.getAmountNominated());
        assertEquals(Long.valueOf(0), newBudget.getAmountNominated());
        
        
        Application change = new Application();
        change.parseRef(app2Ref);
        StateReference newStateRef = new StateReference();
        newStateRef.parseRef(w1StateAccessRef);
        change.setState(newStateRef);
        post(change, app2Ref.getId());
        
        Application app = get(Application.class, app2Ref.getId());
        assertEquals(currentBudgetRef, app.getBudget().asNodeRef());
        assertEquals(w1StateAccessRef, app.getState().asNodeRef());
        currentBudget = getBudgetBean().getBudget(currentBudgetRef);
        newBudget = getBudgetBean().getBudget(newBudgetRef);
        
        expectedAmount = TestUtils.BUDGET1_AMOUNT-TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, currentBudget.getAmountAvailable());
        assertEquals(TestUtils.BUDGET2_AMOUNT, newBudget.getAmountAvailable());
        assertEquals(TestUtils.APPLICATION2_AMOUNT, currentBudget.getAmountAccepted());
        assertEquals(TestUtils.APPLICATION1_AMOUNT, currentBudget.getAmountNominated());
        
        change = new Application();
        change.parseRef(app2Ref);
        BudgetReference newBudgetReference = new BudgetReference();
        newBudgetReference.parseRef(newBudgetRef);
        change.setBudget(newBudgetReference);
        post(change, app2Ref.getId());

        app = get(Application.class, app2Ref.getId());
        assertEquals(newBudgetRef, app.getBudget().asNodeRef());
        currentBudget = getBudgetBean().getBudget(currentBudgetRef);
        newBudget = getBudgetBean().getBudget(newBudgetRef);
        
        assertEquals(TestUtils.BUDGET1_AMOUNT, currentBudget.getAmountAvailable());
        expectedAmount = TestUtils.BUDGET2_AMOUNT-TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, newBudget.getAmountAvailable());

    }
    
    public void testUpdateBranchFromNone() throws Exception {
        NodeRef appRef = TestUtils.application3;
        Application app = get(Application.class, appRef.getId());

        assertNull(app.getBranchSummary());
        assertNull(app.getState());

        Application change = new Application();
        change.parseRef(appRef);
        BranchSummary ref = new BranchSummary();
        ref.parseRef(TestUtils.branchRef1);
        change.setBranchSummary(ref);
        post(change, app.getNodeID());
        
        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef1, app.getBranchSummary().asNodeRef());
        assertEquals(TestUtils.w1StateRecievedRef, app.getState().asNodeRef());
    }

    public void testUpdateBranchAndState() throws IOException, JSONException {
        BranchSummary ref = new BranchSummary();
        ref.parseRef(TestUtils.branchRef1); //todo add different branch
        StateSummary deniedState = new StateSummary();
        deniedState.parseRef(TestUtils.w1StateDeniedRef);

        //Update branch and state on 'old' application
        NodeRef appRef = TestUtils.application1;
        Application app = get(Application.class, appRef.getId());

        assertEquals(TestUtils.branchRef1, app.getBranchSummary().asNodeRef());
        assertEquals(TestUtils.w1StateRecievedRef, app.getState().asNodeRef());

        Application change = new Application();
        change.parseRef(appRef);
        change.setBranchSummary(ref);
        change.setState(deniedState);
        post(change, app.getNodeID());

        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef1, app.getBranchSummary().asNodeRef()); //todo test for the other branch
        assertEquals(TestUtils.w1StateDeniedRef, app.getState().asNodeRef());


        //Update branch and state on 'new' application
        appRef = TestUtils.application3;
        app = get(Application.class, appRef.getId());

        assertNull(app.getBranchSummary());
        assertNull(app.getState());

        change = new Application();
        change.parseRef(appRef);
        change.setBranchSummary(ref);
        change.setState(deniedState);
        post(change, app.getNodeID());

        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef1, app.getBranchSummary().asNodeRef()); //todo test for the other branch
        assertEquals(TestUtils.w1StateDeniedRef, app.getState().asNodeRef());

    }

    public void testChangeState() throws Exception {
        NodeRef appRef = TestUtils.application2;
        Application app = get(Application.class, appRef.getId());

        assertEquals(TestUtils.w1StateRecievedRef, app.getState().asNodeRef());

        Application change = new Application();
        change.parseRef(appRef);
        StateReference ref = new StateReference();
        ref.parseRef(TestUtils.w1StateAccessRef);
        change.setState(ref);
        post(change, app.getNodeID());
        
        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.w1StateAccessRef, app.getState().asNodeRef());
    }
    

    public void testSeenByList() throws Exception {
        NodeRef appRef = TestUtils.application2;

        //isSeen is initially false
        Application app = get(Application.class, appRef.getId());
        assertFalse(app.getIsSeen());

        //from isSeen=false to isSeen=false
        Application change = new Application();
        change.parseRef(appRef);
        change.setIsSeen(false);
        post(change,appRef.getId());

        app = get(Application.class, appRef.getId());
        assertFalse(app.getIsSeen());

        //from isSeen=false to isSeen=true
        change = new Application();
        change.parseRef(appRef);
        change.setIsSeen(true);
        post(change,appRef.getId());

        app = get(Application.class, appRef.getId());
        assertTrue(app.getIsSeen());

        //from isSeen=true to isSeen=true
        change = new Application();
        change.parseRef(appRef);
        change.setIsSeen(true);
        post(change,appRef.getId());

        app = get(Application.class, appRef.getId());
        assertTrue(app.getIsSeen());

        //from isSeen=true to isSeen=false
        change = new Application();
        change.parseRef(appRef);
        change.setIsSeen(false);
        post(change,appRef.getId());

        app = get(Application.class, appRef.getId());
        assertFalse(app.getIsSeen());

        //todo Test with two different users as well
    }

    public void testDeleteApplication() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();

        //before delete
        List<ChildAssociationRef> applications = ns.getChildAssocs(getApplicationBean().getDataHome(), Utilities.getODFName(Utilities.DATA_ASSOC_APPLICATIONS), null);
        List<ChildAssociationRef> deletedApplications = ns.getChildAssocs(getApplicationBean().getDataHome(), Utilities.getODFName(Utilities.DATA_ASSOC_DELETED_APPLICATION), null);

        assertEquals(3, applications.size());
        assertEquals(0, deletedApplications.size());

        //choosing application to remove
        NodeRef applicationToRemove = applications.get(0).getChildRef();
        assertFalse(ns.getTargetAssocs(applicationToRemove,qname -> true).size() == 0); //the application has associations (branch, budget, state)

        //removing application with foundationBean method
        getApplicationBean().deleteApplication(applicationToRemove);

        //after delete
        applications = ns.getChildAssocs(getApplicationBean().getDataHome(), Utilities.getODFName(Utilities.DATA_ASSOC_APPLICATIONS), null);
        deletedApplications = ns.getChildAssocs(getApplicationBean().getDataHome(), Utilities.getODFName(Utilities.DATA_ASSOC_DELETED_APPLICATION), null);

        assertEquals(2, applications.size());
        assertEquals(1, deletedApplications.size());

        assertEquals(deletedApplications.get(0).getChildRef(), applicationToRemove); //the deleted application is the intended one
        assertTrue(ns.getTargetAssocs(applicationToRemove,qname -> true).size() == 0); //the associations of the application has been removed

        //choosing application to remove
        applicationToRemove = applications.get(0).getChildRef();

        //removing application with webscript
        delete(String.class, applicationToRemove.getId());

        //after delete
        applications = ns.getChildAssocs(getApplicationBean().getDataHome(), Utilities.getODFName(Utilities.DATA_ASSOC_APPLICATIONS), null);
        deletedApplications = ns.getChildAssocs(getApplicationBean().getDataHome(), Utilities.getODFName(Utilities.DATA_ASSOC_DELETED_APPLICATION), null);

        assertEquals(1, applications.size());
        assertEquals(2, deletedApplications.size());


    }
    
    public void testUpdateApplication() throws Exception{
        String newDescription = "new description";
        Application beforeChange = getApplicationBean().getApplication(TestUtils.application1);
        ApplicationBlock overview = beforeChange.getBlocks().get(1);
        ApplicationFieldValue description = overview.getFields().get(1);
        assertEquals("Overview", overview.getLabel());
        assertEquals("Short Description", description.getLabel());
        assertEquals("Give me money", description.getSingleValue());
        
        Application change = Utilities.buildChange(beforeChange).changeField(description.getId()).setValue(newDescription).done().build();
        getApplicationBean().updateApplication(change);
        
        Application afterChange = getApplicationBean().getApplication(TestUtils.application1);
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getSingleValue());
        
        for(int blk = 0 ; blk<beforeChange.getBlocks().size() ; blk++){
            for(int fld = 0 ; fld<beforeChange.getBlocks().get(blk).getFields().size() ; fld++){
                if(!(blk == 1 && fld == 1)){
                    assertEquals(beforeChange.getBlocks().get(blk).getFields().get(fld), afterChange.getBlocks().get(blk).getFields().get(fld));
                }
                
            }
        }
    }
    
        
    public void testUpdateFullApplication() throws Exception{
        String newDescription = "new description";
        Application beforeChange = getApplicationBean().getApplication(TestUtils.application1);
        ApplicationBlock overview = beforeChange.getBlocks().get(1);
        ApplicationFieldValue description = overview.getFields().get(1);
        assertEquals("Overview", overview.getLabel());
        assertEquals("Short Description", description.getLabel());
        assertEquals("Give me money", description.getSingleValue());
        
        description.setSingleValue(newDescription);
        getApplicationBean().updateApplication(beforeChange);
        
        Application afterChange = getApplicationBean().getApplication(TestUtils.application1);
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getSingleValue());
        for(int blk = 0 ; blk<beforeChange.getBlocks().size() ; blk++){
            for(int fld = 0 ; fld<beforeChange.getBlocks().get(blk).getFields().size() ; fld++){
                if(!(blk == 1 && fld == 1)){
                    assertEquals(beforeChange.getBlocks().get(blk).getFields().get(fld), afterChange.getBlocks().get(blk).getFields().get(fld));
                }
                
            }
        }
    }
    
    public void testRestUpdateApplication() throws Exception {
        String newDescription = "changed description";
        Application beforeChange = get(Application.class, TestUtils.application1.getId());
        ApplicationBlock overview = beforeChange.getBlocks().get(1);
        ApplicationFieldValue description = overview.getFields().get(1);
        assertEquals("Overview", overview.getLabel());
        assertEquals("Short Description", description.getLabel());
        assertEquals("Give me money", description.getSingleValue());

        Application change = Utilities.buildChange(beforeChange).changeField(description.getId()).setValue(newDescription).done().build();
        post(change, TestUtils.application1.getId());

        Application afterChange = get(Application.class, TestUtils.application1.getId());
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getSingleValue());

        for (int blk = 0; blk < beforeChange.getBlocks().size(); blk++) {
            for (int fld = 0; fld < beforeChange.getBlocks().get(blk).getFields().size(); fld++) {
                if (!(blk == 1 && fld == 1)) {
                    assertEquals(beforeChange.getBlocks().get(blk).getFields().get(fld), afterChange.getBlocks().get(blk).getFields().get(fld));
                }
            }
        }
    }
    
    public void testRestUpdateFullApplication() throws Exception {
        String newDescription = "changed description";
        Application beforeChange = get(Application.class, TestUtils.application1.getId());
        ApplicationBlock overview = beforeChange.getBlocks().get(1);
        ApplicationFieldValue description = overview.getFields().get(1);
        assertEquals("Overview", overview.getLabel());
        assertEquals("Short Description", description.getLabel());
        assertEquals("Give me money", description.getSingleValue());
        description.setSingleValue(newDescription);
        
        post(beforeChange, TestUtils.application1.getId());

        Application afterChange = get(Application.class, TestUtils.application1.getId());
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getSingleValue());

        for (int blk = 0; blk < beforeChange.getBlocks().size(); blk++) {
            for (int fld = 0; fld < beforeChange.getBlocks().get(blk).getFields().size(); fld++) {
                if (!(blk == 1 && fld == 1)) {
                    assertEquals(beforeChange.getBlocks().get(blk).getFields().get(fld), afterChange.getBlocks().get(blk).getFields().get(fld));
                }
            }
        }
    }
    
    public void testGetApplications() throws Exception {
        List<ApplicationSummary> allApplications = get(List.class, ApplicationSummary.class);
        assertEquals(3, allApplications.size());
        List<ApplicationSummary> branchApplications = get(List.class, ApplicationSummary.class, "?branchID="+TestUtils.branchRef1.getId());
        assertEquals(2, branchApplications.size());
        List<ApplicationSummary> budget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId());
        assertEquals(2, budget1Applications.size());
        List<ApplicationSummary> budget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId());
        assertEquals(0, budget2Applications.size());
        List<ApplicationSummary> branchbudget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId()+"&branchID="+TestUtils.branchRef1.getId());
        assertEquals(2, branchbudget1Applications.size());
        List<ApplicationSummary> branchbudget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId()+"&branchID="+TestUtils.branchRef1.getId());
        assertEquals(0, branchbudget2Applications.size());
        
        Application change = Utilities.buildChange(getApplicationBean().getApplication(TestUtils.application1)).setBudget(TestUtils.budgetRef2).build();
        getApplicationBean().updateApplication(change);
        
        budget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId());
        assertEquals(1, budget1Applications.size());
        budget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId());
        assertEquals(1, budget2Applications.size());
        branchbudget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId()+"&branchID="+TestUtils.branchRef1.getId());
        assertEquals(1, branchbudget1Applications.size());
        branchbudget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId()+"&branchID="+TestUtils.branchRef1.getId());
        assertEquals(1, branchbudget2Applications.size());
        
    }

    public void testBlocksAndFields() throws Exception {
        NodeRef appRef = TestUtils.application1;
        Application app = getApplicationBean().getApplication(appRef);

        Application change = Utilities.buildChange(app)
                .changeBlock("3")
                .setLabel("testDetails")
                .setLayout("testLayout")
                .setIcon("testIcon")
                .setCollapsible(true)
                .setRepeatable(true)
                .done()
                .build();
        getApplicationBean().updateApplication(change);

        List<ApplicationField> schemaChange = Utilities.buildFieldChange(getApplicationBean()).changeField("14")
                .setLabel("testAmount")
                .setHint("testHint")
                .setWrapper("testWrapper")
                .done()
                .build();
        
        getApplicationBean().updateApplicationStaticData(schemaChange);

        
        //does the blocks exists
        Application newApp = getApplicationBean().getApplication(appRef);
        List<ApplicationBlock> blocks = newApp.getBlocks();
        assertEquals(3, blocks.size());

        ApplicationBlock recipientBlock = null;
        ApplicationBlock overviewBlock = null;
        ApplicationBlock detailsBlock = null;
        for (ApplicationBlock block : blocks) {
            if (block.getLabel().equals("Recipient")) {
                recipientBlock = block;
            }
            if (block.getLabel().equals("Overview")) {
                overviewBlock = block;
            }
            if (block.getLabel().equals("testDetails")) {
                detailsBlock = block;
            }
        }
        assertNotNull(recipientBlock);
        assertNotNull(overviewBlock);
        assertNotNull(detailsBlock);

        assertEquals("testLayout", detailsBlock.getLayout());
        assertEquals("testIcon", detailsBlock.getIcon());
        assertTrue(detailsBlock.getCollapsible());
        assertTrue(detailsBlock.getRepeatable());

        //does the fields on block 'testDetails' exist
        List<ApplicationFieldValue> fields = detailsBlock.getFields();
        assertEquals(3, fields.size());

        ApplicationFieldValue amountField = null;
        ApplicationFieldValue regNumField = null;
        ApplicationFieldValue accNumField = null;
        for (ApplicationFieldValue field : fields) {
            if (field.getLabel().equals("testAmount")) {
                amountField = field;
            }
            if (field.getLabel().equals("Registration Number")) {
                regNumField = field;
            }
            if (field.getLabel().equals("Account Number")) {
                accNumField = field;
            }
        }
        assertNotNull(amountField);
        assertNotNull(regNumField);
        assertNotNull(accNumField);

        //does all fields on field 'testAmount' exist
        assertEquals("14", amountField.getId());
        assertEquals(Long.class, amountField.getTypeAsClass());
        assertEquals("Long", amountField.getComponent());
        assertEquals(Functional.amount().getFriendlyName(), amountField.getDescribes());
        assertEquals("display:block;", amountField.getLayout());
        assertEquals("testHint", amountField.getHint());
        assertEquals("testWrapper", amountField.getWrapper());
        assertEquals("'v-validate': 'number|max:15'",amountField.getValidation());
        //assertFalse(amountField.getReadOnly());
    }

    public void testGetOrCreateDocumentFolder() throws Exception {
        //no document folder
        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_FOLDER_DOCUMENT), null);
        assertEquals(0, childAssociationRefs.size());

        //one document folder
        NodeRef folderRefFirstTime = getApplicationBean().getOrCreateDocumentFolder(TestUtils.application1);
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_FOLDER_DOCUMENT), null);
        assertEquals(1, childAssociationRefs.size());

        //still one document folder
        NodeRef folderRefSecondTime = getApplicationBean().getOrCreateDocumentFolder(TestUtils.application1);
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_FOLDER_DOCUMENT), null);
        assertEquals(1, childAssociationRefs.size());

        assertEquals(folderRefFirstTime, folderRefSecondTime);
    }

    public void testGetApplicationDocumentFolderScript() throws Exception {
        String appId = TestUtils.application1.getId();

        //no document folder
        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_FOLDER_DOCUMENT), null);
        assertEquals(0, childAssociationRefs.size());

        //one document folder
        String folderIdFirstTime = get(String.class, appId + "/documentfolder");
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_FOLDER_DOCUMENT), null);
        assertEquals(1, childAssociationRefs.size());

        //still one document folder
        String folderIdSecondTime = get(String.class, appId + "/documentfolder");
        childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(TestUtils.application1, Utilities.getODFName(APPLICATION_FOLDER_DOCUMENT), null);
        assertEquals(1, childAssociationRefs.size());

        assertEquals(folderIdFirstTime, folderIdSecondTime);
    }

    public void testGetApplicationReference() throws Exception {
        ApplicationReference applicationReference = getApplicationBean().getApplicationReference(TestUtils.application1);
        assertEquals(TestUtils.APPLICATION1_NAME, applicationReference.getTitle());
        assertNotNull(applicationReference.getId());
        assertNotNull(applicationReference.getIsSeen());
    }

    public void testApplicationId() throws Exception {
        //checking that an application with an id already set gets the right id assigned
        Application application6 = new Application();
        application6.setTitle("title6");
        application6.setId("6");
        ApplicationReference appRef6 = getApplicationBean().addNewApplication(application6);
        assertEquals("6", appRef6.getId());

        Application application7 = new Application();
        application7.setTitle("title7");
        application7.setId("7");
        ApplicationReference appRef7 = getApplicationBean().addNewApplication(application7);
        assertEquals("7", appRef7.getId());

        //checking consecutive id's on new application (id's 1-3 already made in setup)
        Application application4 = new Application();
        application4.setTitle("title1");
        ApplicationReference appRef4 = getApplicationBean().addNewApplication(application4);
        assertEquals("4", appRef4.getId());

        Application application5 = new Application();
        application5.setTitle("title5");
        ApplicationReference appRef5 = getApplicationBean().addNewApplication(application5);
        assertEquals("5", appRef5.getId());

        //checking that repeated id gets rejected
        Application application5dot2 = new Application();
        application5dot2.setTitle("title5dot2");
        application5dot2.setId("5");
        try {
            getApplicationBean().addNewApplication(application5dot2);
            fail();
        } catch (AlfrescoRuntimeException e) {
            assertTrue(e.getMessage().contains(ApplicationBean.ID_IN_USE));
        }

        //checking that the next application after id 5 gets id 8 because id 6 and id 7 already exists
        Application application8 = new Application();
        application8.setTitle("title8");
        application8.setId("8");
        ApplicationReference appRef8 = getApplicationBean().addNewApplication(application8);
        assertEquals("8", appRef8.getId());
        application6.setId("6");



    }


    public void testMoveApplicationWithWebscript() throws Exception {
        NodeRef appRef = TestUtils.application1;
        String appId = appRef.getId();
        String stateId = w1StateAccessRef.getId();
        //String data = "{\"nodeRef\" : \"workspace://SpacesStore/"+ appId +"\",\"state\":{\"nodeRef\" : \"workspace://SpacesStore/" + stateId + "\",\"nodeID\":\"" + stateId + "\"}}";
        String data = "{\"nodeRef\" : \"workspace://SpacesStore/"+ appId +"\",\"state\":{\"nodeID\":\"" + stateId + "\"}}";

        Application change = Utilities.getMapper().readValue(data,Application.class);
        assertEquals(w1StateRecievedRef, getApplicationBean().getApplication(appRef).getState().asNodeRef());
        post(change, "/" + appId);
        assertEquals(w1StateAccessRef, getApplicationBean().getApplication(appRef).getState().asNodeRef());
    }
}