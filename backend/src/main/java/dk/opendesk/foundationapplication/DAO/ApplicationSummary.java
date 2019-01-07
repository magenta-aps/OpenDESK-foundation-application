/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class ApplicationSummary extends ApplicationReference {

    private BranchReference branchRef;
    private String category;
    private String recipient;
    private String shortDescription;
    private Date startDate;
    private Date endDate;
    private Long amountApplied;

    public ApplicationSummary() {
    }

    public BranchReference getBranchRef() {
        return branchRef;
    }

    public void setBranchRef(BranchReference branchRef) {
        this.branchRef = branchRef;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getAmountApplied() {
        return amountApplied;
    }

    public void setAmountApplied(Long amountApplied) {
        this.amountApplied = amountApplied;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.getTitle());
        hash = 23 * hash + Objects.hashCode(this.getNodeRef());
        hash = 23 * hash + Objects.hashCode(this.category);
        hash = 23 * hash + Objects.hashCode(this.recipient);
        hash = 23 * hash + Objects.hashCode(this.shortDescription);
        hash = 23 * hash + Objects.hashCode(this.startDate);
        hash = 23 * hash + Objects.hashCode(this.endDate);
        hash = 23 * hash + Objects.hashCode(this.amountApplied);
        hash = 23 * hash + Objects.hashCode(this.branchRef);
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
        if (!Objects.equals(this.category, other.category)) {
            return false;
        }
        if (!Objects.equals(this.recipient, other.recipient)) {
            return false;
        }
        if (!Objects.equals(this.shortDescription, other.shortDescription)) {
            return false;
        }
        if (!Objects.equals(this.startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(this.endDate, other.endDate)) {
            return false;
        }
        if (!Objects.equals(this.amountApplied, other.amountApplied)) {
            return false;
        }
        if (!Objects.equals(this.branchRef, other.branchRef)) {
            return false;
        }
        return true;
    }

    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("branchRef", this.getBranchRef()).append("category", this.getCategory()).append("amountApplied", this.getAmountApplied()).append("startDate", this.getStartDate()).append("endDate", this.getEndDate()).append("recipient", this.getRecipient()).append("shortDescription", this.getShortDescription());
    }

    @Override
    public String toString() {
        return toStringBuilder().build();
    }

}
