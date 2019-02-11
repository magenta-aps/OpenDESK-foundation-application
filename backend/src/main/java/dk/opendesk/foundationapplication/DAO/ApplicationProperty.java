/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author martin
 */
public class ApplicationProperty<E> extends DAOType{
    private Optional<String> id;
    private Optional<String> label;
    private Optional<Class<E>> javaType;
    private Optional<String> type;
    private Optional<String> function;
    private Optional<List<E>> allowedValues;
    private Optional<String> layout;

    public ApplicationProperty() {
    }

    public ApplicationProperty(Optional<String> id, Optional<String> label, Optional<Class<E>> javaType, Optional<String> type, Optional<String> function, Optional<List<E>> allowedValues, Optional<String> layout) {
        this.id = id;
        this.label = label;
        this.javaType = javaType;
        this.type = type;
        this.function = function;
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

    public String getFunction() {
        return get(function);
    }
    
    public boolean wasFunctionSet(){
        return wasSet(function);
    }

    public void setFunction(String function) {
        this.function = optional(function);
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
    public ToStringBuilder toStringBuilder(){
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append("id", id).append("label", label).append("javaType", javaType).append("type", type).append("function", function).append("layout", layout);
        return builder;
    }
    
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
    
}
