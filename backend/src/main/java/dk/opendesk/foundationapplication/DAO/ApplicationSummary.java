/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class ApplicationSummary extends ApplicationReference {

    private Optional<BranchReference> branchRef;
    private Optional<String> category;
    private Optional<String> recipient;
    private Optional<String> shortDescription;
    private Optional<Date> startDate;
    private Optional<Date> endDate;
    private Optional<Long> amountApplied;
    private Optional<String> cvr;

    public ApplicationSummary() {
    }

    public BranchReference getBranchRef() {
        return get(branchRef);
    }
    
    public boolean wasBranchRefSet(){
        return wasSet(branchRef);
    }

    public void setBranchRef(BranchReference branchRef) {
        this.branchRef = optional(branchRef);
    }

    public String getCategory() {
        return get(category);
    }
    
    public boolean wasCategorySet(){
        return wasSet(category);
    }

    public void setCategory(String category) {
        this.category = optional(category);
    }

    public String getRecipient() {
        return get(recipient);
    }
    
    public boolean wasRecipientSet() {
        return wasSet(recipient);
    }

    public void setRecipient(String recipient) {
        this.recipient = optional(recipient);
    }

    public String getShortDescription() {
        return get(shortDescription);
    }
    
    public boolean wasShortDescriptionSet(){
        return wasSet(shortDescription);
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = optional(shortDescription);
    }

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

    public Long getAmountApplied() {
        return get(amountApplied);
    }
    
    public boolean wasAmountAppliedSet(){
        return wasSet(amountApplied);
    }

    public void setAmountApplied(Long amountApplied) {
        this.amountApplied = optional(amountApplied);
    }

    public String getCvr() {
        return get(cvr);
    }
    
    public boolean wasCvrSet(){
        return wasSet(cvr);
    }

    public void setCvr(String cvr) {
        this.cvr = optional(cvr);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.getTitle());
        hash = 23 * hash + Objects.hashCode(this.getNodeRef());
        hash = 23 * hash + Objects.hashCode(this.getCategory());
        hash = 23 * hash + Objects.hashCode(this.getRecipient());
        hash = 23 * hash + Objects.hashCode(this.getShortDescription());
        hash = 23 * hash + Objects.hashCode(this.getStartDate());
        hash = 23 * hash + Objects.hashCode(this.getEndDate());
        hash = 23 * hash + Objects.hashCode(this.getAmountApplied());
        hash = 23 * hash + Objects.hashCode(this.getBranchRef());
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
        final ApplicationSummary other = (ApplicationSummary) obj;
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
        if (!Objects.equals(this.getShortDescription(), other.getShortDescription())) {
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
        if (!Objects.equals(this.getBranchRef(), other.getBranchRef())) {
            return false;
        }
        if (!Objects.equals(this.getCvr(), other.getCvr())) {
            return false;
        }
        return true;
    }

    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("branchRef", this.getBranchRef()).append("category", this.getCategory()).append("amountApplied", this.getAmountApplied()).append("startDate", this.getStartDate()).append("endDate", this.getEndDate()).append("recipient", this.getRecipient()).append("shortDescription", this.getShortDescription());
    }


}
