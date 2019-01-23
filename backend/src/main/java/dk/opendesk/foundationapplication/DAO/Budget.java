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
public class Budget extends BudgetSummary {
    private Optional<Long> amountAccepted;
    private Optional<Long> amountNominated;
    private Optional<Long> amountAvailable;
    private Optional<List<ApplicationReference>> applications;
    

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
    
    public List<ApplicationReference> getApplications() {
        return get(applications);
    }    
    
    public boolean wasApplicationsSet(){
        return wasSet(applications);
    }

    public void setApplications(List<ApplicationReference> applications) {
        this.applications = optional(applications);
    }

    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.getNodeRef());
        hash = 17 * hash + Objects.hashCode(this.getTitle());
        hash = 17 * hash + Objects.hashCode(this.getAmountTotal());
        hash = 17 * hash + Objects.hashCode(this.getAmountAccepted());
        hash = 17 * hash + Objects.hashCode(this.getAmountNominated());
        hash = 17 * hash + Objects.hashCode(this.getAmountAvailable());
        hash = 17 * hash + Objects.hashCode(this.getApplications());
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
        if (!Objects.equals(this.getAmountAccepted(), other.getAmountAccepted())) {
            return false;
        }
        if (!Objects.equals(this.getAmountNominated(), other.getAmountNominated())) {
            return false;
        }
        if (!Objects.equals(this.getAmountAvailable(), other.getAmountAvailable())) {
            return false;
        }
        if (!Objects.equals(this.getApplications(), other.getApplications())) {
            return false;
        }
        return true;
    }

}
