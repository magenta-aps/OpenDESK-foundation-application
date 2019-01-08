/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.time.Duration;
import java.time.Instant;
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
        newApplication.setRecipient("Cats4Dogs");
        newApplication.setShortDescription("We want to buy a cat for every dog");
        newApplication.setContactFirstName("Test");
        newApplication.setContactLastName("Osteron");
        newApplication.setContactEmail("t@est.dk");
        newApplication.setContactPhone("12345678");
        newApplication.setCategory("Category3");
        newApplication.setAddressRoad("Testgade");
        newApplication.setAddressNumber(1337);
        newApplication.setAddressFloor("2");
        newApplication.setAddressPostalCode("9999");
        newApplication.setAmountApplied(10000l);
        newApplication.setAccountRegistration("1234");
        newApplication.setAccountNumber("12345678");
        newApplication.setStartDate(Date.from(Instant.now()));
        newApplication.setEndDate(Date.from(Instant.now().plus(Duration.ofDays(30))));
        
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
        NodeRef currentBudget = TestUtils.budgetRef1;
        NodeRef newBudget = TestUtils.budgetRef2;
        NodeRef appRef = TestUtils.application2;
        
        Long expectedAmount = TestUtils.BUDGET1_AMOUNT-TestUtils.APPLICATION1_AMOUNT-TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, foundationBean.getBudgetRemainingFunding(currentBudget));
        assertEquals(TestUtils.BUDGET2_AMOUNT, foundationBean.getBudgetRemainingFunding(newBudget));
        
        Application app = get(Application.class, appRef.getId());
        assertEquals(currentBudget, app.getBudget().asNodeRef());
        
        Application change = new Application();
        change.parseRef(appRef);
        BudgetReference newBudgetReference = new BudgetReference();
        newBudgetReference.parseRef(newBudget);
        change.setBudget(newBudgetReference);
        
        post(change, appRef.getId());
        
        app = get(Application.class, appRef.getId());
        assertEquals(newBudget, app.getBudget().asNodeRef());
        
        expectedAmount = TestUtils.BUDGET1_AMOUNT-TestUtils.APPLICATION1_AMOUNT;
        assertEquals(expectedAmount, foundationBean.getBudgetRemainingFunding(currentBudget));
        expectedAmount = TestUtils.BUDGET2_AMOUNT-TestUtils.APPLICATION2_AMOUNT;
        assertEquals(expectedAmount, foundationBean.getBudgetRemainingFunding(newBudget));

    }
    
    public void updateBranchFromNone() throws Exception {
        NodeRef appRef = TestUtils.application3;
        Application app = get(Application.class, appRef.getId());

        assertNull(app.getBranchRef().asNodeRef());
        assertNull(app.getState().asNodeRef());

        Application change = new Application();
        change.parseRef(appRef);
        BranchReference ref = new BranchReference();
        ref.parseRef(TestUtils.branchRef);
        change.setBranchRef(ref);
        post(change, app.getNodeID());
        
        app = get(Application.class, appRef.getId());
        assertEquals(TestUtils.branchRef, app.getBranchRef().asNodeRef());
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