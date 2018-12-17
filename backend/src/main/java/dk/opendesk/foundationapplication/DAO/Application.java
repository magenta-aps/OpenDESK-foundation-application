/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;

/**
 *
 * @author martin
 */
public class Application extends ApplicationSummary{
    private StateReference state;
    private Budget budget;
    
     public StateReference getState() {
        return state;
    }

    public void setState(StateReference state) {
        this.state = state;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.state);
        hash = 79 * hash + Objects.hashCode(this.budget);
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
        if (!Objects.equals(this.getCategory(), other.getCategory())) {
            return false;
        }
        if (!Objects.equals(this.getRecipient(), other.getRecipient())) {
            return false;
        }
        if (!Objects.equals(this.getShortDescription() , other.getShortDescription())) {
            return false;
        }
        if (!Objects.equals(this.getBudgetTitle(), other.getBudgetTitle())) {
            return false;
        }
        if (!Objects.equals(this.getStateTitle(), other.getStateTitle())) {
            return false;
        }
        if (!Objects.equals(this.getStartDate(), other.getStartDate())) {
            return false;
        }
        if (!Objects.equals(this.getEndDate(), other.getEndDate())) {
            return false;
        }
        if (!Objects.equals(this.getAmountApplied(), other.getAmountApplied())) {
            return false;
        }
        if (!Objects.equals(this.getBudgetRef(), other.getBudgetRef())) {
            return false;
        }
        if (!Objects.equals(this.getStateRef(), other.getStateRef())) {
            return false;
        }
        if (!Objects.equals(this.state, other.state)) {
            return false;
        }
        if (!Objects.equals(this.budget, other.budget)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Application{" + "state=" + state + ", budget=" + budget + '}';
    }
    
    
    
    
}
