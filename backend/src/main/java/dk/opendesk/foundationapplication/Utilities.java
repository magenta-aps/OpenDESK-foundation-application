/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.patches.InitialStructure;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.w3c.dom.Document;

/**
 *
 * @author martin
 */
public final class Utilities {
    private Utilities(){};
    
    public static final String FOUNDATION_MODEL_LOCATION="/alfresco/module/foundationapplication/model/foundation-model.xml";
    
    public static final String DATA_TYPE_NAME = "data";
    public static final String DATA_ASSOC_WORKFLOW = "workflows";
    public static final String DATA_ASSOC_BRANCHES = "branches";
    public static final String DATA_ASSOC_BUDGETS = "budgets";
    
    public static final String BRANCH_TYPE_NAME = "applicationBranch";
    public static final String BRANCH_ASSOC_APPLICATIONS = "applications"; 
    public static final String BRANCH_ASSOC_WORKFLOW = "branchWorkflow";
    public static final String BRANCH_ASSOC_BUDGETS = "branchBudgets";
    public static final String BRANCH_PARAM_TITLE = "branchTitle";
    
    public static final String WORKFLOW_TYPE_NAME = "applicationWorkflow";
    public static final String WORKFLOW_ASSOC_STATES = "workflowStates";
    public static final String WORKFLOW_PARAM_TITLE = "workflowTitle";
    
    public static final String STATE_TYPE_NAME = "applicationState";
    public static final String STATE_ASSOC_TRANSITIONS = "workflowTransitions";
    
    public static final String BUDGET_TYPE_NAME = "applicationBudget";
    public static final String BUDGET_PARAM_TITLE="budgetTitle";
    public static final String BUDGET_PARAM_AMOUNT="budgetAmountAvailable";
    
    
    
    
    public static String getFoundationModelNameSpace() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(InitialStructure.class.getResourceAsStream(FOUNDATION_MODEL_LOCATION));

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();


        XPathExpression expr = xpath.compile("string(/model/namespaces/namespace[@prefix=\"odf\"]/@uri)");
        String namespace = (String)expr.evaluate(doc, XPathConstants.STRING);
        return namespace;
    }
    
    public static NodeRef getDataNode(ServiceRegistry sr){
        return getDataNode(sr.getNodeService(), sr.getSearchService(), sr.getNamespaceService());
    }
     
    
    public static NodeRef getDataNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService){
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        List<NodeRef> refs = searchService.selectNodes(rootRef, InitialStructure.DATA_PATH, null, namespaceService, false);
        if (refs.size() != 1) {
            throw new AlfrescoRuntimeException("Failed to create structure: Returned multiple refs for " + InitialStructure.DATA_PATH);
        }
        
        return refs.get(0);
    }
    
    
    
    
    
}
