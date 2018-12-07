/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

    public NodeRef addNewApplication(NodeRef branchRef, NodeRef budgetRef, String localName, String title, String category, String recipient, String addressRoad, Integer addressNumber, String addressFloor,
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

        NodeRef applicationRef = serviceRegistry.getNodeService().createNode(branchRef, branchAssocApplication, applicationQname, applicationTypeQname, properties).getChildRef();
        serviceRegistry.getNodeService().createAssociation(applicationRef, budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        
        NodeRef workFlowRef = serviceRegistry.getNodeService().getTargetAssocs(branchRef, getODFName(BRANCH_ASSOC_WORKFLOW)).get(0).getTargetRef();
        List<AssociationRef> workflowEntryRefs = serviceRegistry.getNodeService().getTargetAssocs(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY));
        if(workflowEntryRefs.size() != 1){
             throw new AlfrescoRuntimeException("Cannot create new application. The workflow on this branch does not have an entry point set");
        }
        
        serviceRegistry.getNodeService().createAssociation(applicationRef, workflowEntryRefs.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_STATE));
        
        return applicationRef;
    }
    
    
    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception{
        return serviceRegistry.getNodeService().getTargetAssocs(applicationRef, getODFName(APPLICATION_ASSOC_BUDGET)).get(0).getTargetRef();
    }
    
    public NodeRef getApplicationState(NodeRef applicationRef) throws Exception{
        QName applicationStateName = getODFName(APPLICATION_ASSOC_STATE);
        List<AssociationRef> states = serviceRegistry.getNodeService().getTargetAssocs(applicationRef, applicationStateName);
        //The association is singular, it is never a list
        return states != null && !states.isEmpty() ? states.get(0).getTargetRef() : null;
    }   
    
