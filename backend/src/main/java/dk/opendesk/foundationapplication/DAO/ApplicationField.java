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
    private Optional<Class<E>> type;
    private Optional<String> component;
    private Optional<String> describes;
    private Optional<List<E>> allowedValues;
    private Optional<String> layout;
    private Optional<String> hint;
    private Optional<String> wrapper;
    private Optional<String> validation;
    private Optional<String> permissions;
    private Optional<Boolean> readOnly;


    public ApplicationField() {
    }

    public ApplicationField(Optional<String> id, Optional<String> label, Optional<Class<E>> type, Optional<String> component, Optional<String> function, Optional<List<E>> allowedValues, Optional<String> layout) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.component = component;
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

    public Class<E> getType() {
        return get(type);
    }
    
    public boolean wasTypeSet(){
        return wasSet(type);
    }

    public void setType(Class<E> javaType) {
        this.type = optional(javaType);
    }

    public String getComponent() {
        return get(component);
    }
    
    public boolean wasComponentSet(){
        return wasSet(component);
    }

    public void setComponent(String component) {
        this.component = optional(component);
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
    
    public String getHint() {
        return get(hint);
    }
    
    public boolean wasHintSet(){
        return wasSet(hint);
    }

    public void setHint(String hint) {
        this.hint = optional(hint);
    }

    public String getWrapper() {
        return get(wrapper);
    }

    public boolean wasWrapperSet(){
        return wasSet(wrapper);
    }

    public void setWrapper(String wrapper) {
        this.wrapper = optional(wrapper);
    }

    public String getValidation() {
        return get(validation);
    }

    public boolean wasValidationSet(){
        return wasSet(validation);
    }

    public void setValidation(String validation) {
        this.validation = optional(validation);
    }

    public String getPermissions() {
        return get(permissions);
    }

    public boolean wasPermissionsSet(){
        return wasSet(permissions);
    }

    public void setPermissions(String permissions) {
        this.permissions = optional(permissions);
    }

    public Boolean getReadOnly() {
        return get(readOnly);
    }

    public boolean wasReadOnlySet(){
        return wasSet(readOnly);
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = optional(readOnly);
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
        hash = 97 * hash + Objects.hashCode(this.getType());
        hash = 97 * hash + Objects.hashCode(this.getComponent());
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
        if (!Objects.equals(this.getType(), other.getType())) {
            return false;
        }
        if (!Objects.equals(this.getComponent(), other.getComponent())) {
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
        builder.append("id", id).append("label", label).append("type", type).append("component", component).append("function", describes).append("layout", layout);
        return builder;
    }
    
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
    
}
