/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ApplicationFieldValue<E> extends ApplicationField<E> {
    public static final String MULTI_VALUES = "odf.field.multi.error";
    
    private Optional<ArrayList<E>> value;
    private Optional<ArrayList<String>> options;

    public ApplicationFieldValue() {
    }
    
    public ArrayList<E> getValue() {
        return get(value);
    }
    
    public boolean isSingleValue(){
        ArrayList<E> values = getValue();
        if(values == null){
            return false;
        }else return values.size() <= 1;
    }
    
    public boolean wasValueSet(){
        return wasSet(value);
    }

    public void setValue(ArrayList<E> value) {
        this.value = optional(value);
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.getValue());
        hash = 31 * hash + Objects.hashCode(this.getOptions());
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
        final ApplicationFieldValue<?> other = (ApplicationFieldValue<?>) obj;
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        if (!Objects.equals(this.getOptions(), other.getOptions())) {
            return false;
        }
        return true;
    }
    
    
    
    @Override
    public ToStringBuilder toStringBuilder(){
        return super.toStringBuilder().append("value", value).append("options", options).append("value(Class)", (getValue() != null ? getValue().getClass() : null));
    }
    
}
