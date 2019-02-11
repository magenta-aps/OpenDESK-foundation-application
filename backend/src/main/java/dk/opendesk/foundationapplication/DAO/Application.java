/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class Application extends ApplicationSummary{
    private Optional<StateReference> state;
    private Optional<BudgetReference> budget;
    
    public StateReference getState() {
        return get(state);
    }
    
    public boolean wasStateReferenceSet(){
        return wasSet(state);
    }

    public void setState(StateReference state) {
        this.state = optional(state);
    }

    public BudgetReference getBudget() {
        return get(budget);
    }
    
    public boolean wasBudgetSet(){
        return wasSet(budget);
    }

    public void setBudget(BudgetReference budget) {
        this.budget = optional(budget);
    }
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.getState());
        hash = 79 * hash + Objects.hashCode(this.getBudget());
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
        final Application other = (Application) obj;
                if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getBranchRef(), other.getBranchRef())) {
            return false;
        }
        if (!Objects.equals(this.getState(), other.getState())) {
            return false;
        }
        if (!Objects.equals(this.getBudget(), other.getBudget())) {
            return false;
        }
        return true;
    }
    
    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("budget", this.getBudget()).append("state", this.getState());
    }
    
}
