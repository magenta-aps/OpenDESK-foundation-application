/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author martin
 */
public class BudgetYearSummary extends BudgetYearReference {
    //Consider making these into strings
    private Optional<Date> startDate;
    private Optional<Date> endDate;

    public Date getStartDate() {
        return get(startDate);
    }    
    
    public boolean wasStartDateSet(){
        return wasSet(startDate);
    }

    public void setStartDate(Date startDate) {
        this.startDate = optional(startDate);
    }
    
    public Date getEndDate() {
        return get(endDate);
    }    
    
    public boolean wasEndDateSet(){
        return wasSet(endDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = optional(endDate);
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getNodeRef());
        hash = 97 * hash + Objects.hashCode(this.getTitle());
        hash = 97 * hash + Objects.hashCode(this.getStartDate());
        hash = 97 * hash + Objects.hashCode(this.getEndDate());
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
        final BudgetYearSummary other = (BudgetYearSummary) obj;
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
        return true;
    }
    
}
