/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.repo.beans.PersonBean;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class FoundationBean {

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public NodeRef getDataHome() {
        return Utilities.getDataNode(serviceRegistry);
    }

    public NodeRef addNewApplication(NodeRef branchRef, String localName, String title, String category, String recipient, String addressRoad, Integer addressNumber, String addressFloor,
            String addressPostalCode, String contactFirstName, String contactLastName, String contactEmail, String contactPhone, String shortDescription,
            Date startDate, Date endDate, Long appliedAmount, String accountRegistration, String accountNumber) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(APPLICATION_PARAM_TITLE), title);
        properties.put(getODFName(APPLICATION_PARAM_CATEGORY), category);
        properties.put(getODFName(APPLICATION_PARAM_RECIPIENT), recipient);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_ROAD), addressRoad);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_NUMBER), addressNumber);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_FLOOR), addressFloor);
        properties.put(getODFName(APPLICATION_PARAM_ARRD_POSTALCODE), addressPostalCode);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_FIRSTNAME), contactFirstName);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_SURNAME), contactLastName);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_EMAIL), contactEmail);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_PHONE), contactPhone);
        properties.put(getODFName(APPLICATION_PARAM_SHORT_DESCRIPTION), shortDescription);
        properties.put(getODFName(APPLICATION_PARAM_START_DATE), startDate);
        properties.put(getODFName(APPLICATION_PARAM_END_DATE), endDate);
        properties.put(getODFName(APPLICATION_PARAM_APPLIED_AMOUNT), appliedAmount);
        properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_REGISTRATION), accountRegistration);
        properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_NUMBER), accountNumber);

        QName applicationTypeQname = getODFName(APPLICATION_TYPE_NAME);
        QName applicationQname = getODFName(localName);
        QName branchAssocApplication = getODFName(BRANCH_ASSOC_APPLICATIONS);

        return serviceRegistry.getNodeService().createNode(branchRef, branchAssocApplication, applicationQname, applicationTypeQname, properties).getChildRef();
    }

    public NodeRef addNewBudget(String localName, String title, Long amount) throws Exception {
        QName dataBudgetsQname = getODFName(DATA_ASSOC_BUDGETS);
        QName budgetTypeQname = getODFName(BUDGET_TYPE_NAME);
        QName budgetQname = getODFName(localName);
        NodeRef dataHome = getDataHome();

        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(getODFName(BUDGET_PARAM_TITLE), title);
        budgetParams.put(getODFName(BUDGET_PARAM_AMOUNT), amount);

        return serviceRegistry.getNodeService().createNode(dataHome, dataBudgetsQname, budgetQname, budgetTypeQname, budgetParams).getChildRef();
    }

    public NodeRef addNewBranch(String localName, String title) throws Exception {
        NodeRef dataHome = getDataHome();
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        QName branchTypeQname = getODFName(BRANCH_TYPE_NAME);
        QName branchQname = getODFName(localName);
        QName branchTitle = getODFName(BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, title);

        return serviceRegistry.getNodeService().createNode(dataHome, dataBranchesQname, branchQname, branchTypeQname, branchParams).getChildRef();
    }

    public NodeRef addNewWorkflow(String localName, String title) throws Exception {
        NodeRef dataHome = getDataHome();
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        QName workFlowTypeQname = getODFName(WORKFLOW_TYPE_NAME);
        QName workFlowQname = getODFName(localName);

        QName workflowTitle = getODFName(WORKFLOW_PARAM_TITLE);
        Map<QName, Serializable> workflowParams = new HashMap<>();
        workflowParams.put(workflowTitle, title);

        return serviceRegistry.getNodeService().createNode(dataHome, dataWorkflowsQname, workFlowQname, workFlowTypeQname, workflowParams).getChildRef();
    }
    
    public NodeRef addNewWorkflowState(NodeRef workFlowRef, String localName, String title) throws Exception{
        QName workFlowStatesQname = getODFName(WORKFLOW_ASSOC_STATES);
        QName stateTypeQname = getODFName(STATE_TYPE_NAME);
        QName stateQName = getODFName(localName);
        QName stateTitle = getODFName(STATE_PARAM_TITLE);
        Map<QName, Serializable> stateParams = new HashMap<>();
        stateParams.put(stateTitle, title);

        return serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateQName, stateTypeQname, stateParams).getChildRef();
    }
    
    public AssociationRef createWorkflowTransition(NodeRef stateFrom, NodeRef stateTo) throws Exception{
        QName stateTransitionsQname = getODFName(STATE_ASSOC_TRANSITIONS);
        return serviceRegistry.getNodeService().createAssociation(stateFrom, stateTo, stateTransitionsQname);
    }
    
    public AssociationRef setBranchWorkflow(NodeRef branchRef, NodeRef workflowRef) throws Exception{
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        return serviceRegistry.getNodeService().createAssociation(branchRef, workflowRef, branchWorkflowQname);
    }
    
    public AssociationRef setBranchBudget(NodeRef branchRef, NodeRef budgetRef) throws Exception{
        QName branchBudgetsQname = getODFName(BRANCH_ASSOC_BUDGETS);
        return serviceRegistry.getNodeService().createAssociation(branchRef, budgetRef, branchBudgetsQname);
    }

    public List<NodeRef> getWorkflows() throws Exception {
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> workflowAssocs = serviceRegistry.getNodeService().getChildAssocs(dataHome, dataWorkflowsQname, null);
        List<NodeRef> workflows = new ArrayList<>(workflowAssocs.size());
        for (ChildAssociationRef ref : workflowAssocs) {
            workflows.add(ref.getChildRef());
        }
        return workflows;
    }

    public List<NodeRef> getBudgets() throws Exception {
        QName dataBudgetsQname = getODFName(DATA_ASSOC_BUDGETS);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> budgetAssocs = serviceRegistry.getNodeService().getChildAssocs(dataHome, dataBudgetsQname, null);
        List<NodeRef> budgets = new ArrayList<>(budgetAssocs.size());
        for (ChildAssociationRef ref : budgetAssocs) {
            budgets.add(ref.getChildRef());
        }
        return budgets;
    }

    public List<NodeRef> getBranches() throws Exception {
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> branchAssocs = serviceRegistry.getNodeService().getChildAssocs(dataHome, dataBranchesQname, null);
        List<NodeRef> branches = new ArrayList<>(branchAssocs.size());
        for (ChildAssociationRef ref : branchAssocs) {
            branches.add(ref.getChildRef());
        }
        return branches;
    }

}
