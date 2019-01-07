/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class Application extends ApplicationSummary{
    private StateReference state;
    private BudgetReference budget;
    private String addressRoad;
    private Integer addressNumber;
    private String addressFloor;
    private String addressPostalCode;
    private String contactFirstName;
    private String contactLastName;
    private String contactEmail;
    private String contactPhone;
    private String accountRegistration;
    private String accountNumber;
    
    public StateReference getState() {
        return state;
    }

    public void setState(StateReference state) {
        this.state = state;
    }

    public BudgetReference getBudget() {
        return budget;
    }

    public void setBudget(BudgetReference budget) {
        this.budget = budget;
    }

    public String getAddressRoad() {
        return addressRoad;
    }

    public void setAddressRoad(String addressRoad) {
        this.addressRoad = addressRoad;
    }

    public Integer getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(Integer addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getAddressFloor() {
        return addressFloor;
    }

    public void setAddressFloor(String addressFloor) {
        this.addressFloor = addressFloor;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getAccountRegistration() {
        return accountRegistration;
    }

    public void setAccountRegistration(String accountRegistration) {
        this.accountRegistration = accountRegistration;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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
        if (!Objects.equals(this.state, other.state)) {
            return false;
        }
        if (!Objects.equals(this.budget, other.budget)) {
            return false;
        }
        return true;
    }

    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("budget", this.getBudget()).append("state", this.getState());
    }

    @Override
    public String toString() {
        return toStringBuilder().build();
    }
    
}
