/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import static dk.opendesk.foundationapplication.TestUtils.stateAccessRef;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.webscripts.foundation.ResetDemoData;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 *
 * @author martin
 */
public class ApplicationTest extends AbstractTestClass{
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

    public ApplicationTest() {
        super("/foundation/application");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        TestUtils.wipeData(serviceRegistry);
        TestUtils.setupSimpleFlow(serviceRegistry);
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.wipeData(serviceRegistry);
    }
    
    public void testAddApplication() throws Exception{
        assertEquals(3, foundationBean.getApplicationSummaries().size());
        
        String applicationTitle = "More cats for dogs";
        
        Application newApplication = new Application();
        newApplication.setTitle(applicationTitle);
        ApplicationPropertiesContainer app1blockRecipient = new ApplicationPropertiesContainer();
        ApplicationPropertiesContainer app1blockOverview = new ApplicationPropertiesContainer();
        ApplicationPropertiesContainer app1details = new ApplicationPropertiesContainer();
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
        newApplication.setBlocks(Arrays.asList(new ApplicationPropertiesContainer[]{app1blockRecipient, app1blockOverview, app1details}));
        
        ApplicationReference reference = post(newApplication, ApplicationReference.class);
        assertNotNull(reference);
        assertEquals(applicationTitle, reference.getTitle());
        
        assertEquals(4, foundationBean.getApplicationSummaries().size());
    }
    
    public void testAddTestApplication() throws Exception{
        assertEquals(3, foundationBean.getApplicationSummaries().size());
        
        ApplicationReference input = new ApplicationReference();
        input.setTitle("Hello");
        
        ApplicationReference reference = post(input, ApplicationReference.class);
        assertNotNull(reference);
        assertEquals("Hello", reference.getTitle());
        
        assertEquals(4, foundationBean.getApplicationSummaries().size());
    }
    
    public void testGetApplication() throws Exception{
        for(ApplicationSummary summary : foundationBean.getApplicationSummaries()){
            Application application = get(Application.class, summary.getNodeID());
            assertEquals(summary.getTitle(), application.getTitle());
        }
    }
    
    public void testUpdateBudget() throws Exception{
        NodeRef currentBudgetRef = TestUtils.budgetRef1;
        NodeRef newBudgetRef = TestUtils.budgetRef2;
        NodeRef app2Ref = TestUtils.application2;
        
        Budget currentBudget = foundationBean.getBudget(currentBudgetRef);
        Budget newBudget = foundationBean.getBudget(newBudgetRef);
        
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
        currentBudget = foundationBean.getBudget(currentBudgetRef);
        newBudget = foundationBean.getBudget(newBudgetRef);
        
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
        currentBudget = foundationBean.getBudget(currentBudgetRef);
        newBudget = foundationBean.getBudget(newBudgetRef);
        
        assertEquals(TestUtils.BUDGET1_AMOUNT, currentBudget.getAmountAvailable());
        expectedAmount = TestUtils.BUDGET2_AMOUNT-TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, newBudget.getAmountAvailable());

    }
    
    public void updateBranchFromNone() throws Exception {
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
    
    
    
    
    
}