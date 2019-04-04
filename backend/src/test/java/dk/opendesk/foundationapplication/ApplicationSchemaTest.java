/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 *
 * @author martin
 */
public class ApplicationSchemaTest extends AbstractTestClass {

    public ApplicationSchemaTest() {
        super("/foundation/applicationschema");
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
    
    public void testGetApplicationSchema() throws Exception{
        
    }
    
}
