/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.patches.InitialStructure;
import static dk.opendesk.foundationapplication.patches.InitialStructure.DICTIONARY_PATH;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
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
    private static Logger LOGGER = Logger.getLogger(TestUtils.class);
    private TestUtils(){};
    
    public static final String WORKFLOW_NAME = "defaultWorkFlow";
    
    public static final String BRANCH_NAME = "defaultBranch";
    
    public static final String STATE_RECIEVED_NAME = "recieved";
    public static final String STATE_ASSESS_NAME = "assesment";
    public static final String STATE_DENIED_NAME = "denied";
    public static final String STATE_ACCEPTED_NAME = "accepted";
    
    public static final String BUDGET_NAME = "defaultBudget";
    
    
    
    public static void wipeData(NodeService nodeService, SearchService searchService, NamespaceService namespaceService) throws Exception{
        NodeRef dataRef = getDataNode(nodeService, searchService, namespaceService);
        
        String foundationNamespace = getFoundationModelNameSpace();
        QName dataWorkflowsQname = QName.createQName(foundationNamespace, DATA_ASSOC_WORKFLOW);
        QName dataBudgetsQname = QName.createQName(foundationNamespace, DATA_ASSOC_BUDGETS);
        QName dataBranchesQname = QName.createQName(foundationNamespace, DATA_ASSOC_BRANCHES);
        
        List<AssociationRef> workFlows = nodeService.getTargetAssocs(dataRef, dataWorkflowsQname);
        LOGGER.info("Wiping "+workFlows.size()+" workflows");
        for(AssociationRef workflow : workFlows){
            nodeService.removeChild(dataRef, workflow.getTargetRef());
        }
        
        List<AssociationRef> budgets = nodeService.getTargetAssocs(dataRef, dataBudgetsQname);
        LOGGER.info("Wiping "+budgets.size()+" budgets");
        for(AssociationRef budget : budgets){
            nodeService.removeChild(dataRef, budget.getTargetRef());
        }
        
        List<AssociationRef> branches = nodeService.getTargetAssocs(dataRef, dataWorkflowsQname);
        LOGGER.info("Wiping "+branches.size()+" branches");
        for(AssociationRef branch : branches){
            nodeService.removeChild(dataRef, branch.getTargetRef());
        }
        
        
        
    }
    
    public static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception{
        setupSimpleFlow(serviceRegistry.getNodeService(), serviceRegistry.getSearchService(), serviceRegistry.getNamespaceService());
    }
    
    public static void setupSimpleFlow(NodeService nodeService, SearchService searchService, NamespaceService namespaceService) throws Exception{
        String foundationNamespace = getFoundationModelNameSpace();
        
//        QName dataTypeQname = QName.createQName(foundationNamespace, DATA_TYPE_NAME);
//        QName dataQname = QName.createQName(foundationNamespace, InitialStructure.DATA_NAME);
        QName dataWorkflowsQname = QName.createQName(foundationNamespace, DATA_ASSOC_WORKFLOW);
        QName dataBudgetsQname = QName.createQName(foundationNamespace, DATA_ASSOC_BUDGETS);
        QName dataBranchesQname = QName.createQName(foundationNamespace, DATA_ASSOC_BRANCHES);
        
        QName branchTypeQname = QName.createQName(foundationNamespace, BRANCH_TYPE_NAME);
        QName branchQname = QName.createQName(foundationNamespace, BRANCH_NAME);
        QName branchApplicationsQname = QName.createQName(foundationNamespace, BRANCH_ASSOC_APPLICATIONS);
        QName branchWorkflowQname = QName.createQName(foundationNamespace, BRANCH_ASSOC_WORKFLOW);
        QName branchBudgetsQname = QName.createQName(foundationNamespace, BRANCH_ASSOC_BUDGETS);
        
        QName workFlowTypeQname = QName.createQName(foundationNamespace, WORKFLOW_TYPE_NAME);
        QName workFlowQname = QName.createQName(foundationNamespace, WORKFLOW_NAME);
        QName workFlowStatesQname = QName.createQName(foundationNamespace, WORKFLOW_ASSOC_STATES);
        
        QName stateTypeQname = QName.createQName(foundationNamespace, STATE_TYPE_NAME);
        QName stateRecievedQname = QName.createQName(foundationNamespace, STATE_RECIEVED_NAME);
        QName stateAssessQname = QName.createQName(foundationNamespace, STATE_ASSESS_NAME);
        QName stateDeniedQname = QName.createQName(foundationNamespace, STATE_DENIED_NAME);
        QName stateAcceptedQname = QName.createQName(foundationNamespace, STATE_ACCEPTED_NAME);
        QName stateTransitionsQname = QName.createQName(foundationNamespace, STATE_ASSOC_TRANSITIONS);
        
        QName budgetTypeQname = QName.createQName(foundationNamespace, BUDGET_TYPE_NAME);
        QName budgetQname = QName.createQName(foundationNamespace, BUDGET_NAME);
        
        
        
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        List<NodeRef> refs = searchService.selectNodes(rootRef, InitialStructure.DATA_PATH, null, namespaceService, false);
        if (refs.size() != 1) {
            throw new AlfrescoRuntimeException("Failed to create structure: Returned multiple refs for " + InitialStructure.DATA_PATH);
        }
        
        
        NodeRef dataRef = getDataNode(nodeService, searchService, namespaceService);
        
        //Create workflow
        QName workflowTitle = QName.createQName(foundationNamespace, WORKFLOW_PARAM_TITLE);
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
        QName branchTitle = QName.createQName(foundationNamespace, BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, BRANCH_NAME+"(title)");
        NodeRef branchRef = nodeService.createNode(dataRef, dataBranchesQname, branchQname, branchTypeQname, branchParams).getChildRef();
        nodeService.createAssociation(branchRef, workFlowRef, branchWorkflowQname);
        
        //Create budget and associate it with a branch
        QName budgetTitle = QName.createQName(foundationNamespace, BUDGET_PARAM_TITLE);
        QName budgetamount = QName.createQName(foundationNamespace, BUDGET_PARAM_AMOUNT);
        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(budgetTitle, BUDGET_NAME+"(title)");
        budgetParams.put(budgetamount, 1000000000000000l);
        NodeRef budgetRef = nodeService.createNode(dataRef, dataBudgetsQname, budgetQname, budgetTypeQname, budgetParams).getChildRef();
        nodeService.createAssociation(branchRef, budgetRef, branchBudgetsQname);
        
    }
    
   
    
}
