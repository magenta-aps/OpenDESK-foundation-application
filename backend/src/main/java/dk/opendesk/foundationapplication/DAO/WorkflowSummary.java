/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class WorkflowSummary extends WorkflowReference{
    private Optional<StateReference> entry;
    private Optional<List<StateReference>> states;

    public WorkflowSummary() {
        
    }


    public StateReference getEntry() {
        return get(entry);
    }
    
    public boolean wasEntrySet(){
        return wasSet(entry);
    }

    public void setEntry(StateReference entry) {
        this.entry = optional(entry);
    }

    public List<StateReference> getStates() {
        return get(states);
    }
    
    public boolean wasStatesSet(){
        return wasSet(states);
    }

    public void setStates(List<StateReference> states) {
        this.states = optional(states);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.getNodeRef());
        hash = 83 * hash + Objects.hashCode(this.getTitle());
        hash = 83 * hash + Objects.hashCode(this.getEntry());
        hash = 83 * hash + Objects.hashCode(this.getStates());
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
        final WorkflowSummary other = (WorkflowSummary) obj;
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getEntry(), other.getEntry())) {
            return false;
        }
        if (!Objects.equals(this.getStates(), other.getStates())) {
            return false;
        }
        return true;
    }
    
    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("entry", entry).append("states", states);
    }
    
    
    
    
}
