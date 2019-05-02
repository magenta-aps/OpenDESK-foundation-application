/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.opendesk.foundationapplication.enums.StateCategory;
import dk.opendesk.foundationapplication.validator.aggregate.Aggregator;
import java.util.ArrayList;
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
public class MultiFieldData <E, T> extends Reference{
    private static final Logger LOGGER = Logger.getLogger(MultiFieldData.class);
    private Optional<String> id;
    private Optional<String> label;
    private Optional<Class<E>> type;
    private Optional<String> component;
    private Optional<String> describes;
    private Optional<String> layout;
    private Optional<String> hint;
    private Optional<String> wrapper;
    private Optional<String> validation;
    private Optional<ArrayList<String>> controlledBy;
    private Optional<ArrayList<String>> aggregateStateCategories;
    private Optional<Class<T>> aggregateType;
    private Optional<String> aggregateComponent;
    private Optional<String> aggregateLayout;
    private Optional<String> aggregateHint;
    private Optional<String> aggregateWrapper;
    private Optional<String> aggregateDescribes;
    private Optional<Class<? extends Aggregator<E, T>>> aggregatorClass;

      
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
    
    public String getValidation() {
        return get(validation);
    }

    public boolean wasValidationSet(){
        return wasSet(validation);
    }

    public void setValidation(String validation) {
        this.validation = optional(validation);
    }

    @JsonIgnore
    public Class<E> getTypeAsClass() {
        return get(type);
    }
    
    public String getType(){
        Class classType = get(type);
        if(classType == null){
            return null;
        }else{
            return classType.getCanonicalName();
        }
    }
    
    public boolean wasTypeSet(){
        return wasSet(type);
    }

