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
public class BudgetSummary extends BudgetReference {
    private Optional<Long> amountTotal;
    
    public Long getAmountTotal() {
        return get(amountTotal);
    }    
    
    public boolean wasAmountTotalSet(){
        return wasSet(amountTotal);
    }

    public void setAmountTotal(Long amountTotal) {
        this.amountTotal = optional(amountTotal);
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.getNodeRef());
        hash = 17 * hash + Objects.hashCode(this.getTitle());
        hash = 17 * hash + Objects.hashCode(this.getAmountTotal());
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
        if (!Objects.equals(this.getAmountTotal(), other.getAmountTotal())) {
            return false;
        }
        return true;
    }

}
