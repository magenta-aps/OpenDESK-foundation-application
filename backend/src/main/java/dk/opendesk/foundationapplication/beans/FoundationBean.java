/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.State;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.StateSummary;
import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
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

    public final String ONLY_ONE_REFERENCE = "odf.one.ref.requred";

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
        if (workflowEntryRefs.size() != 1) {
            throw new AlfrescoRuntimeException("Cannot create new application. The workflow on this branch does not have an entry point set");
        }

        serviceRegistry.getNodeService().createAssociation(applicationRef, workflowEntryRefs.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_STATE));

        return applicationRef;
    }

    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception {
        return serviceRegistry.getNodeService().getTargetAssocs(applicationRef, getODFName(APPLICATION_ASSOC_BUDGET)).get(0).getTargetRef();
    }

    public NodeRef getApplicationState(NodeRef applicationRef) throws Exception {
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

    public Long getBudgetAllocatedFunding(NodeRef budgetRef) throws Exception {
        List<AssociationRef> refs = serviceRegistry.getNodeService().getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        long totalAllocatedFunding = 0;
        for (AssociationRef ref : refs) {
            Long amount = (Long) serviceRegistry.getNodeService().getProperty(ref.getSourceRef(), getODFName(APPLICATION_PARAM_APPLIED_AMOUNT));
            totalAllocatedFunding = +amount;
        }
        return totalAllocatedFunding;
    }

    public Long getBudgetTotalFunding(NodeRef budgetRef) throws Exception {
        Long totalAmount = (Long) serviceRegistry.getNodeService().getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT));
        return totalAmount;
    }

    public Long getBudgetRemainingFunding(NodeRef budgetRef) throws Exception {
        Long totalAmount = getBudgetTotalFunding(budgetRef);
        Long usedAmount = getBudgetAllocatedFunding(budgetRef);
        return totalAmount - usedAmount;
    }

    public BudgetReference getBudgetReference(NodeRef budgetRef) throws Exception {
        BudgetReference ref = new BudgetReference();
        ref.parseRef(budgetRef);
        ref.setTitle(getProperty(budgetRef, BUDGET_PARAM_TITLE, String.class));
        return ref;
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

    public WorkflowReference getWorkflowReference(NodeRef reference) throws Exception {
        WorkflowReference ref = new WorkflowReference();
        ref.parseRef(reference);
        ref.setTitle(getProperty(reference, WORKFLOW_PARAM_TITLE, String.class));
        return ref;
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

    public void setWorkflowEntryPoint(NodeRef workFlowRef, NodeRef workflowStateRef) throws Exception {
        serviceRegistry.getNodeService().setAssociations(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY), Collections.singletonList(workflowStateRef));
    }

    public NodeRef addNewWorkflowState(NodeRef workFlowRef, String localName, String title) throws Exception {
        QName workFlowStatesQname = getODFName(WORKFLOW_ASSOC_STATES);
        QName stateTypeQname = getODFName(STATE_TYPE_NAME);
        QName stateQName = getODFName(localName);
        QName stateTitle = getODFName(STATE_PARAM_TITLE);
        Map<QName, Serializable> stateParams = new HashMap<>();
        stateParams.put(stateTitle, title);

        return serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateQName, stateTypeQname, stateParams).getChildRef();
    }

    public AssociationRef createWorkflowTransition(NodeRef stateFrom, NodeRef stateTo) throws Exception {
        QName stateTransitionsQname = getODFName(STATE_ASSOC_TRANSITIONS);
        return serviceRegistry.getNodeService().createAssociation(stateFrom, stateTo, stateTransitionsQname);
    }

    public AssociationRef setBranchWorkflow(NodeRef branchRef, NodeRef workflowRef) throws Exception {
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        return serviceRegistry.getNodeService().createAssociation(branchRef, workflowRef, branchWorkflowQname);
    }

    public AssociationRef setBranchBudget(NodeRef branchRef, NodeRef budgetRef) throws Exception {
        QName branchBudgetsQname = getODFName(BRANCH_ASSOC_BUDGETS);
        return serviceRegistry.getNodeService().createAssociation(branchRef, budgetRef, branchBudgetsQname);
    }

    public NodeRef getBranchWorkflow(NodeRef branchRef) throws Exception {
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
        for (NodeRef budgetRef : getBudgetRefs()) {
            Budget budget = new Budget();
            budget.parseRef(budgetRef);
            budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
            budget.setAmount((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));
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
        for (NodeRef branchRef : refs) {
            BranchSummary summary = new BranchSummary();
            summary.parseRef(branchRef);
            summary.setTitle((String) ns.getProperty(branchRef, getODFName(BRANCH_PARAM_TITLE)));
            NodeRef workflowRef = getBranchWorkflow(branchRef);
            if (workflowRef != null) {
                WorkflowReference workflow = new WorkflowReference();
                workflow.parseRef(workflowRef);
                workflow.setTitle((String) ns.getProperty(workflowRef, getODFName(WORKFLOW_PARAM_TITLE)));
                summary.setWorkflowRef(workflow);
            }
            branches.add(summary);

        }

        return branches;
    }

    public Branch getBranch(NodeRef branchRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        Branch branch = new Branch();
        branch.parseRef(branchRef);
        branch.setTitle(getProperty(branchRef, BRANCH_PARAM_TITLE, String.class));
        NodeRef workflowRef = getBranchWorkflow(branchRef);
        if (workflowRef != null) {
            WorkflowReference workflow = new WorkflowReference();
            workflow.parseRef(workflowRef);
            workflow.setTitle((String) ns.getProperty(workflowRef, getODFName(WORKFLOW_PARAM_TITLE)));
            branch.setWorkflowRef(workflow);
        }
        branch.setSummaries(getBranchApplications(branchRef));
        List<BudgetReference> budgets = new ArrayList<>();
        for (AssociationRef ref : ns.getTargetAssocs(branchRef, getODFName(BRANCH_ASSOC_BUDGETS))) {
            budgets.add(getBudgetReference(ref.getTargetRef()));
        }
        branch.setBudgets(budgets);
        return branch;
    }

    public List<ApplicationSummary> getBranchApplications(NodeRef branchRef) throws Exception {

        List<AssociationRef> applicationRefs = serviceRegistry.getNodeService().getTargetAssocs(branchRef, getODFName(BRANCH_ASSOC_APPLICATIONS));
        List<ApplicationSummary> applications = new ArrayList<>();
        for (AssociationRef ref : applicationRefs) {
            NodeRef appRef = ref.getTargetRef();
            applications.add(getApplicationSummary(appRef));

        }
        return applications;

    }

    public ApplicationReference getApplicationReference(NodeRef applicationRef) throws Exception {
        ApplicationReference reference = new ApplicationReference();
        reference.parseRef(applicationRef);
        reference.setTitle(getProperty(applicationRef, APPLICATION_PARAM_TITLE, String.class));
        return reference;
    }

    public ApplicationSummary getApplicationSummary(NodeRef applicationSummary) throws Exception {
        ApplicationSummary app = new ApplicationSummary();
        app.parseRef(applicationSummary);
        app.setTitle(getProperty(applicationSummary, APPLICATION_PARAM_TITLE, String.class));
        app.setAmountApplied(getProperty(applicationSummary, APPLICATION_PARAM_APPLIED_AMOUNT, Long.class));
        NodeRef budgetRef = getApplicationBudget(applicationSummary);
        app.setBudgetRef(budgetRef);
        app.setBudgetTitle(getProperty(applicationSummary, BUDGET_PARAM_TITLE, String.class));
        app.setCategory(getProperty(applicationSummary, APPLICATION_PARAM_CATEGORY, String.class));
        app.setStartDate(getProperty(applicationSummary, APPLICATION_PARAM_START_DATE, Date.class));
        app.setEndDate(getProperty(applicationSummary, APPLICATION_PARAM_END_DATE, Date.class));
        app.setRecipient(getProperty(applicationSummary, APPLICATION_PARAM_RECIPIENT, String.class));
        app.setShortDescription(getProperty(applicationSummary, APPLICATION_PARAM_SHORT_DESCRIPTION, String.class));
        NodeRef stateRef = getApplicationState(applicationSummary);
        app.setStateRef(stateRef);
        app.setStateTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        return app;
    }

    public void updateBudget(Budget budget) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(BUDGET_PARAM_TITLE), budget.getTitle());
        properties.put(getODFName(BUDGET_PARAM_AMOUNT), budget.getAmount());
        ns.addProperties(budget.asNodeRef(), properties);
    }

    public void updateBranch(BranchSummary branch) throws Exception {//BranchSummary is intentional. We don't want to update applications to the branch in this method.
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(BRANCH_PARAM_TITLE), branch.getTitle());
        ns.addProperties(branch.asNodeRef(), properties);
        if (branch.getWorkflowRef() != null) {
            ns.setAssociations(branch.asNodeRef(), getODFName(BRANCH_ASSOC_WORKFLOW), Collections.singletonList(branch.getWorkflowRef().asNodeRef()));
        }
    }

    public List<WorkflowSummary> getWorkflowSummaries() throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<WorkflowSummary> summaries = new ArrayList<>();
        for (ChildAssociationRef ref : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_WORKFLOW), null)) {
            summaries.add(getWorkflowSummary(ref.getChildRef()));
        }
        return summaries;
    }

    public WorkflowSummary getWorkflowSummary(NodeRef workflowRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        WorkflowSummary summary = new WorkflowSummary();
        summary.parseRef(workflowRef);
        summary.setTitle(getProperty(workflowRef, WORKFLOW_PARAM_TITLE, String.class));
        summary.setEntry(getStateReference(getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY)));
        List<StateReference> stateReferences = new ArrayList<>();
        for (AssociationRef state : ns.getTargetAssocs(workflowRef, getODFName(WORKFLOW_ASSOC_STATES))) {
            stateReferences.add(getStateReference(state.getTargetRef()));
        }
        summary.setStates(stateReferences);
        return summary;
    }

    public Workflow getWorkflow(NodeRef workflowRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        Workflow workflow = new Workflow();
        workflow.parseRef(workflowRef);
        workflow.setTitle(getProperty(workflowRef, WORKFLOW_PARAM_TITLE, String.class));

        NodeRef entryRef = getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY);
        workflow.setEntry(getStateSummary(entryRef));

        List<StateSummary> states = new ArrayList<>();
        for (AssociationRef stateRef : ns.getTargetAssocs(workflowRef, getODFName(WORKFLOW_ASSOC_STATES))) {
            states.add(getState(stateRef.getTargetRef()));
        }
        workflow.setStates(states);

        return workflow;

    }

    public State getState(NodeRef stateRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        State state = new State();
        state.parseRef(stateRef);
        state.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        List<StateReference> transitions = new ArrayList<>();
        for (AssociationRef transitionRef : ns.getTargetAssocs(stateRef, getODFName(STATE_ASSOC_TRANSITIONS))) {
            transitions.add(getStateReference(transitionRef.getTargetRef()));
        }
        state.setReferences(transitions);
        List<ApplicationReference> applications = new ArrayList<>();
        for (AssociationRef applicationRef : ns.getSourceAssocs(stateRef, getODFName(APPLICATION_ASSOC_STATE))) {
            applications.add(getApplicationReference(applicationRef.getTargetRef()));
        }
        state.setApplications(applications);
        return state;
    }

    public StateSummary getStateSummary(NodeRef stateRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        StateSummary summary = new StateSummary();
        summary.parseRef(stateRef);
        summary.setTitle(STATE_PARAM_TITLE);
        List<StateReference> transitions = new ArrayList<>();
        for (AssociationRef transitionRef : ns.getTargetAssocs(stateRef, getODFName(STATE_ASSOC_TRANSITIONS))) {
            transitions.add(getStateReference(transitionRef.getTargetRef()));
        }
        summary.setReferences(transitions);
        return summary;
    }

    public StateReference getStateReference(NodeRef stateRef) throws Exception {
        StateReference reference = new StateReference();
        reference.parseRef(stateRef);
        reference.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        return reference;
    }

    public <T> T getProperty(NodeRef ref, String name, Class<T> Type) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        return (T) ns.getProperty(ref, getODFName(name));
    }

    public NodeRef getSingleTargetAssoc(NodeRef sourceRef, String assocName) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<AssociationRef> refs = ns.getTargetAssocs(sourceRef, getODFName(assocName));
        if (refs != null && refs.size() > 1) {
            throw new AlfrescoRuntimeException(ONLY_ONE_REFERENCE);
        }
        if (refs != null && !refs.isEmpty()) {
            return refs.get(0).getTargetRef();
        } else {
            return null;
        }
    }

}
