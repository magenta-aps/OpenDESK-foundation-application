/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Date;
import java.util.Objects;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * @author martin
 */
public class ApplicationSummary extends ApplicationReference{
    private String category;
    private String recipient;
    private String shortDescription;
    private Date startDate;
    private Date endDate;
    private Long amountApplied;
    private NodeRef budgetRef;
    private String budgetTitle;
    private NodeRef stateRef;
    private String stateTitle;
    

    public ApplicationSummary() {
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

    public NodeRef getBudgetRef() {
        return budgetRef;
    }

    public void setBudgetRef(NodeRef budgetRef) {
        this.budgetRef = budgetRef;
    }

    public String getBudgetTitle() {
        return budgetTitle;
    }

    public void setBudgetTitle(String budgetTitle) {
        this.budgetTitle = budgetTitle;
    }

    public NodeRef getStateRef() {
        return stateRef;
    }

    public void setStateRef(NodeRef stateRef) {
        this.stateRef = stateRef;
    }

    public String getStateTitle() {
        return stateTitle;
    }

    public void setStateTitle(String stateTitle) {
        this.stateTitle = stateTitle;
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
        hash = 23 * hash + Objects.hashCode(this.budgetRef);
        hash = 23 * hash + Objects.hashCode(this.budgetTitle);
        hash = 23 * hash + Objects.hashCode(this.stateRef);
        hash = 23 * hash + Objects.hashCode(this.stateTitle);
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
        if (!Objects.equals(this.budgetTitle, other.budgetTitle)) {
            return false;
        }
        if (!Objects.equals(this.stateTitle, other.stateTitle)) {
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
        if (!Objects.equals(this.budgetRef, other.budgetRef)) {
            return false;
        }
        if (!Objects.equals(this.stateRef, other.stateRef)) {
            return false;
        }
        return true;
    }
    
    
    
}
