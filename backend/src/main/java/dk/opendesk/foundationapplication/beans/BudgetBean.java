/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.BudgetSummary;
import dk.opendesk.foundationapplication.DAO.BudgetYear;
import dk.opendesk.foundationapplication.DAO.BudgetYearReference;
import dk.opendesk.foundationapplication.DAO.BudgetYearSummary;
import dk.opendesk.foundationapplication.DAO.State;
import static dk.opendesk.foundationapplication.Utilities.APPLICATION_ASSOC_BUDGET;
import static dk.opendesk.foundationapplication.Utilities.BUDGETYEAR_ASSOC_BUDGETS;
import static dk.opendesk.foundationapplication.Utilities.BUDGETYEAR_PARAM_ENDDATE;
import static dk.opendesk.foundationapplication.Utilities.BUDGETYEAR_PARAM_STARTDATE;
import static dk.opendesk.foundationapplication.Utilities.BUDGETYEAR_PARAM_TITLE;
import static dk.opendesk.foundationapplication.Utilities.BUDGETYEAR_TYPE_NAME;
import static dk.opendesk.foundationapplication.Utilities.BUDGET_PARAM_AMOUNT;
import static dk.opendesk.foundationapplication.Utilities.BUDGET_PARAM_TITLE;
import static dk.opendesk.foundationapplication.Utilities.BUDGET_TYPE_NAME;
import static dk.opendesk.foundationapplication.Utilities.DATA_ASSOC_BUDGETYEARS;
import static dk.opendesk.foundationapplication.Utilities.getODFName;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import dk.opendesk.foundationapplication.enums.StateCategory;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class BudgetBean extends FoundationBean {

    private ApplicationBean applicationBean;
    private AuthorityBean authBean;
    private WorkflowBean workflowBean;

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setAuthBean(AuthorityBean authBean) {
        this.authBean = authBean;
    }

    public void setWorkflowBean(WorkflowBean workflowBean) {
        this.workflowBean = workflowBean;
    }

    public List<BudgetYear> getCurrentBudgetYears() throws Exception {
        Instant now = Instant.now();
        List<BudgetYearSummary> budgetYears = getBudgetYearSummaries();
        List<BudgetYear> currentBudgets = new ArrayList<>();
        for (BudgetYearSummary budgetYear : budgetYears) {
            Instant budgetStartDate = budgetYear.getStartDate().toInstant();
            Instant budgetEndDate = budgetYear.getEndDate().toInstant();
            if (now.isAfter(budgetStartDate) && now.isBefore(budgetEndDate)) {
                try {
                    currentBudgets.add(getBudgetYear(budgetYear.asNodeRef()));
                } catch (AccessDeniedException ex) {
                    //Skip the node and continue
                }
            }
        }
        return currentBudgets;
    }

    public NodeRef addNewBudgetYear(String localName, String title, Date startDate, Date endDate) throws Exception {
        QName budgetYearsQname = getODFName(DATA_ASSOC_BUDGETYEARS);
        QName budgetYearTypeQname = getODFName(BUDGETYEAR_TYPE_NAME);
        QName budgetYearQname = getODFName(localName);

        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(getODFName(BUDGETYEAR_PARAM_TITLE), title);
        budgetParams.put(getODFName(BUDGETYEAR_PARAM_STARTDATE), startDate);
        budgetParams.put(getODFName(BUDGETYEAR_PARAM_ENDDATE), endDate);

        NodeRef newBudgetYear = getServiceRegistry().getNodeService().createNode(getDataHome(), budgetYearsQname, budgetYearQname, budgetYearTypeQname, budgetParams).getChildRef();
        authBean.addFullPermission(newBudgetYear, PermissionGroup.BUDGET_YEAR, newBudgetYear);
        authBean.disableInheritPermissions(newBudgetYear);

        return newBudgetYear;

    }

    public NodeRef addNewBudget(NodeRef budgetYear, String localName, String title, Long amount) throws Exception {
        QName budgetYearBudgetsQname = getODFName(BUDGETYEAR_ASSOC_BUDGETS);
        QName budgetTypeQname = getODFName(BUDGET_TYPE_NAME);
        QName budgetQname = getODFName(localName);

        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(getODFName(BUDGET_PARAM_TITLE), title);
        budgetParams.put(getODFName(BUDGET_PARAM_AMOUNT), amount);

        NodeRef newBudget = getServiceRegistry().getNodeService().createNode(budgetYear, budgetYearBudgetsQname, budgetQname, budgetTypeQname, budgetParams).getChildRef();
        authBean.addFullPermission(newBudget, PermissionGroup.BUDGET, newBudget);
        authBean.disableInheritPermissions(newBudget);

        return newBudget;

    }

    public BudgetReference getBudgetReference(NodeRef budgetRef) throws Exception {
        ensureType(getODFName(BUDGET_TYPE_NAME), budgetRef);

        BudgetReference ref = new BudgetReference();
        ref.parseRef(budgetRef);
        ref.setTitle(getProperty(budgetRef, BUDGET_PARAM_TITLE, String.class));
        return ref;
    }

    public List<NodeRef> getBudgetYearRefs() throws Exception {
        QName budgetYearsQName = getODFName(DATA_ASSOC_BUDGETYEARS);
        List<ChildAssociationRef> budgetAssocs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), budgetYearsQName, null);
        List<NodeRef> budgetYears = new ArrayList<>(budgetAssocs.size());
        for (ChildAssociationRef ref : budgetAssocs) {
            try {
                budgetYears.add(ref.getChildRef());
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return budgetYears;
    }

    public List<NodeRef> getBudgetRefs(NodeRef budgetYear) throws Exception {
        QName budgetYearBudgets = getODFName(BUDGETYEAR_ASSOC_BUDGETS);
        List<ChildAssociationRef> budgetAssocs = getServiceRegistry().getNodeService().getChildAssocs(budgetYear, budgetYearBudgets, null);
        List<NodeRef> budgets = new ArrayList<>(budgetAssocs.size());
        for (ChildAssociationRef ref : budgetAssocs) {
            try {
                budgets.add(ref.getChildRef());
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return budgets;
    }

    public List<BudgetYearSummary> getBudgetYearSummaries() throws Exception {
        List<BudgetYearSummary> summaries = new ArrayList<>();
        NodeService ns = getServiceRegistry().getNodeService();
        for (ChildAssociationRef budgetYear : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_BUDGETYEARS), null)) {
            try {
                summaries.add(getBudgetYearSummary(budgetYear.getChildRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return summaries;
    }

    public BudgetYearReference getBudgetYearReference(NodeRef budgetYearRef) throws Exception {
        ensureType(getODFName(BUDGETYEAR_TYPE_NAME), budgetYearRef);

        BudgetYearReference reference = new BudgetYearReference();
        reference.parseRef(budgetYearRef);
        reference.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        return reference;
    }

    public BudgetYearSummary getBudgetYearSummary(NodeRef budgetYearRef) throws Exception {
        ensureType(getODFName(BUDGETYEAR_TYPE_NAME), budgetYearRef);

        BudgetYearSummary summary = new BudgetYearSummary();
        summary.parseRef(budgetYearRef);
        summary.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        summary.setStartDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_STARTDATE, Date.class));
        summary.setEndDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_ENDDATE, Date.class));

        Long totalAmount = 0l;
        List<BudgetSummary> budgets = getBudgetSummaries(summary);
        for (BudgetSummary budget : budgets) {
            totalAmount += budget.getAmountTotal();
        }

        summary.setAmountTotal(totalAmount);
        return summary;
    }

    public BudgetYear getBudgetYear(NodeRef budgetYearRef) throws Exception {
        ensureType(getODFName(BUDGETYEAR_TYPE_NAME), budgetYearRef);

        NodeService ns = getServiceRegistry().getNodeService();

        BudgetYear budgetYear = new BudgetYear();
        budgetYear.parseRef(budgetYearRef);
        budgetYear.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        budgetYear.setStartDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_STARTDATE, Date.class));
        budgetYear.setEndDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_ENDDATE, Date.class));

        List<Budget> budgets = new ArrayList<>();

        for (ChildAssociationRef budgetRef : ns.getChildAssocs(budgetYearRef, getODFName(BUDGETYEAR_ASSOC_BUDGETS), null)) {
            budgets.add(getBudget(budgetRef.getChildRef()));
        }

        Long amountTotal = 0l;
        Long amountAccepted = 0l;
        Long amountNominated = 0l;
        Long amountAvailable = 0l;
        Long amountClosed = 0l;
        Long amountApplied = 0l;

        for (Budget budget : budgets) {
            amountTotal += budget.getAmountTotal();
            amountAccepted += budget.getAmountAccepted();
            amountNominated += budget.getAmountNominated();
            amountAvailable += budget.getAmountAvailable();
            amountClosed += budget.getAmountClosed();
            amountApplied += budget.getAmountApplied();
        }

        budgetYear.setAmountTotal(amountTotal);
        budgetYear.setAmountAccepted(amountAccepted);
        budgetYear.setAmountNominated(amountNominated);
        budgetYear.setAmountAvailable(amountAvailable);
        budgetYear.setAmountClosed(amountClosed);
        budgetYear.setAmountApplied(amountApplied);

        return budgetYear;
    }

    public Budget getBudget(NodeRef budgetRef) throws Exception {
        ensureType(getODFName(BUDGET_TYPE_NAME), budgetRef);

        NodeService ns = getServiceRegistry().getNodeService();
        Budget budget = new Budget();
        budget.parseRef(budgetRef);
        budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
        budget.setAmountTotal((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));

        Long amountAccepted = 0l;
        Long amountNominated = 0l;
        Long amountClosed = 0l;
        Long ammountApplied = 0l;
        List<ApplicationReference> applications = new ArrayList<>();
        List<AssociationRef> applicationRefs = ns.getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        for (AssociationRef applicationRef : applicationRefs) {
            ApplicationSummary application = applicationBean.getApplicationSummary(applicationRef.getSourceRef());
            ApplicationReference appRef = new ApplicationReference();
            appRef.setNodeRef(application.getNodeRef());
            appRef.setTitle(application.getTitle());
            applications.add(appRef);
            ApplicationFieldValue<Long> value = application.totalAmount();
            if (value == null) {
                continue;
            }
            Long applicationAmount = value.getValue();
            NodeRef applicationStateRef = applicationBean.getApplicationState(applicationRef.getSourceRef());
            if (applicationStateRef == null) {
                continue;
            }
            State state = workflowBean.getState(applicationStateRef);
            StateCategory category = state.getCategory();
            if (category == null) {
                ammountApplied += applicationAmount;
            } else {
                switch (category) {
                    case ACCEPTED:
                        amountAccepted += applicationAmount;
                        break;
                    case NOMINATED:
                        amountNominated += applicationAmount;
                        break;
                    case CLOSED:
                        amountClosed += applicationAmount;
                        break;
                }
            }

        }
        Long amountAvailable = budget.getAmountTotal() - amountAccepted - amountClosed;

        budget.setAmountAccepted(amountAccepted);
        budget.setAmountNominated(amountNominated);
        budget.setAmountClosed(amountClosed);
        budget.setAmountApplied(ammountApplied);
        budget.setAmountAvailable(amountAvailable);

        budget.setApplications(applications);

        return budget;
    }

    public List<BudgetSummary> getBudgetSummaries(BudgetYearReference budgetYear) throws Exception {

        List<BudgetSummary> summaries = new ArrayList<>();
        NodeService ns = getServiceRegistry().getNodeService();
        for (NodeRef budgetRef : getBudgetRefs(budgetYear.asNodeRef())) {
            BudgetSummary budget = new BudgetSummary();
            budget.parseRef(budgetRef);
            budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
            budget.setAmountTotal((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));
            summaries.add(budget);
        }
        return summaries;
    }

    public void updateBudget(Budget budget) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        if (budget.wasTitleSet()) {
            properties.put(getODFName(BUDGET_PARAM_TITLE), budget.getTitle());
        }
        if (budget.wasAmountTotalSet()) {
            properties.put(getODFName(BUDGET_PARAM_AMOUNT), budget.getAmountTotal());
        }
        ns.addProperties(budget.asNodeRef(), properties);
    }

}
