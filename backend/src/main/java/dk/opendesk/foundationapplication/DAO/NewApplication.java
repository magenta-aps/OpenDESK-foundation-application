/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author martin
 */
public class NewApplication extends ApplicationSchema{
    private Optional<BranchReference> branch;
    private Optional<StateReference> state;
    private Optional<BudgetReference> budget;

    public NewApplication() {
    }
    
    public BranchReference getBranch() {
        return get(branch);
    }
    
    public boolean wasBranchSet(){
        return wasSet(branch);
    }

    public void setBranch(BranchReference branch) {
        this.branch = optional(branch);
    }

    public StateReference getState() {
        return get(state);
    }
    
    public boolean wasStateSet(){
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
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.getId());
        hash = 83 * hash + Objects.hashCode(this.getTitle());
        hash = 83 * hash + Objects.hashCode(this.getBlocks());
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
        final NewApplication other = (NewApplication) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getBlocks(), other.getBlocks())) {
            return false;
        }
        return true;
    }
    
    
}
