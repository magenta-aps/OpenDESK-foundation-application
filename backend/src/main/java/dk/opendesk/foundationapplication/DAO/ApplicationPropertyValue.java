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
public class ApplicationPropertyValue<E> extends ApplicationProperty<E> {
    private Optional<E> value;

    public ApplicationPropertyValue() {
    }

    public ApplicationPropertyValue(Optional<E> value, Optional<String> id, Optional<String> label, Optional<Class<E>> javaType, Optional<String> type, Optional<String> function, Optional<List<E>> allowedValues, Optional<String> layout) {
        super(id, label, javaType, type, function, allowedValues, layout);
        this.value = value;
    }

    public E getValue() {
        return get(value);
    }
    
    public boolean wasValueSet(){
        return wasSet(value);
    }

    public void setValue(E value) {
        this.value = optional(value);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.getValue());
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
        final ApplicationPropertyValue<?> other = (ApplicationPropertyValue<?>) obj;
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        return true;
    }
    
    
    
    @Override
    public ToStringBuilder toStringBuilder(){
        return super.toStringBuilder().append("value", value).append("value(Class)", (value != null ? value.getClass() : null));
    }
    
}
