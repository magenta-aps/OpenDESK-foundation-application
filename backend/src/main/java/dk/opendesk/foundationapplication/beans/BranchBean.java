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
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.Utilities;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class BranchBean extends FoundationBean {
    private BudgetBean budgetBean;
    private ApplicationBean applicationBean;

    public void setBudgetBean(BudgetBean budgetBean) {
        this.budgetBean = budgetBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public NodeRef addNewBranch(String localName, String title) throws Exception {
        NodeRef dataHome = getDataHome();
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        QName branchTypeQname = getODFName(BRANCH_TYPE_NAME);
        QName branchQname = getODFName(localName);
        QName branchTitle = getODFName(BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, title);

        return getServiceRegistry().getNodeService().createNode(dataHome, dataBranchesQname, branchQname, branchTypeQname, branchParams).getChildRef();
    }

    public AssociationRef addBranchBudget(NodeRef branchRef, NodeRef budgetRef) throws Exception {
        QName branchBudgetsQname = getODFName(BRANCH_ASSOC_BUDGETS);
        return getServiceRegistry().getNodeService().createAssociation(branchRef, budgetRef, branchBudgetsQname);
    }
    
    public AssociationRef addBranchWorkflow(NodeRef branchRef, NodeRef workflowRef) throws Exception {
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        return getServiceRegistry().getNodeService().createAssociation(branchRef, workflowRef, branchWorkflowQname);
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
        NodeService ns = getServiceRegistry().getNodeService();
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
            ns.setAssociations(branch.asNodeRef(), getODFName(BRANCH_ASSOC_WORKFLOW), Collections.singletonList(branch.getWorkflowRef().asNodeRef()));
        }
    }


}
