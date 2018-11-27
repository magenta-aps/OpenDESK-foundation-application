/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.beans.FoundationBean;
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
    
    
    public static final String WORKFLOW_NAME = "defaultWorkFlow";
    
    public static final String BRANCH_NAME = "defaultBranch";
    
    public static final String STATE_RECIEVED_NAME = "recieved";
    public static final String STATE_ASSESS_NAME = "assesment";
    public static final String STATE_DENIED_NAME = "denied";
    public static final String STATE_ACCEPTED_NAME = "accepted";
    
    public static final String BUDGET_NAME = "defaultBudget";
    
    private TestUtils(){};
    
    public static void wipeData(ServiceRegistry serviceRegistry) throws Exception{
        NodeService nodeService = serviceRegistry.getNodeService();
        
        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);
        NodeRef dataRef = foundationBean.getDataHome();
        
        
        for(NodeRef workflow : foundationBean.getWorkflows()){
            nodeService.removeChild(dataRef, workflow);
        }
        
        for(NodeRef budget : foundationBean.getBudgets()){
            nodeService.removeChild(dataRef, budget);
        }
        
        for(NodeRef branch : foundationBean.getBranches()){
            nodeService.removeChild(dataRef, branch);
        }
        
        
        
    }
    
    public static void setupSimpleFlow(ServiceRegistry serviceRegistry) throws Exception{
        FoundationBean foundationBean = new FoundationBean();
        foundationBean.setServiceRegistry(serviceRegistry);
        
        
        //Create workflow
        QName workflowTitle = getODFName(WORKFLOW_PARAM_TITLE);
        Map<QName, Serializable> workflowParams = new HashMap<>();
        workflowParams.put(workflowTitle, WORKFLOW_NAME+"(title)");
        NodeRef workFlowRef = foundationBean.addNewWorkflow(WORKFLOW_NAME, WORKFLOW_NAME+"(title)");
        
        //Create workflow states
        NodeRef stateRecievedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_RECIEVED_NAME, STATE_RECIEVED_NAME+"(Title)");
        NodeRef stateAccessRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ASSESS_NAME, STATE_ASSESS_NAME+"(Title)");
        NodeRef stateDeniedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_DENIED_NAME, STATE_DENIED_NAME+"(Title)");
        NodeRef stateAcceptedRef = foundationBean.addNewWorkflowState(workFlowRef, STATE_ACCEPTED_NAME, STATE_ACCEPTED_NAME+"(Title)");
         
        //Create associations
        
        foundationBean.createWorkflowTransition(stateRecievedRef, stateAccessRef);
        foundationBean.createWorkflowTransition(stateRecievedRef, stateDeniedRef);
        
        foundationBean.createWorkflowTransition(stateAccessRef, stateAcceptedRef);
        foundationBean.createWorkflowTransition(stateAccessRef, stateDeniedRef);
    
        //Create branch and associate it with the workflow
        QName branchTitle = getODFName(BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, BRANCH_NAME+"(title)");
        NodeRef branchRef = foundationBean.addNewBranch(BRANCH_NAME, BRANCH_NAME+"(title)");
        foundationBean.setBranchWorkflow(branchRef, workFlowRef);
        
        //Create budget and associate it with a branch
        QName budgetTitle = getODFName(BUDGET_PARAM_TITLE);
        QName budgetamount = getODFName(BUDGET_PARAM_AMOUNT);
        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(budgetTitle, BUDGET_NAME+"(title)");
        budgetParams.put(budgetamount, 1000000000000000l);
        NodeRef budgetRef = foundationBean.addNewBudget(BUDGET_NAME, BUDGET_NAME+"(title)", 1000000000000000l);
        foundationBean.setBranchBudget(branchRef, budgetRef);
        
    }
    
}