/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author martin
 */
public class Branch extends BranchSummary{
    private Optional<List<BudgetReference>> budgets = null;
    private Optional<List<ApplicationSummary>> summaries = null;

    public Branch() {
    }

    public List<BudgetReference> getBudgets() {
        return get(budgets);
    }
    
    public boolean wasBudgetsSet(){
        return wasSet(budgets);
    }

    public void setBudgets(List<BudgetReference> budgets) {
        this.budgets = optional(budgets);
    }

    public List<ApplicationSummary> getSummaries() {
        return get(summaries);
    }
    
    public boolean wasSummariesSet(){
        return wasSet(summaries);
    }

    public void setSummaries(List<ApplicationSummary> summaries) {
        this.summaries = optional(summaries);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.getBudgets());
        hash = 79 * hash + Objects.hashCode(this.getSummaries());
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
        if (!Objects.equals(this.getBudgets(), other.getBudgets())) {
            return false;
        }
        if (!Objects.equals(this.getSummaries(), other.getSummaries())) {
            return false;
        }
        return true;
    }

    
    
}