//    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception{
//        QName applicationBudgetName = getODFName(APPLICATION_ASSOC_BUDGET);
//        List<AssociationRef> budgets = serviceRegistry.getNodeService().getTargetAssocs(applicationRef, applicationBudgetName);
//        //The association is singular, it is never a list
//        return budgets != null && !budgets.isEmpty() ? budgets.get(0).getTargetRef() : null;
//    }

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
    
    public Long getBudgetAllocatedFunding(NodeRef budgetRef) throws Exception{
        List<AssociationRef> refs = serviceRegistry.getNodeService().getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        long totalAllocatedFunding = 0;
        for(AssociationRef ref : refs){
            Long amount = (Long)serviceRegistry.getNodeService().getProperty(ref.getSourceRef(), getODFName(APPLICATION_PARAM_APPLIED_AMOUNT));
            totalAllocatedFunding =+ amount;
        }
        return totalAllocatedFunding;
    }
    
    public Long getBudgetTotalFunding(NodeRef budgetRef) throws Exception{
        Long totalAmount = (Long)serviceRegistry.getNodeService().getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT));
        return totalAmount;
    }
    
    public Long getBudgetRemainingFunding(NodeRef budgetRef) throws Exception{
        Long totalAmount = getBudgetTotalFunding(budgetRef);
        Long usedAmount = getBudgetAllocatedFunding(budgetRef);
        return totalAmount - usedAmount;
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
    
    public void setWorkflowEntryPoint(NodeRef workFlowRef, NodeRef workflowStateRef) throws Exception{
        serviceRegistry.getNodeService().setAssociations(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY), Collections.singletonList(workflowStateRef));
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
    
    public NodeRef getBranchWorkflow(NodeRef branchRef) throws Exception{
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        List<AssociationRef> workflows = serviceRegistry.getNodeService().getTargetAssocs(branchRef, branchWorkflowQname);
        //The workflow association is singular, it is never a list
        return workflows != null && !workflows.isEmpty() ? workflows.get(0).getTargetRef() : null;
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

    public List<NodeRef> getBudgetRefs() throws Exception {
        QName dataBudgetsQname = getODFName(DATA_ASSOC_BUDGETS);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> budgetAssocs = serviceRegistry.getNodeService().getChildAssocs(dataHome, dataBudgetsQname, null);
        List<NodeRef> budgets = new ArrayList<>(budgetAssocs.size());
        for (ChildAssociationRef ref : budgetAssocs) {
            budgets.add(ref.getChildRef());
        }
        return budgets;
    }
    
    public List<Budget> getBudgets() throws Exception {
        List<Budget> budgets = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for(NodeRef budgetRef : getBudgetRefs()){
            Budget budget = new Budget();
            budget.fromRef(budgetRef);
            budget.setTitle((String)ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
            budget.setAmount((Long)ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));
            budgets.add(budget);
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
    
    public List<BranchSummary> getBranchSummaries() throws Exception {
        List<NodeRef> refs = getBranches();
        List<BranchSummary> branches = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for(NodeRef branchRef : refs){
            BranchSummary summary = new BranchSummary();
            summary.setNodeRef(branchRef.getId());
            summary.setTitle((String)ns.getProperty(branchRef, getODFName(BRANCH_PARAM_TITLE)));
            NodeRef workflowRef = getBranchWorkflow(branchRef);
            if(workflowRef != null){
                summary.setWorkflowRef(workflowRef.getId());
                summary.setWorkflowTitle((String)ns.getProperty(workflowRef, getODFName(WORKFLOW_PARAM_TITLE)));
            }
            branches.add(summary);
            
        }
        
        return branches;
    }
    
    public List<ApplicationSummary> getBranchApplications(NodeRef branchRef) throws Exception{
        NodeService ns = serviceRegistry.getNodeService();
        List<AssociationRef> applicationRefs = serviceRegistry.getNodeService().getTargetAssocs(branchRef, getODFName(BRANCH_ASSOC_APPLICATIONS));
        List<ApplicationSummary> applications = new ArrayList<>();
        for(AssociationRef ref : applicationRefs){
            NodeRef appRef = ref.getTargetRef();
            ApplicationSummary app = new ApplicationSummary();
            app.fromRef(appRef);
            app.setTitle(getProperty(appRef, APPLICATION_PARAM_TITLE, String.class));
            app.setAmountApplied(getProperty(appRef, APPLICATION_PARAM_APPLIED_AMOUNT, Long.class));
            NodeRef budgetRef = getApplicationBudget(appRef);
            app.setBudgetRef(budgetRef);
            app.setBudgetTitle(getProperty(appRef, BUDGET_PARAM_TITLE, String.class));
            app.setCategory(getProperty(appRef, APPLICATION_PARAM_CATEGORY, String.class));
            app.setStartDate(getProperty(appRef, APPLICATION_PARAM_START_DATE, Date.class));
            app.setEndDate(getProperty(appRef, APPLICATION_PARAM_END_DATE, Date.class));
            app.setRecipient(getProperty(appRef, APPLICATION_PARAM_RECIPIENT, String.class));
            app.setShortDescription(getProperty(appRef, APPLICATION_PARAM_SHORT_DESCRIPTION, String.class));
            NodeRef stateRef = getApplicationState(appRef);
            app.setStateRef(stateRef);
            app.setStateTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
            applications.add(app);
        }
        return applications;
        
    }
    
    public void updateBudget(Budget budget) throws Exception{
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(BUDGET_PARAM_TITLE), budget.getTitle());
        properties.put(getODFName(BUDGET_PARAM_AMOUNT), budget.getAmount());
        ns.addProperties(budget.asRef(), properties);
    }
    
    public void updateBranch(Branch branch) throws Exception{
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(BRANCH_PARAM_TITLE), branch.getTitle());
        ns.addProperties(branch.asRef(), properties);
    }
    
    public <T> T getProperty(NodeRef ref, String name, Class<T> Type) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        return (T)ns.getProperty(ref, getODFName(name));
    }

}
