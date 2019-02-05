/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 *
 * @author martin
 */
public class ApplicationPropertyValue<E> extends ApplicationProperty<E> {
    private E value;
    private String layout;

    public ApplicationPropertyValue() {
    }

    public ApplicationPropertyValue(E value) {
        this.value = value;
    }

    public ApplicationPropertyValue(E value, String layout) {
        this.value = value;
        this.layout = layout;
    }

    public ApplicationPropertyValue(E value, String layout, String id, String label, Class<E> javaType, String type) {
        super(id, label, javaType, type);
        this.value = value;
        this.layout = layout;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    @Override
    public ToStringBuilder toStringBuilder(){
        return super.toStringBuilder().append("value", value).append("value(Class)", (value != null ? value.getClass() : null)).append("layout", layout);
    }
    
    
}
