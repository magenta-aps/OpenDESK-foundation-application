/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author martin
 */
public class Branch extends BranchSummary{
    private List<BudgetReference> budgets = new ArrayList<>();
    private List<ApplicationSummary> summaries = new ArrayList<>();

    public Branch() {
    }

    public Branch(String title, String uuid, WorkflowReference workflowRef) {
        super(title, uuid, workflowRef);
    }

    public List<BudgetReference> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<BudgetReference> budgets) {
        this.budgets = budgets;
    }

    public List<ApplicationSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<ApplicationSummary> summaries) {
        this.summaries = summaries;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.budgets);
        hash = 79 * hash + Objects.hashCode(this.summaries);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Branch other = (Branch) obj;
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getWorkflowRef(), other.getWorkflowRef())) {
            return false;
        }
        if (!Objects.equals(this.budgets, other.budgets)) {
            return false;
        }
        if (!Objects.equals(this.summaries, other.summaries)) {
            return false;
        }
        return true;
    }

    
    
}
