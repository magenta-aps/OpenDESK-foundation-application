/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;

/**
 *
 * @author martin
 */
public class BranchSummary extends BranchReference{
    private WorkflowReference workflowRef;

    public BranchSummary() {
    }
    
    public BranchSummary(String title, String uuid, WorkflowReference workflowRef) {
        this.workflowRef = workflowRef;
    }

    public WorkflowReference getWorkflowRef() {
        return workflowRef;
    }

    public void setWorkflowRef(WorkflowReference workflowRef) {
        this.workflowRef = workflowRef;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.getTitle());
        hash = 53 * hash + Objects.hashCode(this.getNodeRef());
        hash = 53 * hash + Objects.hashCode(this.workflowRef);
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
        if (!Objects.equals(this.workflowRef, other.workflowRef)) {
            return false;
        }
        return true;
    }
    
    

    
    
}
