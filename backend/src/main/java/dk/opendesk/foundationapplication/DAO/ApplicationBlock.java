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
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class ApplicationBlock extends AbstractBlock{
    private static final Logger LOGGER = Logger.getLogger(ApplicationBlock.class);

    private Optional<List<ApplicationFieldValue>> fields;
    
    public List<ApplicationFieldValue> getFields() {
        return get(fields);
    }

    public boolean wasFieldsSet(){
        return wasSet(fields);
    }

    public void setFields(List<ApplicationFieldValue> fields) {
        this.fields = optional(fields);
    }

    
    public <E, A extends ApplicationFieldValue<E>> A getFunctionalField(Functional<E> describes) {
        List<ApplicationFieldValue> currentFields = get(this.fields);
        for (ApplicationFieldValue blockField : currentFields) {
            if (describes.getFriendlyName().equals(blockField.getDescribes())) {
                if(!describes.getRequiredType().isAssignableFrom(blockField.getTypeAsClass())){
                    LOGGER.warn("Found a match for "+describes+" in "+blockField+" but the types did not match");
                }
                return (A)blockField;
            }
        }
        return null;
    }

    @Override
    public ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("fields", fields);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.getId());
        hash = 59 * hash + Objects.hashCode(this.getLabel());
        hash = 59 * hash + Objects.hashCode(this.getLayout());
        hash = 59 * hash + Objects.hashCode(this.getIcon());
        hash = 59 * hash + Objects.hashCode(this.getCollapsible());
        hash = 59 * hash + Objects.hashCode(this.getRepeatable());
        hash = 59 * hash + Objects.hashCode(this.fields);
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
        final ApplicationBlock other = (ApplicationBlock) obj;
                if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLabel(), other.getLabel())) {
            return false;
        }
        if (!Objects.equals(this.getLayout(), other.getLayout())) {
            return false;
        }
        if (!Objects.equals(this.getIcon(), other.getIcon())) {
            return false;
        }
        if (!Objects.equals(this.getCollapsible(), other.getCollapsible())) {
            return false;
        }
        if (!Objects.equals(this.getRepeatable(), other.getRepeatable())) {
            return false;
        }
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }

    

    
    
}
