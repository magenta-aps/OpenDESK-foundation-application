/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import java.io.IOException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;

/**
 *
 * @author martin
 */
public class BranchTest extends BaseWebScriptTest {
    private final ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");
    private final FoundationBean foundationBean = (FoundationBean) getServer().getApplicationContext().getBean("foundationBean");

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
    
//    public void testBranchToJSON() throws JSONException{
//        Branch branch = new Branch("My test title", "Very unique", "Das title", "Also very unique");
//        JSONObject jsonBranch = new JSONObject(branch);
//        Branch fromJSON = Branch.fromJSON(jsonBranch);
//        assertEquals(branch, fromJSON);
//    }
    
    public void testAddBranchWebScript() throws Exception{
        assertEquals(1, foundationBean.getBranches().size());
        assertEquals(TestUtils.BRANCH_NAME+"(title)", foundationBean.getBranchSummaries().get(0).getTitle());
        JSONObject requestData = new JSONObject();
        requestData.put("title", "My new branch");
        post(requestData);
        assertEquals(2, foundationBean.getBranches().size());
    }
    
    public void testAddWorkflowToBranch() throws Exception{
        testAddBranchWebScript();
        
        
    }
    
    private JSONObject post(JSONObject data) throws IOException, JSONException {
        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest("/foundation/branch", data.toString(), "application/json");
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, TestUtils.ADMIN_USER);
        return new JSONObject(response.getContentAsString());
    }
    
}
