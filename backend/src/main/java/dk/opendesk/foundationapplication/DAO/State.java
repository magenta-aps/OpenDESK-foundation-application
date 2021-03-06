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
public class State extends StateSummary {
    Optional<List<ApplicationReference>> applications;

    public State() {
        
    }

    public List<ApplicationReference> getApplications() {
        return get(applications);
    }
    
    public boolean wasApplicationsSet(){
        return wasSet(applications);
    }

    public void setApplications(List<ApplicationReference> applications) {
        this.applications = optional(applications);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.getTitle());
        hash = 53 * hash + Objects.hashCode(this.getNodeRef());
        hash = 53 * hash + Objects.hashCode(this.getReferences());
        hash = 53 * hash + Objects.hashCode(this.getApplications());
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
        final State other = (State) obj;
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getReferences(), other.getReferences())) {
            return false;
        }
        if (!Objects.equals(this.getApplications(), other.getApplications())) {
            return false;
        }

        return true;
    }
    
    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("applications", applications);
    }

}
