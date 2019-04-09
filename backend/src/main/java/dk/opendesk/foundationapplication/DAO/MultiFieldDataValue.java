/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import static dk.opendesk.foundationapplication.DAO.ApplicationFieldValue.MULTI_VALUES;
import dk.opendesk.foundationapplication.ListBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class MultiFieldDataValue <E, T> extends MultiFieldData<E, T> {
    private Optional<ArrayList<E>> value;
    private Optional<ArrayList<String>> options;
    private Optional<ArrayList<T>> aggregateValue;
    private Optional<ArrayList<String>> aggregateOptions;
    
     public ArrayList<T> getAggregateValue() {
        return get(aggregateValue);
    }

    public boolean wasAggregateValueSet(){
        return wasSet(aggregateValue);
    }

    public void setAggregateValue(ArrayList<T> aggregateValue) {
        this.aggregateValue = optional(aggregateValue);
    }
    
    @JsonIgnore
    public boolean isSingleAggregateValue(){
        ArrayList<T> values = getAggregateValue();
        if(values == null){
            return false;
        }else if(values.isEmpty()){
            return false;
        }else if(values.size() == 1){
            return true;
        }else{
            return false;
        }
    }
    
    @JsonIgnore
    public void setSingleAggregateValue(T value) {
        setAggregateValue(new ListBuilder<>(new ArrayList<T>()).add(value).build());
    }
    
    @JsonIgnore
    public T getSingleAggregateValue(){
        ArrayList<T> values = getAggregateValue();
        if(values == null){
            return null;
        }else if(values.isEmpty()){
            return null;
        }else if(values.size() > 1){
            throw new AlfrescoRuntimeException(MULTI_VALUES);
        }
        return values.get(0);
    }
    
    
    
    public ArrayList<E> getValue() {
        return get(value);
    }

    public boolean wasValueSet(){
        return wasSet(value);
    }

    public void setValue(ArrayList<E> value) {
        this.value = optional(value);
    }
    
    @JsonIgnore
    public boolean isSingleValue(){
        ArrayList<E> values = getValue();
        if(values == null){
            return false;
        }else if(values.isEmpty()){
            return false;
        }else if(values.size() == 1){
            return true;
        }else{
            return false;
        }
    }
    
    @JsonIgnore
    public void setSingleValue(E value) {
        setValue(new ListBuilder<>(new ArrayList<E>()).add(value).build());
    }
    
    @JsonIgnore
    public E getSingleValue(){
        ArrayList<E> values = getValue();
        if(values == null){
            return null;
        }else if(values.isEmpty()){
            return null;
        }else if(values.size() > 1){
            throw new AlfrescoRuntimeException(MULTI_VALUES);
        }
        return values.get(0);
    }
    
    public ArrayList<String> getOptions() {
        return get(options);
    }
    
    public boolean wasOptionsSet(){
        return wasSet(options);
    }

    public void setOptions(List<String> options) {
        if(options == null){
            return;
        }else if(options instanceof ArrayList){
            this.options = optional((ArrayList<String>)options);
        }else{
            this.options = optional(new ArrayList<>(options));
        }
        
    }
    
    public ArrayList<String> getAggregateOptions() {
        return get(aggregateOptions);
    }
    
    public boolean wasAggregateOptionsSet(){
        return wasSet(aggregateOptions);
    }

    public void setAggregateOptions(List<String> aggregateOptions) {
        if(aggregateOptions == null){
            return;
        }else if(aggregateOptions instanceof ArrayList){
            this.aggregateOptions = optional((ArrayList<String>)aggregateOptions);
        }else{
            this.aggregateOptions = optional(new ArrayList<>(aggregateOptions));
        }
        
    }
    
    @Override
    public ToStringBuilder toStringBuilder(){
        return super.toStringBuilder().append("value", value).append("aggregateValue", aggregateValue).append("options", options).append("aggregateOptions", aggregateOptions);
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
        hash = 97 * hash + Objects.hashCode(this.getAggregateValue());
        hash = 97 * hash + Objects.hashCode(this.getValue());
        hash = 97 * hash + Objects.hashCode(this.getAggregateOptions());
        hash = 97 * hash + Objects.hashCode(this.getOptions());
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
        final MultiFieldDataValue<?, ?> other = (MultiFieldDataValue<?, ?>) obj;
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
        if (!Objects.equals(this.getAggregateValue(), other.getAggregateValue())) {
            return false;
        }
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        if (!Objects.equals(this.getAggregateOptions(), other.getAggregateOptions())) {
            return false;
        }
        if (!Objects.equals(this.getOptions(), other.getOptions())) {
            return false;
        }
        return true;
    }
}
