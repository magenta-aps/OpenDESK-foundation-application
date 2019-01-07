/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

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
    
    
    
}