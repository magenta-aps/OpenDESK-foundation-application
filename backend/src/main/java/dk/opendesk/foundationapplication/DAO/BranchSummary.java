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
public class BranchSummary extends BranchReference{
    private Optional<WorkflowReference> workflowRef;

    public BranchSummary() {
    }
    

    public WorkflowReference getWorkflowRef() {
        return get(workflowRef);
    }
    
    public boolean wasWorkflowRefSet(){
        return wasSet(workflowRef);
    }

    public void setWorkflowRef(WorkflowReference workflowRef) {
        this.workflowRef = optional(workflowRef);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.getTitle());
        hash = 53 * hash + Objects.hashCode(this.getNodeRef());
        hash = 53 * hash + Objects.hashCode(this.getWorkflowRef());
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
        final BranchSummary other = (BranchSummary) obj;
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getWorkflowRef(), other.getWorkflowRef())) {
            return false;
        }
        return true;
    }
    
    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("workflowRef", workflowRef);
    }

    
    
}
