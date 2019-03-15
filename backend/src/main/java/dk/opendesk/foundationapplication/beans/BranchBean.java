/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class BranchBean extends FoundationBean {
    private BudgetBean budgetBean;
    private ApplicationBean applicationBean;
    private AuthorityBean authBean;
    private WorkflowBean workflowBean;

    public void setBudgetBean(BudgetBean budgetBean) {
        this.budgetBean = budgetBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setAuthBean(AuthorityBean authBean) {
        this.authBean = authBean;
    }

    public void setWorkflowBean(WorkflowBean workflowBean) {
        this.workflowBean = workflowBean;
    }
    
    

    public NodeRef addNewBranch(String localName, String title) throws Exception {
        NodeRef dataHome = getDataHome();
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        QName branchTypeQname = getODFName(BRANCH_TYPE_NAME);
        QName branchQname = getODFName(localName);
        QName branchTitle = getODFName(BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, title);
        
        NodeRef newBranch = getServiceRegistry().getNodeService().createNode(dataHome, dataBranchesQname, branchQname, branchTypeQname, branchParams).getChildRef();
        authBean.addFullPermission(dataHome, PermissionGroup.BRANCH, title);
        authBean.disableInheritPermissions(newBranch);

        return newBranch;
    }
    
     public AssociationRef addBranchBudget(NodeRef branchRef, NodeRef budgetRef) throws Exception {
         ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);
         ensureType(getODFName(BUDGET_TYPE_NAME), budgetRef);
         return addBranchBudget(getBranchReference(branchRef), budgetBean.getBudgetReference(budgetRef));
     }

    public AssociationRef addBranchBudget(BranchReference branchRef, BudgetReference budgetRef) throws Exception {
        QName branchBudgetsQname = getODFName(BRANCH_ASSOC_BUDGETS);
        authBean.linkAuthorities(authBean.getGroup(PermissionGroup.BRANCH, branchRef, true), authBean.getGroup(PermissionGroup.BUDGET, budgetRef, false));
        authBean.linkAuthorities(authBean.getGroup(PermissionGroup.BRANCH, branchRef, false), authBean.getGroup(PermissionGroup.BUDGET, budgetRef, false));
        return getServiceRegistry().getNodeService().createAssociation(branchRef.asNodeRef(), budgetRef.asNodeRef(), branchBudgetsQname);
    }
    
    public NodeRef setBranchWorkflow(NodeRef branchRef, NodeRef workflowRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);
        ensureType(getODFName(WORKFLOW_TYPE_NAME), workflowRef);
        return setBranchWorkflow(getBranchReference(branchRef), workflowBean.getWorkflowReference(workflowRef));
    }
    
    public NodeRef setBranchWorkflow(BranchReference branchRef, WorkflowReference workflowRef) throws Exception {
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        BranchSummary currentBranch = getBranchSummary(branchRef.asNodeRef());
        if(currentBranch.getWorkflowRef() != null){
            authBean.unlinkAuthorities(authBean.getGroup(PermissionGroup.BRANCH, branchRef, true), authBean.getGroup(PermissionGroup.WORKFLOW, currentBranch.getWorkflowRef(), false));
            authBean.unlinkAuthorities(authBean.getGroup(PermissionGroup.BRANCH, branchRef, false), authBean.getGroup(PermissionGroup.WORKFLOW, currentBranch.getWorkflowRef(), false));
        }
        authBean.linkAuthorities(authBean.getGroup(PermissionGroup.BRANCH, branchRef, true), authBean.getGroup(PermissionGroup.WORKFLOW, workflowRef, false));
        authBean.linkAuthorities(authBean.getGroup(PermissionGroup.BRANCH, branchRef, false), authBean.getGroup(PermissionGroup.WORKFLOW, workflowRef, false));
        
        getServiceRegistry().getNodeService().setAssociations(branchRef.asNodeRef(), branchWorkflowQname, Collections.singletonList(workflowRef.asNodeRef()));
        return workflowRef.asNodeRef();
    }
    
     public NodeRef getBranchWorkflow(NodeRef branchRef) throws Exception {
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        List<AssociationRef> workflows = getServiceRegistry().getNodeService().getTargetAssocs(branchRef, branchWorkflowQname);
        //The workflow association is singular, it is never a list
        return workflows != null && !workflows.isEmpty() ? workflows.get(0).getTargetRef() : null;
    }
     
     public List<NodeRef> getBranches() throws Exception {
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> branchAssocs = getServiceRegistry().getNodeService().getChildAssocs(dataHome, dataBranchesQname, null);
        List<NodeRef> branches = new ArrayList<>(branchAssocs.size());
        for (ChildAssociationRef ref : branchAssocs) {
            branches.add(ref.getChildRef());
        }
        return branches;
    }

    public List<BranchSummary> getBranchSummaries() throws Exception {
        List<NodeRef> refs = getBranches();
        List<BranchSummary> branches = new ArrayList<>();
        for (NodeRef branchRef : refs) {
            branches.add(getBranchSummary(branchRef));
        }
        return branches;
    }

    public BranchReference getBranchReference(NodeRef branchRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);

        BranchReference branchReference = new BranchReference();
        branchReference.parseRef(branchRef);
        branchReference.setTitle(getProperty(branchRef, BRANCH_PARAM_TITLE, String.class));
        return branchReference;
    }

    public BranchSummary getBranchSummary(NodeRef branchRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);

        NodeService ns = getServiceRegistry().getNodeService();
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
        return summary;
    }

    public Branch getBranch(NodeRef branchRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);

        NodeService ns = getServiceRegistry().getNodeService();
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
            budgets.add(budgetBean.getBudgetReference(ref.getTargetRef()));
        }
        branch.setBudgets(budgets);
        return branch;
    }

    public List<ApplicationSummary> getBranchApplications(NodeRef branchRef) throws Exception {

        List<AssociationRef> applicationRefs = getServiceRegistry().getNodeService().getSourceAssocs(branchRef, getODFName(APPLICATION_ASSOC_BRANCH));
        List<ApplicationSummary> applications = new ArrayList<>();
        for (AssociationRef ref : applicationRefs) {
            NodeRef appRef = ref.getSourceRef();
            applications.add(applicationBean.getApplicationSummary(appRef));
        }
        return applications;

    }
    
    public void updateBranch(BranchSummary branch) throws Exception {//BranchSummary is intentional. We don't want to update applications on the branch in this method, because applications doesn't actually belong to branches.
        NodeService ns = getServiceRegistry().getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        if (branch.wasTitleSet()) {
            properties.put(getODFName(BRANCH_PARAM_TITLE), branch.getTitle());
        }
        ns.addProperties(branch.asNodeRef(), properties);
        if (branch.getWorkflowRef() != null) {
            setBranchWorkflow(branch, branch.getWorkflowRef());
        }
    }


}
