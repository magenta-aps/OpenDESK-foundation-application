/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.patches.InitialStructure;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public final class TestUtils {
    private static final Logger LOGGER = Logger.getLogger(TestUtils.class);
    private TestUtils(){};
    
    public static final String WORKFLOW_NAME = "defaultWorkFlow";
    
    public static final String BRANCH_NAME = "defaultBranch";
    
    public static final String STATE_RECIEVED_NAME = "recieved";
    public static final String STATE_ASSESS_NAME = "assesment";
    public static final String STATE_DENIED_NAME = "denied";
    public static final String STATE_ACCEPTED_NAME = "accepted";
    
    public static final String BUDGET_NAME = "defaultBudget";
    
    public static void wipeData(ServiceRegistry serviceRegistry) throws Exception{
        wipeData(serviceRegistry.getNodeService(), serviceRegistry.getSearchService(), serviceRegistry.getNamespaceService());
    }
    
    public static void wipeData(NodeService nodeService, SearchService searchService, NamespaceService namespaceService) throws Exception{
        NodeRef dataRef = getDataNode(nodeService, searchService, namespaceService);
        
        //String foundationNamespace = getFoundationModelNameSpace();
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        QName dataBudgetsQname = getODFName(DATA_ASSOC_BUDGETS);
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        
        
        List<ChildAssociationRef> workFlows = nodeService.getChildAssocs(dataRef, dataWorkflowsQname, null);
        LOGGER.info("Wiping "+workFlows.size()+" workflows");
        for(ChildAssociationRef workflow : workFlows){
            nodeService.removeChild(dataRef, workflow.getChildRef());
        }
        
        List<ChildAssociationRef> budgets = nodeService.getChildAssocs(dataRef, dataBudgetsQname, null);
        LOGGER.info("Wiping "+budgets.size()+" budgets");
        for(ChildAssociationRef budget : budgets){
            nodeService.removeChild(dataRef, budget.getChildRef());
        }
        
        List<ChildAssociationRef> branches = nodeService.getChildAssocs(dataRef, dataBranchesQname, null);
        LOGGER.info("Wiping "+branches.size()+" branches");
        for(ChildAssociationRef branch : branches){
            nodeService.removeChild(dataRef, branch.getChildRef());
        }
        
        
        
    }
    
    public static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception{
        setupSimpleFlow(serviceRegistry.getNodeService(), serviceRegistry.getSearchService(), serviceRegistry.getNamespaceService());
    }
    
    public static void setupSimpleFlow(NodeService nodeService, SearchService searchService, NamespaceService namespaceService) throws Exception{
        
//        QName dataTypeQname = getODFName(DATA_TYPE_NAME);
//        QName dataQname = getODFName(InitialStructure.DATA_NAME);
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        QName dataBudgetsQname = getODFName(DATA_ASSOC_BUDGETS);
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        
        QName branchTypeQname = getODFName(BRANCH_TYPE_NAME);
        QName branchQname = getODFName(BRANCH_NAME);
        QName branchApplicationsQname = getODFName(BRANCH_ASSOC_APPLICATIONS);
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        QName branchBudgetsQname = getODFName(BRANCH_ASSOC_BUDGETS);
        
        QName workFlowTypeQname = getODFName(WORKFLOW_TYPE_NAME);
        QName workFlowQname = getODFName(WORKFLOW_NAME);
        QName workFlowStatesQname = getODFName(WORKFLOW_ASSOC_STATES);
        
        QName stateTypeQname = getODFName(STATE_TYPE_NAME);
        QName stateRecievedQname = getODFName(STATE_RECIEVED_NAME);
        QName stateAssessQname = getODFName(STATE_ASSESS_NAME);
        QName stateDeniedQname = getODFName(STATE_DENIED_NAME);
        QName stateAcceptedQname = getODFName(STATE_ACCEPTED_NAME);
        QName stateTransitionsQname = getODFName(STATE_ASSOC_TRANSITIONS);
        
        QName budgetTypeQname = getODFName(BUDGET_TYPE_NAME);
        QName budgetQname = getODFName(BUDGET_NAME);
        
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        List<NodeRef> refs = searchService.selectNodes(rootRef, InitialStructure.DATA_PATH, null, namespaceService, false);
        if (refs.size() != 1) {
            throw new AlfrescoRuntimeException("Failed to create structure: Returned multiple refs for " + InitialStructure.DATA_PATH);
        }
        
        
        NodeRef dataRef = getDataNode(nodeService, searchService, namespaceService);
        
        //Create workflow
        QName workflowTitle = getODFName(WORKFLOW_PARAM_TITLE);
        Map<QName, Serializable> workflowParams = new HashMap<>();
        workflowParams.put(workflowTitle, WORKFLOW_NAME+"(title)");
        NodeRef workFlowRef = nodeService.createNode(dataRef, dataWorkflowsQname, workFlowQname, workFlowTypeQname, workflowParams).getChildRef();
        
        //Create workflow states
        NodeRef stateRecievedRef = nodeService.createNode(workFlowRef, workFlowStatesQname, stateRecievedQname, stateTypeQname).getChildRef();
        NodeRef stateAccessRef = nodeService.createNode(workFlowRef, workFlowStatesQname, stateAssessQname, stateTypeQname).getChildRef();
        NodeRef stateDeniedRef = nodeService.createNode(workFlowRef, workFlowStatesQname, stateDeniedQname, stateTypeQname).getChildRef();
        NodeRef stateAcceptedRef = nodeService.createNode(workFlowRef, workFlowStatesQname, stateAcceptedQname, stateTypeQname).getChildRef();
        
        //Create associations
        nodeService.createAssociation(stateRecievedRef, stateAccessRef, stateTransitionsQname);
        nodeService.createAssociation(stateRecievedRef, stateDeniedRef, stateTransitionsQname);
        
        nodeService.createAssociation(stateAccessRef, stateAcceptedRef, stateTransitionsQname);
        nodeService.createAssociation(stateAccessRef, stateDeniedRef, stateTransitionsQname);
    
        //Create branch and associate it with the workflow
        QName branchTitle = getODFName(BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, BRANCH_NAME+"(title)");
        NodeRef branchRef = nodeService.createNode(dataRef, dataBranchesQname, branchQname, branchTypeQname, branchParams).getChildRef();
        nodeService.createAssociation(branchRef, workFlowRef, branchWorkflowQname);
        
        //Create budget and associate it with a branch
        QName budgetTitle = getODFName(BUDGET_PARAM_TITLE);
        QName budgetamount = getODFName(BUDGET_PARAM_AMOUNT);
        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(budgetTitle, BUDGET_NAME+"(title)");
        budgetParams.put(budgetamount, 1000000000000000l);
        NodeRef budgetRef = nodeService.createNode(dataRef, dataBudgetsQname, budgetQname, budgetTypeQname, budgetParams).getChildRef();
        nodeService.createAssociation(branchRef, budgetRef, branchBudgetsQname);
        
    }
    
}