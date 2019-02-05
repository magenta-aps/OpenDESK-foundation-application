/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author martin
 */
public class ApplicationProperty<E> {
    private String id;
    private String label;
    private Class<E> javaType;
    private String type;

    public ApplicationProperty() {
    }

    public ApplicationProperty(String id, String label, Class<E> javaType, String type) {
        this.id = id;
        this.label = label;
        this.javaType = javaType;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Class<E> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<E> javaType) {
        this.javaType = javaType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public ToStringBuilder toStringBuilder(){
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append("id", id).append("label", label).append("javaType", javaType).append("type", type);
        return builder;
    }
    
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
    
}
