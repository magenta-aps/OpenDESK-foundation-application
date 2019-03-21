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
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author martin
 */
public class ApplicationField<E> extends DAOType{
    private Optional<String> id;
    private Optional<String> label;
    private Optional<Class<E>> javaType;
    private Optional<String> type;
    private Optional<String> describes;
    private Optional<List<E>> allowedValues;
    private Optional<String> layout;

    public ApplicationField() {
    }

    public ApplicationField(Optional<String> id, Optional<String> label, Optional<Class<E>> javaType, Optional<String> type, Optional<String> function, Optional<List<E>> allowedValues, Optional<String> layout) {
        this.id = id;
        this.label = label;
        this.javaType = javaType;
        this.type = type;
        this.describes = function;
        this.allowedValues = allowedValues;
        this.layout = layout;
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

    public Class<E> getJavaType() {
        return get(javaType);
    }
    
    public boolean wasJavaTypeSet(){
        return wasSet(javaType);
    }

    public void setJavaType(Class<E> javaType) {
        this.javaType = optional(javaType);
    }

    public String getType() {
        return get(type);
    }
    
    public boolean wasTypeSet(){
        return wasSet(type);
    }

    public void setType(String type) {
        this.type = optional(type);
    }

    public String getDescribes() {
        return get(describes);
    }
    
    public boolean wasDescribesSet(){
        return wasSet(describes);
    }

    public void setDescribes(String describes) {
        this.describes = optional(describes);
    }

    public List<E> getAllowedValues() {
        return get(allowedValues);
    }
    
    public boolean wasAllowedValuesSet(){
        return wasSet(allowedValues);
    }

    public void setAllowedValues(List<E> allowedValues) {
        this.allowedValues = optional(allowedValues);
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getId());
        hash = 97 * hash + Objects.hashCode(this.getLabel());
        hash = 97 * hash + Objects.hashCode(this.getJavaType());
        hash = 97 * hash + Objects.hashCode(this.getType());
        hash = 97 * hash + Objects.hashCode(this.getDescribes());
        hash = 97 * hash + Objects.hashCode(this.getAllowedValues());
        hash = 97 * hash + Objects.hashCode(this.getLayout());
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
        final ApplicationField<?> other = (ApplicationField<?>) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLabel(), other.getLabel())) {
            return false;
        }
        if (!Objects.equals(this.getJavaType(), other.getJavaType())) {
            return false;
        }
        if (!Objects.equals(this.getType(), other.getType())) {
            return false;
        }
        if (!Objects.equals(this.getDescribes(), other.getDescribes())) {
            return false;
        }
        if (!Objects.equals(this.getAllowedValues(), other.getAllowedValues())) {
            return false;
        }
        if (!Objects.equals(this.getLayout(), other.getLayout())) {
            return false;
        }
        return true;
    }
    
    
    
    @Override
    public ToStringBuilder toStringBuilder(){
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append("id", id).append("label", label).append("javaType", javaType).append("type", type).append("function", describes).append("layout", layout);
        return builder;
    }
    
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
    
}
