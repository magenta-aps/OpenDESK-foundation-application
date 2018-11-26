/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.patches.InitialStructure;
import static dk.opendesk.foundationapplication.Utilities.*;
import static dk.opendesk.foundationapplication.patches.InitialStructure.DICTIONARY_PATH;
import static dk.opendesk.foundationapplication.patches.InitialStructure.FOUNDATION_TAG;
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

/**
 *
 * @author martin
 */
public class WorkflowTest extends BaseWebScriptTest {

    private ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean("ServiceRegistry");

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

        List<NodeRef> branchRefs = serviceRegistry.getSearchService().selectNodes(dataNode, "./odf:" + TestUtils.BRANCH_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, branchRefs.size());

        List<NodeRef> budgetRefs = serviceRegistry.getSearchService().selectNodes(dataNode, "./odf:" + TestUtils.BUDGET_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, budgetRefs.size());

        List<NodeRef> workflowRefs = serviceRegistry.getSearchService().selectNodes(dataNode, "./odf:" + TestUtils.WORKFLOW_NAME, null, serviceRegistry.getNamespaceService(), false);
        assertEquals(1, workflowRefs.size());

        List<ChildAssociationRef> childrenRefs = serviceRegistry.getNodeService().getChildAssocs(dataNode);
        assertEquals(3, childrenRefs.size());
    }

    public void testApplicationFlow() throws Exception {
        String APPLICATION_NAME = "App1";
        
        NodeRef dataNode = getDataDictionaryRef();
        assertNotNull("Data node should have been bootstrapped", dataNode);

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(APPLICATION_PARAM_TITLE), "NewApplication");
        properties.put(getODFName(APPLICATION_PARAM_CATEGORY), "Category1");
        properties.put(getODFName(APPLICATION_PARAM_RECIPIENT), "Dansk Dræbersnegls Bevaringsforbund");
        properties.put(getODFName(APPLICATION_PARAM_ADDR_ROAD), "Sneglestien");
        properties.put(getODFName(APPLICATION_PARAM_ADDR_NUMBER), 3);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_FLOOR), "2");
        properties.put(getODFName(APPLICATION_PARAM_ARRD_POSTALCODE), "1445");
        properties.put(getODFName(APPLICATION_PARAM_PERSON_FIRSTNAME), "Svend");
        properties.put(getODFName(APPLICATION_PARAM_PERSON_SURNAME), "Svendsen");
        properties.put(getODFName(APPLICATION_PARAM_PERSON_EMAIL), "ikkedraebesneglen@gmail.com");
        properties.put(getODFName(APPLICATION_PARAM_PERSON_PHONE), "12345678");
        properties.put(getODFName(APPLICATION_PARAM_SHORT_DESCRIPTION), "Vi ønsker at undgå flere unødvendige drab af dræbersnegle, samt at ophøje den til Danmarks nationaldyr.");
        properties.put(getODFName(APPLICATION_PARAM_START_DATE), Date.from(Instant.now()));
        properties.put(getODFName(APPLICATION_PARAM_END_DATE), Date.from(Instant.now().plus(Duration.ofDays(2))));
        properties.put(getODFName(APPLICATION_PARAM_APPLIED_AMOUNT), "10000000");
        properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_REGISTRATION), "1234");
        properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_NUMBER), "00123456");

        QName applicationTypeQname = getODFName(APPLICATION_TYPE_NAME);
        QName applicationQname = getODFName(APPLICATION_NAME);
        QName branchAssocApplication = getODFName(BRANCH_ASSOC_APPLICATIONS);

        serviceRegistry.getNodeService().createNode(getBranchRef(), branchAssocApplication, applicationQname, applicationTypeQname, properties);
        
        
        
        
        
        List<NodeRef> applications = serviceRegistry.getSearchService().selectNodes(getBranchRef(), "./odf:" + APPLICATION_NAME, null, serviceRegistry.getNamespaceService(), false);
        
        
        
        List<AssociationRef> branches = serviceRegistry.getNodeService().getTargetAssocs(getBranchRef(), getODFName(BRANCH_ASSOC_BUDGETS));
        
        serviceRegistry.getNodeService().createAssociation(applications.get(0), branches.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_BUDGET));

        
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

}
