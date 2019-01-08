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
public class Budget extends BudgetReference {

    private Optional<Long> amount;
    private Optional<Long> remaining;

    public Budget() {
    }

    public Long getAmount() {
        return get(amount);
    }
    
    public boolean wasAmountSet() {
        return wasSet(amount);
    }

    public void setAmount(Long amount) {
        this.amount = optional(amount);
    }
    
    public Long getRemaining() {
        return get(remaining);
    }
    
    public boolean WasRemainingSet(){
        return wasSet(remaining);
    }

    public void setRemaining(Long remaining) {
        this.remaining = optional(remaining);
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.getNodeRef());
        hash = 17 * hash + Objects.hashCode(this.getTitle());
        hash = 17 * hash + Objects.hashCode(this.getAmount());
        hash = 17 * hash + Objects.hashCode(this.getRemaining());
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
        final Budget other = (Budget) obj;
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getAmount(), other.getAmount())) {
            return false;
        }
        if (!Objects.equals(this.getRemaining(), other.getRemaining())) {
            return false;
        }
        return true;
    }

}
