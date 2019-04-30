/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.State;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.StateSummary;
import dk.opendesk.foundationapplication.DAO.Workflow;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import dk.opendesk.foundationapplication.enums.StateCategory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class WorkflowBean extends FoundationBean {

    private ApplicationBean applicationBean;
    private AuthorityBean authBean;
    private BranchBean branchBean;

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setAuthBean(AuthorityBean authBean) {
        this.authBean = authBean;
    }

    public void setBranchBean(BranchBean branchBean) {
        this.branchBean = branchBean;
    }

    public WorkflowReference getWorkflowReference(NodeRef reference) throws Exception {
        ensureType(getODFName(WORKFLOW_TYPE_NAME), reference);

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

        NodeRef newWorkflow = getServiceRegistry().getNodeService().createNode(dataHome, dataWorkflowsQname, workFlowQname, workFlowTypeQname, workflowParams).getChildRef();
        authBean.addFullPermission(newWorkflow, PermissionGroup.WORKFLOW, newWorkflow);
        authBean.disableInheritPermissions(newWorkflow);
        return newWorkflow;
    }

    public void setWorkflowEntryPoint(NodeRef workFlowRef, NodeRef workflowStateRef) throws Exception {
        getServiceRegistry().getNodeService().setAssociations(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY), Collections.singletonList(workflowStateRef));
    }
    
    public NodeRef getWorkflowEntryPoint(NodeRef workflowRef) throws Exception{
        ensureType(getODFName(Utilities.WORKFLOW_TYPE_NAME), workflowRef);
        return getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY);
    }

    public NodeRef addNewWorkflowState(NodeRef workFlowRef, String localName, String title, StateCategory category) throws Exception {
        QName workFlowStatesQname = getODFName(WORKFLOW_ASSOC_STATES);
        QName stateTypeQname = getODFName(STATE_TYPE_NAME);
        QName stateQName = getODFName(localName);
        QName stateTitle = getODFName(STATE_PARAM_TITLE);
        QName stateCategory = getODFName(STATE_PARAM_CATEGORY);
        Map<QName, Serializable> stateParams = new HashMap<>();
        stateParams.put(stateTitle, title);
        if (category != null) {
            stateParams.put(stateCategory, category.getCategoryName());
        }
        return getServiceRegistry().getNodeService().createNode(workFlowRef, workFlowStatesQname, stateQName, stateTypeQname, stateParams).getChildRef();

        /*
        NodeRef newWorkflowState = serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateQName, stateTypeQname, stateParams).getChildRef();

        ChildAssociationRef createStateActionsNode = serviceRegistry.getNodeService().createNode(newWorkflowState, getODFName(STATE_ASSOC_ACTIONS), QName.createQName(CONTENT_NAME_SPACE,"createStateActionsNode"), QName.createQName(CONTENT_NAME_SPACE,"cmobject"));
        ChildAssociationRef deleteStateActionsNode = serviceRegistry.getNodeService().createNode(newWorkflowState, getODFName(STATE_ASSOC_ACTIONS), QName.createQName(CONTENT_NAME_SPACE,"deleteStateActionsNode"), QName.createQName(CONTENT_NAME_SPACE,"cmobject"));

        return newWorkflowState;
         */
    }

    public AssociationRef createWorkflowTransition(NodeRef stateFrom, NodeRef stateTo) throws Exception {
        QName stateTransitionsQname = getODFName(STATE_ASSOC_TRANSITIONS);
        return getServiceRegistry().getNodeService().createAssociation(stateFrom, stateTo, stateTransitionsQname);
    }

    public List<NodeRef> getWorkflows() throws Exception {
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> workflowAssocs = getServiceRegistry().getNodeService().getChildAssocs(dataHome, dataWorkflowsQname, null);
        List<NodeRef> workflows = new ArrayList<>(workflowAssocs.size());
        for (ChildAssociationRef ref : workflowAssocs) {
            try {
                workflows.add(ref.getChildRef());
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return workflows;
    }

    public List<WorkflowSummary> getWorkflowSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        List<WorkflowSummary> summaries = new ArrayList<>();
        for (ChildAssociationRef ref : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_WORKFLOW), null)) {
            try {
                summaries.add(getWorkflowSummary(ref.getChildRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return summaries;
    }

    public WorkflowSummary getWorkflowSummary(NodeRef workflowRef) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        WorkflowSummary summary = new WorkflowSummary();
        summary.parseRef(workflowRef);
        summary.setTitle(getProperty(workflowRef, WORKFLOW_PARAM_TITLE, String.class));
        NodeRef stateRef = getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY);
        if (stateRef != null) {
            try {
                summary.setEntry(getStateReference(stateRef));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }

        List<StateReference> stateReferences = new ArrayList<>();
        for (ChildAssociationRef state : ns.getChildAssocs(workflowRef, getODFName(WORKFLOW_ASSOC_STATES), null)) {
            try {
                stateReferences.add(getStateReference(state.getChildRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        summary.setStates(stateReferences);
        return summary;
    }

    public Workflow getWorkflow(NodeRef workflowRef) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        Workflow workflow = new Workflow();
        workflow.parseRef(workflowRef);
        workflow.setTitle(getProperty(workflowRef, WORKFLOW_PARAM_TITLE, String.class));

        try {
            NodeRef entryRef = getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY);
            workflow.setEntry(getStateReference(entryRef));
        } catch (AccessDeniedException ex) {
            //Skip the node and continue
        }
        List<StateSummary> states = new ArrayList<>();
        for (ChildAssociationRef stateRef : ns.getChildAssocs(workflowRef, getODFName(WORKFLOW_ASSOC_STATES), null)) {
            try {
                states.add(getStateSummary(stateRef.getChildRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        workflow.setStates(states);

        List<BranchReference> branches = new ArrayList<>();
        for (AssociationRef branchRef : ns.getSourceAssocs(workflowRef, getODFName(BRANCH_ASSOC_WORKFLOW))) {
            try {
                branches.add(branchBean.getBranchReference(branchRef.getSourceRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        workflow.setUsedByBranches(branches);

        return workflow;

    }

    public State getState(NodeRef stateRef) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        State state = new State();
        state.parseRef(stateRef);
        state.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        List<StateReference> transitions = new ArrayList<>();
        for (AssociationRef transitionRef : ns.getTargetAssocs(stateRef, getODFName(STATE_ASSOC_TRANSITIONS))) {
            try {
                transitions.add(getStateReference(transitionRef.getTargetRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        state.setReferences(transitions);
        List<ApplicationReference> applications = new ArrayList<>();
        for (AssociationRef applicationRef : ns.getSourceAssocs(stateRef, getODFName(APPLICATION_ASSOC_STATE))) {
            try {
                applications.add(applicationBean.getApplicationReference(applicationRef.getSourceRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        state.setApplications(applications);
        state.setCategory(StateCategory.getFromName(getProperty(stateRef, STATE_PARAM_CATEGORY, String.class)));
        return state;
    }

    public StateSummary getStateSummary(NodeRef stateRef) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        StateSummary summary = new StateSummary();
        summary.parseRef(stateRef);
        summary.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        List<StateReference> transitions = new ArrayList<>();
        for (AssociationRef transitionRef : ns.getTargetAssocs(stateRef, getODFName(STATE_ASSOC_TRANSITIONS))) {
            transitions.add(getStateReference(transitionRef.getTargetRef()));
        }
        summary.setReferences(transitions);
        summary.setCategory(StateCategory.getFromName(getProperty(stateRef, STATE_PARAM_CATEGORY, String.class)));
        return summary;
    }

    public StateReference getStateReference(NodeRef stateRef) throws Exception {
        StateReference reference = new StateReference();
        reference.parseRef(stateRef);
        reference.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        return reference;
    }
    
    /**
     * Returns the category of the specified state. Ignores access rights. (Runs as system)
     * @param stateRef
     * @return
     * @throws Exception
     */
    public StateCategory getStateCategoryByStateRef(NodeRef stateRef) throws Exception {
        ensureType(STATE_TYPE_NAME, stateRef);
        return AuthenticationUtil.runAsSystem(() -> StateCategory.getFromName(getProperty(stateRef, STATE_PARAM_CATEGORY, String.class)));    
    }
    public StateCategory getStateCategoryByApplicationRef(NodeRef applicationRef) throws Exception {
        ensureType(APPLICATION_TYPE_NAME, applicationRef);
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef stateRef = applicationBean.getApplicationState(applicationRef);
            if(stateRef == null){ //If state is null, category is null
                return null;
            }
            return StateCategory.getFromName(getProperty(stateRef, STATE_PARAM_CATEGORY, String.class));
        });    
    }

}