    public void setType(String javaType){
        if(javaType != null){
            try {
                this.type = optional((Class<E>)Class.forName(javaType));
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Failed to find specified class. Setting to null", ex);
                this.type = null;
            }
        }
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

    public String getLayout() {
        return get(layout);
    }

    public boolean wasLayoutSet(){
        return wasSet(layout);
    }

    public void setLayout(String layout) {
        this.layout = optional(layout);
    }
    
    public ArrayList<String> getControlledBy() {
        return get(controlledBy);
    }

    public boolean wasControlledBySet(){
        return wasSet(controlledBy);
    }

    public void setControlledBy(ArrayList<String> controlledBy) {
        this.controlledBy = optional(controlledBy);
    }

    public ArrayList<String> getAggregateStateCategories() {
        return get(aggregateStateCategories);
    }

    public boolean wasAggregateStateCategoriesSet(){
        return wasSet(aggregateStateCategories);
    }

    public void setAggregateStateCategories(ArrayList<String> aggregateStateCategories) {
        this.aggregateStateCategories = optional(aggregateStateCategories);
    }

    public String getAggregateType() {
        Class classType = get(aggregateType);
        if(classType == null){
            return null;
        }else{
            return classType.getCanonicalName();
        }
    }

    public boolean wasAggregateTypeSet(){
        return wasSet(aggregateType);
    }
    
    @JsonIgnore
    public Class<T> getAggregateTypeAsClass() {
        return get(aggregateType);
    }

    public void setAggregateType(String aggregateType) {
        if(aggregateType != null){
            try {
                this.aggregateType = optional((Class<T>)Class.forName(aggregateType));
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Failed to find specified class. Setting to null", ex);
                this.aggregateType = null;
            }
        }
    }

    public String getAggregateComponent() {
        return get(aggregateComponent);
    }

    public boolean wasAggregateComponentSet(){
        return wasSet(aggregateComponent);
    }

    public void setAggregateComponent(String aggregateComponent) {
        this.aggregateComponent = optional(aggregateComponent);
    }

    public String getAggregateLayout() {
        return get(aggregateLayout);
    }

    public boolean wasAggregateLayoutSet(){
        return wasSet(aggregateLayout);
    }

    public void setAggregateLayout(String aggregateLayout) {
        this.aggregateLayout = optional(aggregateLayout);
    }

    public String getAggregateHint() {
        return get(aggregateHint);
    }

    public boolean wasAggregateHintSet(){
        return wasSet(aggregateHint);
    }

    public void setAggregateHint(String aggregateHint) {
        this.aggregateHint = optional(aggregateHint);
    }

    public String getAggregateWrapper() {
        return get(aggregateWrapper);
    }

    public boolean wasAggregateWrapperSet(){
        return wasSet(aggregateWrapper);
    }

    public void setAggregateWrapper(String aggregateWrapper) {
        this.aggregateWrapper = optional(aggregateWrapper);
    }

    public String getAggregateDescribes() {
        return get(aggregateDescribes);
    }

    public boolean wasAggregateDescribesSet(){
        return wasSet(aggregateDescribes);
    }

    public void setAggregateDescribes(String aggregateDescribes) {
        this.aggregateDescribes = optional(aggregateDescribes);
    }
    
    public String getAggregator() {
        Class classType = get(aggregatorClass);
        if(classType == null){
            return null;
        }else{
            return classType.getCanonicalName();
        }
    }

    public boolean wasAggregatorSet(){
        return wasSet(aggregatorClass);
    }
    
    @JsonIgnore
    public Class<? extends Aggregator<E, T>> getAggregatorAsClass() {
        return get(aggregatorClass);
    }

    public void setAggregator(String aggregateType) {
        if(aggregateType != null){
            try {
                this.aggregatorClass = optional((Class<? extends Aggregator<E, T>>)Class.forName(aggregateType));
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Failed to find specified class. Setting to null", ex);
                this.aggregatorClass = null;
            }
        }
    }
    
   @JsonIgnore
   public boolean isAggregate(){
       return aggregatorClass != null;
   }
    
    @Override
    public ToStringBuilder toStringBuilder(){
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append("type", type).append("component", component).append("function", describes).append("layout", layout)
                .append("aggregateComponent", aggregateComponent).append("aggregateDescribes", aggregateDescribes).append("aggregateHint", aggregateHint).append("aggregateLayout", aggregateLayout).append("aggregateType", aggregateType).append("aggregateWrapper", aggregateWrapper).append("aggregatorClass", aggregatorClass);
        return builder;
    }
    
        @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getType());
        hash = 97 * hash + Objects.hashCode(this.getComponent());
        hash = 97 * hash + Objects.hashCode(this.getDescribes());
        hash = 97 * hash + Objects.hashCode(this.getLayout());
        hash = 97 * hash + Objects.hashCode(this.getAggregateComponent());
        hash = 97 * hash + Objects.hashCode(this.getAggregateDescribes());
        hash = 97 * hash + Objects.hashCode(this.getAggregateHint());
        hash = 97 * hash + Objects.hashCode(this.getAggregateLayout());
        hash = 97 * hash + Objects.hashCode(this.getAggregateType());
        hash = 97 * hash + Objects.hashCode(this.getAggregateWrapper());
        hash = 97 * hash + Objects.hashCode(this.getAggregator());
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
        final MultiFieldData<?, ?> other = (MultiFieldData<?, ?>) obj;
        if (!Objects.equals(this.getType(), other.getType())) {
            return false;
        }
        if (!Objects.equals(this.getComponent(), other.getComponent())) {
            return false;
        }
        if (!Objects.equals(this.getDescribes(), other.getDescribes())) {
            return false;
        }
        if (!Objects.equals(this.getLayout(), other.getLayout())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateComponent(), other.getAggregateComponent())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateDescribes(), other.getAggregateDescribes())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateHint(), other.getAggregateHint())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateLayout(), other.getAggregateLayout())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateType(), other.getAggregateType())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateWrapper(), other.getAggregateWrapper())) {
            return false;
        }
        if (!Objects.equals(this.getAggregator(), other.getAggregator())) {
            return false;
        }
        return true;
    }
    
}
