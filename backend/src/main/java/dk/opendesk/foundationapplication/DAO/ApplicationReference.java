/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import java.util.Optional;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class ApplicationReference extends Reference{
    private Optional<String> title;

    public ApplicationReference() {
    }
    

    public String getTitle() {
        return get(title);
    }
    
    public boolean wasTitleSet(){
        return wasSet(title);
    }

    public void setTitle(String title) {
        this.title = optional(title);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.getNodeRef());
        hash = 37 * hash + Objects.hashCode(this.getTitle());
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
        final ApplicationReference other = (ApplicationReference) obj;
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        return true;
    }
    
    @Override
    public ToStringBuilder toStringBuilder(){
        return super.toStringBuilder().append("title", this.getTitle()); 
    }

    
}
