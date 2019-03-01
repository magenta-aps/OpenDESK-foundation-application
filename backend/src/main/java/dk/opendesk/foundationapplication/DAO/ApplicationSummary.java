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
    private Optional<List<ApplicationPropertiesContainer>> blocks;
    private Optional<Boolean> isSeen;
    
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

    public List<ApplicationPropertiesContainer> getBlocks() {
        return get(blocks);
    }
    
    public boolean wasBlocksSet(){
        return wasSet(blocks);
    }

    public void setBlocks(List<ApplicationPropertiesContainer> blocks) {
        this.blocks = optional(blocks);
    }
    
    public final <E, A extends ApplicationPropertyValue<E>> A getFunctionalField(Functional<E> describes){
        if(blocks == null || !blocks.isPresent()){
            return null;
        }
        for(ApplicationPropertiesContainer block: blocks.get()){
            A value = block.getFunctionalField(describes);
            if(value != null){
                return value;
            }
        }
        return null;
    }

    public boolean getIsSeen() {
        if (!isSeen.isPresent()) {
            return false;
        }
        return get(isSeen);
    }

    public boolean wasIsSeenSet() {
        return wasSet(isSeen);
    }

    public void setIsSeen(Boolean isSeen) {
        this.isSeen = optional(isSeen);
    }
    
    public final ApplicationPropertyValue<Long> totalAmount(){
        return getFunctionalField(Functional.amount());
    }
    
    public final ApplicationPropertyValue<String> emailTo(){
        return getFunctionalField(Functional.email_to());
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