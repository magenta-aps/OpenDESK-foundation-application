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
public class ApplicationPropertiesContainer extends DAOType{
    private static final Logger LOGGER = Logger.getLogger(ApplicationPropertiesContainer.class);
    
    private Optional<String> id;
    private Optional<String> label;
    private Optional<String> layout;
    private Optional<List<ApplicationPropertyValue>> fields;

    public ApplicationPropertiesContainer() {
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

    
    public List<ApplicationPropertyValue> getFields() {
        return get(fields);
    }

    public boolean wasFieldsSet(){
        return wasSet(fields);
    }

    public void setFields(List<ApplicationPropertyValue> fields) {
        this.fields = optional(fields);
    }
    
    public final <E, A extends ApplicationPropertyValue<E>> A getFunctionalField(Functional<E> describes) {
        List<ApplicationPropertyValue> fields = get(this.fields);
        for (ApplicationPropertyValue blockField : fields) {
            if (describes.getFriendlyName().equals(blockField.getDescribes())) {
                if(!describes.getRequiredType().isAssignableFrom(blockField.getJavaType())){
                    LOGGER.warn("Found a match for "+describes+" in "+blockField+" but the types did not match");
                }
                return (A)blockField;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.getId());
        hash = 89 * hash + Objects.hashCode(this.getLabel());
        hash = 89 * hash + Objects.hashCode(this.getLayout());
        hash = 89 * hash + Objects.hashCode(this.getFields());
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
        final ApplicationPropertiesContainer other = (ApplicationPropertiesContainer) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLabel(), other.getLabel())) {
            return false;
        }
        if (!Objects.equals(this.getLayout(), other.getLayout())) {
            return false;
        }
        if (!Objects.equals(this.getFields(), other.getFields())) {
            return false;
        }
        return true;
    }
    
    

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append("id", id).append("label", label).append("layout", layout).append("fields", fields);
        return builder.toString();
    }
    
    
}
