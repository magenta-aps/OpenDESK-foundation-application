/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author martin
 */
public class ApplicationSchema extends Reference {
    private Optional<String> id;
    private Optional<String> title;
    private Optional<List<ApplicationBlockSpecification>> blocks;

    public ApplicationSchema() {
    }

    public ApplicationSchema(String id, String title, List<ApplicationBlockSpecification> blocks) {
        this.id = optional(id);
        this.title = optional(title);
        this.blocks = optional(blocks);
    }

    public String getId() {
        return get(id);
    }
    
    public boolean wasIdSet(){
        return wasSet(id);
    }

    public void setId(String id) {
        this.id = optional(id);
    }

    public String getTitle() {
        return get(title);
    }
    
    public boolean wasIdTitle(){
        return wasSet(title);
    }

    public void setTitle(String title) {
        this.title = optional(title);
    }

    public List<ApplicationBlockSpecification> getBlocks() {
        return get(blocks);
    }
    
    public boolean wasBlocksSet(){
        return wasSet(blocks);
    }

    public void setBlocks(List<ApplicationBlockSpecification> blocks) {
        this.blocks = optional(blocks);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.title);
        hash = 83 * hash + Objects.hashCode(this.blocks);
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
        final ApplicationSchema other = (ApplicationSchema) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.blocks, other.blocks)) {
            return false;
        }
        return true;
    }
    
    

}
