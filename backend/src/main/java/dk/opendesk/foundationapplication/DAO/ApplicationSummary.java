/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import dk.opendesk.foundationapplication.enums.Functional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class ApplicationSummary extends ApplicationReference {
    private Optional<BranchSummary> branchSummary;
    private Optional<List<ApplicationBlock>> blocks;

    public ApplicationSummary() {
    }

    public BranchSummary getBranchSummary() {
        return get(branchSummary);
    }
    
    public boolean wasBranchSummarySet(){
        return wasSet(branchSummary);
    }

    public void setBranchSummary(BranchSummary branchSummary) {
        this.branchSummary = optional(branchSummary);
    }

    public List<ApplicationBlock> getBlocks() {
        return get(blocks);
    }
    
    public boolean wasBlocksSet(){
        return wasSet(blocks);
    }

    public void setBlocks(List<ApplicationBlock> blocks) {
        this.blocks = optional(blocks);
    }
    
    public final <E, A extends ApplicationFieldValue<E>> A getFunctionalField(Functional<E> describes){
        if(blocks == null || !blocks.isPresent()){
            return null;
        }
        for(ApplicationBlock block: blocks.get()){
            A value = block.getFunctionalField(describes);
            if(value != null){
                return value;
            }
        }
        return null;
    }

    public final ApplicationFieldValue<Long> totalAmount(){
        return getFunctionalField(Functional.amount());
    }
    
    public final ApplicationFieldValue<String> emailTo(){
        return getFunctionalField(Functional.email_to());
    }

    public final ApplicationFieldValue<String> firstName(){
        return getFunctionalField(Functional.first_name());
    }

    public final ApplicationFieldValue<String> lastName(){
        return getFunctionalField(Functional.last_name());
    }

    public final ApplicationFieldValue<String> phoneNumber(){
        return getFunctionalField(Functional.phone_number());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(super.hashCode());
        hash = 23 * hash + Objects.hashCode(this.getNodeRef());
        hash = 23 * hash + Objects.hashCode(this.getBranchSummary());
        hash = 23 * hash + Objects.hashCode(this.getBlocks());
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
        if (!Objects.equals(this.getBranchSummary(), other.getBranchSummary())) {
            return false;
        }
        if (!Objects.equals(this.getBlocks(), other.getBlocks())) {
            return false;
        }
        return true;
    }

    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("branchRef", branchSummary).append("blocks", blocks);
    }
}