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
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class ApplicationBlockSpecification extends AbstractBlock {
    private static final Logger LOGGER = Logger.getLogger(ApplicationBlockSpecification.class);
    
    
    private Optional<List<ApplicationField>> fields;
    
    public List<ApplicationField> getFields() {
        return get(fields);
    }

    public boolean wasFieldsSet(){
        return wasSet(fields);
    }

    public void setFields(List<ApplicationField> fields) {
        this.fields = optional(fields);
    }
    
    public <E, A extends ApplicationField<E>> A getFunctionalField(Functional<E> describes) {
        List<ApplicationField> currentFields = get(this.fields);
        for (ApplicationField blockField : currentFields) {
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
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getId());
        hash = 97 * hash + Objects.hashCode(this.getLabel());
        hash = 97 * hash + Objects.hashCode(this.getLayout());
        hash = 97 * hash + Objects.hashCode(this.getIcon());
        hash = 97 * hash + Objects.hashCode(this.getCollapsible());
        hash = 97 * hash + Objects.hashCode(this.getRepeatable());
        hash = 97 * hash + Objects.hashCode(this.fields);
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
        final ApplicationBlockSpecification other = (ApplicationBlockSpecification) obj;
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
        if (!Objects.equals(this.getFields(), other.getFields())) {
            return false;
        }
        return true;
    }
    
    
}
