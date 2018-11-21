/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class Workflow extends BaseWebScriptTest{
    private ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
                
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    public void testNewApplication() throws Exception{
        assertNotNull(serviceRegistry);
        assertNotNull(serviceRegistry.getNodeService());
        assertNotNull(serviceRegistry.getSearchService());
        assertNotNull(serviceRegistry.getPermissionService());
    }
    
    
    
    
}
