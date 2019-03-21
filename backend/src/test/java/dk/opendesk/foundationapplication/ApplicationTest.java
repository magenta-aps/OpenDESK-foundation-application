/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import static dk.opendesk.foundationapplication.TestUtils.stateAccessRef;
import static dk.opendesk.foundationapplication.Utilities.APPLICATION_FOLDER_DOCUMENT;

import dk.opendesk.foundationapplication.DAO.StateSummary;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("1", "Recipient", "display:block;", "text", String.class, null, "Cats4Dogs"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("2", "Road", "display:block;", "text", String.class, null, "Testgade"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("3", "Number", "display:block;", "Integer", Integer.class, null, 1337));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("4", "Floor", "display:block;", "text", String.class, null, "2"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("5", "Postal code", "display:block;", "text", String.class, null, "9999"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("6", "First name", "display:block;", "text", String.class, null, "Test"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("7", "Last name", "display:block;", "text", String.class, null, "Osteron"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("8", "Email", "display:block;", "text", String.class, null, "t@est.dk"));
        app1blockRecipient.getFields().add(ResetDemoData.buildValue("9", "Contact Phone", "display:block;", "text", String.class, null, "12345678"));
        
        app1blockOverview.setFields(new ArrayList<>());
        app1blockOverview.getFields().add(ResetDemoData.buildValue("10", "Category", "display:block;", "text", String.class, null, "Category3"));
        app1blockOverview.getFields().add(ResetDemoData.buildValue("11", "Short Description", "display:block;", "text", String.class, null, "We want to buy a cat for every dog"));
        app1blockOverview.getFields().add(ResetDemoData.buildValue("12", "Start Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now())));
        app1blockOverview.getFields().add(ResetDemoData.buildValue("13", "End Date", "display:block;", "datepicker", Date.class, null, Date.from(Instant.now().plus(Duration.ofDays(30)))));
        
        app1details.setFields(new ArrayList<>());
        app1details.getFields().add(ResetDemoData.buildValue("14", "Applied Amount", "display:block;", "Long", Long.class, Functional.amount(), 10000l));
        app1details.getFields().add(ResetDemoData.buildValue("15", "Registration Number", "display:block;", "Long", String.class, null, "1234"));
        app1details.getFields().add(ResetDemoData.buildValue("16", "Account Number", "display:block;", "Long", String.class, null, "12345678"));
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
        assertEquals(TestUtils.stateRecievedRef,application.getState().asNodeRef());
        assertEquals(TestUtils.workFlowRef,application.getWorkflow().asNodeRef());
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
        newStateRef.parseRef(stateAccessRef);
        change.setState(newStateRef);
        post(change, app2Ref.getId());
        
        Application app = get(Application.class, app2Ref.getId());
        assertEquals(currentBudgetRef, app.getBudget().asNodeRef());
        assertEquals(stateAccessRef, app.getState().asNodeRef());
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

        assertNull(app.getBranchSummary().asNodeRef());
        assertNull(app.getState().asNodeRef());

        Application change = new Application();
        change.parseRef(appRef);
        BranchSummary ref = new BranchSummary();
        ref.parseRef(TestUtils.branchRef);
        change.setBranchSummary(ref);
        post(change, app.getNodeID());
        
        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef, app.getBranchSummary().asNodeRef());
        assertEquals(TestUtils.stateRecievedRef, app.getState().asNodeRef());
    }

    public void testUpdateBranchAndState() throws IOException, JSONException {
        BranchSummary ref = new BranchSummary();
        ref.parseRef(TestUtils.branchRef); //todo add different branch
        StateSummary deniedState = new StateSummary();
        deniedState.parseRef(TestUtils.stateDeniedRef);

        //Update branch and state on 'old' application
        NodeRef appRef = TestUtils.application1;
        Application app = get(Application.class, appRef.getId());

        assertEquals(TestUtils.branchRef, app.getBranchSummary().asNodeRef());
        assertEquals(TestUtils.stateRecievedRef, app.getState().asNodeRef());

        Application change = new Application();
        change.parseRef(appRef);
        change.setBranchSummary(ref);
        change.setState(deniedState);
        post(change, app.getNodeID());

        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef, app.getBranchSummary().asNodeRef()); //todo test for the other branch
        assertEquals(TestUtils.stateDeniedRef, app.getState().asNodeRef());


        //Update branch and state on 'new' application
        appRef = TestUtils.application3;
        app = get(Application.class, appRef.getId());

        assertNull(app.getBranchSummary().asNodeRef());
        assertNull(app.getState().asNodeRef());

        change = new Application();
        change.parseRef(appRef);
        change.setBranchSummary(ref);
        change.setState(deniedState);
        post(change, app.getNodeID());

        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef, app.getBranchSummary().asNodeRef()); //todo test for the other branch
        assertEquals(TestUtils.stateDeniedRef, app.getState().asNodeRef());

    }

    public void testChangeState() throws Exception {
        NodeRef appRef = TestUtils.application2;
        Application app = get(Application.class, appRef.getId());

        assertEquals(TestUtils.stateRecievedRef, app.getState().asNodeRef());

        Application change = new Application();
        change.parseRef(appRef);
        StateReference ref = new StateReference();
        ref.parseRef(TestUtils.stateAccessRef);
        change.setState(ref);
        post(change, app.getNodeID());
        
        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.stateAccessRef, app.getState().asNodeRef());
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
        assertEquals("Give me money", description.getValue());
        
        Application change = Utilities.buildChange(beforeChange).changeField(description.getId()).setValue(newDescription).done().build();
        getApplicationBean().updateApplication(change);
        
        Application afterChange = getApplicationBean().getApplication(TestUtils.application1);
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getValue());
        
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
        assertEquals("Give me money", description.getValue());
        
        description.setValue(newDescription);
        getApplicationBean().updateApplication(beforeChange);
        
        Application afterChange = getApplicationBean().getApplication(TestUtils.application1);
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getValue());
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
        assertEquals("Give me money", description.getValue());

        Application change = Utilities.buildChange(beforeChange).changeField(description.getId()).setValue(newDescription).done().build();
        post(change, TestUtils.application1.getId());

        Application afterChange = get(Application.class, TestUtils.application1.getId());
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getValue());

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
        assertEquals("Give me money", description.getValue());
        description.setValue(newDescription);
        
        post(beforeChange, TestUtils.application1.getId());

        Application afterChange = get(Application.class, TestUtils.application1.getId());
        assertEquals(newDescription, afterChange.getBlocks().get(1).getFields().get(1).getValue());

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
        List<ApplicationSummary> branchApplications = get(List.class, ApplicationSummary.class, "?branchID="+TestUtils.branchRef.getId());
        assertEquals(2, branchApplications.size());
        List<ApplicationSummary> budget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId());
        assertEquals(2, budget1Applications.size());
        List<ApplicationSummary> budget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId());
        assertEquals(0, budget2Applications.size());
        List<ApplicationSummary> branchbudget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId()+"&branchID="+TestUtils.branchRef.getId());
        assertEquals(2, branchbudget1Applications.size());
        List<ApplicationSummary> branchbudget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId()+"&branchID="+TestUtils.branchRef.getId());
        assertEquals(0, branchbudget2Applications.size());
        
        Application change = Utilities.buildChange(getApplicationBean().getApplication(TestUtils.application1)).setBudget(TestUtils.budgetRef2).build();
        getApplicationBean().updateApplication(change);
        
        budget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId());
        assertEquals(1, budget1Applications.size());
        budget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId());
        assertEquals(1, budget2Applications.size());
        branchbudget1Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef1.getId()+"&branchID="+TestUtils.branchRef.getId());
        assertEquals(1, branchbudget1Applications.size());
        branchbudget2Applications = get(List.class, ApplicationSummary.class, "?budgetID="+TestUtils.budgetRef2.getId()+"&branchID="+TestUtils.branchRef.getId());
        assertEquals(1, branchbudget2Applications.size());
        
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
    
}