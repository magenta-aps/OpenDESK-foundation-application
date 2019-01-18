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
public class BudgetYear extends BudgetYearSummary{
    
    private Optional<Long> amountTotal;
    private Optional<Long> amountAccepted;
    private Optional<Long> amountNominated;
    private Optional<Long> amountAvailable;

    public Long getAmountTotal() {
        return get(amountTotal);
    }    
    
    public boolean wasAmountTotalSet(){
        return wasSet(amountTotal);
    }

    public void setAmountTotal(Long amountTotal) {
        this.amountTotal = optional(amountTotal);
    }
    
    public Long getAmountAccepted() {
        return get(amountAccepted);
    }    
    
    public boolean wasAmountAcceptedSet(){
        return wasSet(amountAccepted);
    }

    public void setAmountAccepted(Long amountAccepted) {
        this.amountAccepted = optional(amountAccepted);
    }
    
    public Long getAmountNominated() {
        return get(amountNominated);
    }    
    
    public boolean wasAmountNominatedSet(){
        return wasSet(amountNominated);
    }

    public void setAmountNominated(Long amountNominated) {
        this.amountNominated = optional(amountNominated);
    }
    
    public Long getAmountAvailable() {
        return get(amountAvailable);
    }    
    
    public boolean wasAmountAvailableSet(){
        return wasSet(amountAvailable);
    }

    public void setAmountAvailable(Long amountAvailable) {
        this.amountAvailable = optional(amountAvailable);
    }
    
        @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getNodeRef());
        hash = 97 * hash + Objects.hashCode(this.getTitle());
        hash = 97 * hash + Objects.hashCode(this.getStartDate());
        hash = 97 * hash + Objects.hashCode(this.getEndDate());
        hash = 97 * hash + Objects.hashCode(this.getAmountTotal());
        hash = 97 * hash + Objects.hashCode(this.getAmountAccepted());
        hash = 97 * hash + Objects.hashCode(this.getAmountNominated());
        hash = 97 * hash + Objects.hashCode(this.getAmountAvailable());
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
        final BudgetYear other = (BudgetYear) obj;
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getStartDate(), other.getStartDate())) {
            return false;
        }
        if (!Objects.equals(this.getEndDate(), other.getEndDate())) {
            return false;
        }
        if (!Objects.equals(this.getAmountTotal(), other.getAmountTotal())) {
            return false;
        }
        if (!Objects.equals(this.getAmountAccepted(), other.getAmountAccepted())) {
            return false;
        }
        if (!Objects.equals(this.getAmountNominated(), other.getAmountNominated())) {
            return false;
        }
        if (!Objects.equals(this.getAmountAvailable(), other.getAmountAvailable())) {
            return false;
        }
        return true;
    }
    
}