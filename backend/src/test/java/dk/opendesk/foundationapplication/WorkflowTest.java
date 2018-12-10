/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.patches.InitialStructure;
import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import static dk.opendesk.foundationapplication.patches.InitialStructure.DICTIONARY_PATH;
import static dk.opendesk.foundationapplication.patches.InitialStructure.FOUNDATION_TAG;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;

/**
 *
 * @author martin
 */
public class WorkflowTest extends BaseWebScriptTest {

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

    public void testBasicStructure() throws Exception {
        NodeRef dataNode = getDataDictionaryRef();
        assertNotNull("Data node should have been bootstrapped", dataNode);

        //Exactly one branch has been created
        List<NodeRef> branchRefs = serviceRegistry.getSearchService().selectNodes(dataNode, "./odf:" + TestUtils.BRANCH_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, branchRefs.size());

        //Exactly one branch has been created
        List<NodeRef> budgetRefs = serviceRegistry.getSearchService().selectNodes(dataNode, "./odf:" + TestUtils.BUDGET_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, budgetRefs.size());

        //Exactly one branch has been created
        List<NodeRef> workflowRefs = serviceRegistry.getSearchService().selectNodes(dataNode, "./odf:" + TestUtils.WORKFLOW_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, workflowRefs.size());

        //Exactly 3 branches items has been created in total
        List<ChildAssociationRef> childrenRefs = serviceRegistry.getNodeService().getChildAssocs(dataNode);
        assertEquals(3, childrenRefs.size());
    }

    public void testCreateApplication() throws Exception {
        String APPLICATION_NAME = "App1";

        NodeRef dataNode = getDataDictionaryRef();
        assertNotNull("Data node should have been bootstrapped", dataNode);

        List<AssociationRef> branchBudgets = serviceRegistry.getNodeService().getTargetAssocs(getBranchRef(), getODFName(BRANCH_ASSOC_BUDGETS));

        NodeRef budgetRef = branchBudgets.get(0).getTargetRef();

        assertEquals(TestUtils.BUDGET_AMOUNT, foundationBean.getBudgetRemainingFunding(budgetRef));

        Long budgetAllocatedFunding = foundationBean.getBudgetAllocatedFunding(dataNode);
        Long budgetRemaningFunding = foundationBean.getBudgetRemainingFunding(budgetRef);
        Long budgetTotalFunding = foundationBean.getBudgetTotalFunding(budgetRef);

        assertEquals(Long.valueOf(0), budgetAllocatedFunding);
        assertEquals(budgetTotalFunding, budgetRemaningFunding);

        Long appliedAmount = 10000000l;

        foundationBean.addNewApplication(getBranchRef(), budgetRef, APPLICATION_NAME, "NewApplication", "Category1", "Dansk Dræbersnegls Bevaringsforbund", "Sneglesporet", 3, "2", "1445", "Svend", "Svendsen", "ikkedraebesneglen@gmail.com", "12345678",
                "Vi ønsker at undgå flere unødvendige drab af dræbersnegle, samt at ophøje den til Danmarks nationaldyr.", Date.from(Instant.now()), Date.from(Instant.now().plus(Duration.ofDays(2))), appliedAmount, "1234", "00123456");

        Long newBalance = TestUtils.BUDGET_AMOUNT - appliedAmount;
        assertEquals(newBalance, foundationBean.getBudgetRemainingFunding(budgetRef));
        assertEquals(appliedAmount, foundationBean.getBudgetAllocatedFunding(budgetRef));
        //Did we create an application with the expected name in the branch?
        List<NodeRef> applications = serviceRegistry.getSearchService().selectNodes(getBranchRef(), "./odf:" + APPLICATION_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, applications.size());

    }

    protected NodeRef getDataDictionaryRef() {
        StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        NodeRef rootRef = serviceRegistry.getNodeService().getRootNode(store);

        List<NodeRef> refs = serviceRegistry.getSearchService().selectNodes(rootRef, InitialStructure.DATA_PATH, null, serviceRegistry.getNamespaceService(), false);
        if (refs.size() != 1) {
            throw new AlfrescoRuntimeException("Failed to apply patch: Returned multiple refs for " + DICTIONARY_PATH);
        }

        //Data node is present
        return refs.get(0);
    }

    protected NodeRef getBranchRef() {
        StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        NodeRef rootRef = serviceRegistry.getNodeService().getRootNode(store);

        List<NodeRef> refs = serviceRegistry.getSearchService().selectNodes(rootRef, InitialStructure.DATA_PATH + "/" + FOUNDATION_TAG + ":" + TestUtils.BRANCH_NAME, null, serviceRegistry.getNamespaceService(), false);
        if (refs.size() != 1) {
            throw new AlfrescoRuntimeException("Failed to apply patch: Returned multiple refs for " + DICTIONARY_PATH);
        }

        //Data node is present
        return refs.get(0);
    }

    private JSONArray executeWebScript(JSONObject data) throws IOException, JSONException {
        TestWebScriptServer.Request request = new TestWebScriptServer.PostRequest("/notifications", data.toString(), "application/json");
        TestWebScriptServer.Response response = sendRequest(request, Status.STATUS_OK, TestUtils.ADMIN_USER);
        return new JSONArray(response.getContentAsString());
    }

}