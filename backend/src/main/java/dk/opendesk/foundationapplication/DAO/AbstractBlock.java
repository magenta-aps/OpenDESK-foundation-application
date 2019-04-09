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
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public abstract class AbstractBlock extends Reference{
    private static final Logger LOGGER = Logger.getLogger(AbstractBlock.class);
    
    private Optional<String> id;
    private Optional<String> label;
    private Optional<String> layout;
    private Optional<String> icon;
    private Optional<Boolean> collapsible;
    private Optional<Boolean> repeatable;

    public AbstractBlock() {
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

    public String getLabel() {
        return get(label);
    }
    
    public boolean wasLabelSet(){
        return wasSet(label);
    }

    public void setLabel(String label) {
        this.label = optional(label);
    }

    public String getLayout() {
        return get(layout);
    }

    public boolean wasLayoutSet(){
        return wasSet(layout);
    }

    public void setLayout(String layout) {
        this.layout = optional(layout);
    }

    public String getIcon() {
        return get(icon);
    }

    public boolean wasIconSet(){
        return wasSet(icon);
    }

    public void setIcon(String icon) {
        this.icon = optional(icon);
    }

    public Boolean getCollapsible() {
        return get(collapsible);
    }

    public boolean wasCollapsibleSet(){
        return wasSet(collapsible);
    }

    public void setCollapsible(Boolean collapsible) {
        this.collapsible = optional(collapsible);
    }

    public Boolean getRepeatable() {
        return get(repeatable);
    }

    public boolean wasRepeatableSet(){
        return wasSet(repeatable);
    }

    public void setRepeatable(Boolean repeatable) {
        this.repeatable = optional(repeatable);
    }
    


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.getId());
        hash = 89 * hash + Objects.hashCode(this.getLabel());
        hash = 89 * hash + Objects.hashCode(this.getLayout());
        hash = 89 * hash + Objects.hashCode(this.getIcon());
        hash = 89 * hash + Objects.hashCode(this.getCollapsible());
        hash = 89 * hash + Objects.hashCode(this.getRepeatable());
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
        final ApplicationBlock other = (ApplicationBlock) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLabel(), other.getLabel())) {
            return false;
        }
        if (!Objects.equals(this.getLayout(), other.getLayout())) {
            return false;
        }
        if (!Objects.equals(this.getIcon(), other.getIcon())) {
            return false;
        }
        if (!Objects.equals(this.getCollapsible(), other.getCollapsible())) {
            return false;
        }
        if (!Objects.equals(this.getRepeatable(), other.getRepeatable())) {
            return false;
        }
        return true;
    }
    
    @Override
    public ToStringBuilder toStringBuilder(){
        return super.toStringBuilder().append("id", id).append("label", label).append("layout", layout).append("icon", icon).append("collapsible", collapsible).append("repeatable", repeatable);
    }

    @Override
    public String toString() {
        return toStringBuilder().build();
    }
}
