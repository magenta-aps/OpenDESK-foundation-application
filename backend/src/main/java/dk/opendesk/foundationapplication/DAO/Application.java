/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class Application extends ApplicationSummary{
    private Optional<StateReference> state;
    private Optional<BudgetReference> budget;
    private Optional<String> addressRoad;
    private Optional<Integer> addressNumber;
    private Optional<String> addressFloor;
    private Optional<String> addressPostalCode;
    private Optional<String> contactFirstName;
    private Optional<String> contactLastName;
    private Optional<String> contactEmail;
    private Optional<String> contactPhone;
    private Optional<String> accountRegistration;
    private Optional<String> accountNumber;
    private Optional<Reference> projectDescriptionDoc;
    private Optional<Reference> budgetDoc;
    private Optional<Reference> boardMembersDoc;
    private Optional<Reference> articlesOfAssociationDoc;
    private Optional<Reference> financialAccountingDoc;
    
    public StateReference getState() {
        return get(state);
    }
    
    public boolean wasStateReferenceSet(){
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

    public String getAddressRoad() {
        return get(addressRoad);
    }
    
    public boolean wasAddressRoadSet() {
        return wasSet(addressRoad);
    }

    public void setAddressRoad(String addressRoad) {
        this.addressRoad = optional(addressRoad);
    }

    public Integer getAddressNumber() {
        return get(addressNumber);
    }
    
    public boolean wasAddressNumberSet() {
        return wasSet(addressNumber);
    }

    public void setAddressNumber(Integer addressNumber) {
        this.addressNumber = optional(addressNumber);
    }

    public String getAddressFloor() {
        return get(addressFloor);
    }
    
    public boolean wasAddressFloorSet(){
        return wasSet(addressFloor);
    }

    public void setAddressFloor(String addressFloor) {
        this.addressFloor = optional(addressFloor);
    }

    public String getAddressPostalCode() {
        return get(addressPostalCode);
    }
    
    public boolean wasAddressPostalCodeSet(){
        return wasSet(addressPostalCode);
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = optional(addressPostalCode);
    }

    public String getContactFirstName() {
        return get(contactFirstName);
    }
    
    public boolean wasContactFirstNameSet() {
        return wasSet(contactFirstName);
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = optional(contactFirstName);
    }

    public String getContactLastName() {
        return get(contactLastName);
    }
    
    public boolean wasContactLastNameSet() {
        return wasSet(contactLastName);
    }
    
    public boolean wastContactLastNameSet() {
        return wasSet(contactLastName);
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = optional(contactLastName);
    }

    public String getContactEmail() {
        return get(contactEmail);
    }
    
    public boolean wasContactEmailSet() {
        return wasSet(contactEmail);
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = optional(contactEmail);
    }

    public String getContactPhone() {
        return get(contactPhone);
    }
    
    public boolean wasContactPhoneSet() {
        return wasSet(contactPhone);
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = optional(contactPhone);
    }

    public String getAccountRegistration() {
        return get(accountRegistration);
    }
    
    public boolean wasAccountRegistrationSet() {
        return wasSet(accountRegistration);
    }

    public void setAccountRegistration(String accountRegistration) {
        this.accountRegistration = optional(accountRegistration);
    }

    public String getAccountNumber() {
        return get(accountNumber);
    }
    
    public boolean wasAccountNumberSet() {
        return wasSet(accountNumber);
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = optional(accountNumber);
    }

    public Reference getProjectDescriptionDoc() {
        return get(projectDescriptionDoc);
    }
    
    public boolean wasProjectDescriptionDocSet() {
        return wasSet(projectDescriptionDoc);
    }

    public void setProjectDescriptionDoc(Reference projectDescriptionDoc) {
        this.projectDescriptionDoc = optional(projectDescriptionDoc);
    }

    public Reference getBudgetDoc() {
        return get(budgetDoc);
    }
    
    public boolean wasBudgetDocSet() {
        return wasSet(budgetDoc);
    }

    public void setBudgetDoc(Reference budgetDoc) {
        this.budgetDoc = optional(budgetDoc);
    }

    public Reference getBoardMembersDoc() {
        return get(boardMembersDoc);
    }
    
    public boolean wasBoardMembersDocSet() {
        return wasSet(boardMembersDoc);
    }

    public void setBoardMembersDoc(Reference boardMembersDoc) {
        this.boardMembersDoc = optional(boardMembersDoc);
    }

    public Reference getArticlesOfAssociationDoc() {
        return get(articlesOfAssociationDoc);
    }
    
    public boolean wasArticlesOfAssociationDocSet() {
        return wasSet(articlesOfAssociationDoc);
    }

    public void setArticlesOfAssociationDoc(Reference articlesOfAssociationDoc) {
        this.articlesOfAssociationDoc = optional(articlesOfAssociationDoc);
    }

    public Reference getFinancialAccountingDoc() {
        return get(financialAccountingDoc);
    }
    
    public boolean wasFinancialAccountingDocSet() {
        return wasSet(financialAccountingDoc);
    }

    public void setFinancialAccountingDoc(Reference financialAccountingDoc) {
        this.financialAccountingDoc = optional(financialAccountingDoc);
    }
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.getState());
        hash = 79 * hash + Objects.hashCode(this.getBudget());
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
        if (!Objects.equals(this.getCvr(), other.getCvr())) {
            return false;
        }
        if (!Objects.equals(this.getAmountApplied(), other.getAmountApplied())) {
            return false;
        }
        if (!Objects.equals(this.getBranchRef(), other.getBranchRef())) {
            return false;
        }
        if (!Objects.equals(this.getState(), other.getState())) {
            return false;
        }
        if (!Objects.equals(this.getBudget(), other.getBudget())) {
            return false;
        }
        if (!Objects.equals(this.getProjectDescriptionDoc(), other.getProjectDescriptionDoc())) {
            return false;
        }
        if (!Objects.equals(this.getBudgetDoc(), other.getBudgetDoc())) {
            return false;
        }
        if (!Objects.equals(this.getBoardMembersDoc(), other.getBoardMembersDoc())) {
            return false;
        }
        if (!Objects.equals(this.getArticlesOfAssociationDoc(), other.getArticlesOfAssociationDoc())) {
            return false;
        }
        if (!Objects.equals(this.getFinancialAccountingDoc(), other.getFinancialAccountingDoc())) {
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
