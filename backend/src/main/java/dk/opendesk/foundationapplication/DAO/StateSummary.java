/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import dk.opendesk.foundationapplication.enums.StateCategory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author martin
 */
public class StateSummary extends StateReference{
    private Optional<StateCategory> category = null;
    private Optional<List<StateReference>> references = null;

    public StateSummary() {
        
    }

    public StateCategory getCategory() {
        return get(category);
    }
    
    public boolean wasCategorySet(){
        return wasSet(category);
    }

    public void setCategory(StateCategory category) {
        this.category = optional(category);
    }

    public List<StateReference> getReferences() {
        return get(references);
    }
    
    public boolean wasReferencesSet(){
        return wasSet(references);
    }

    public void setReferences(List<StateReference> references) {
        this.references = optional(references);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.getTitle());
        hash = 53 * hash + Objects.hashCode(this.getNodeRef());
        hash = 53 * hash + Objects.hashCode(this.getReferences());
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
        final StateSummary other = (StateSummary) obj;
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getReferences(), other.getReferences())) {
            return false;
        }

        return true;
    }
    
}
