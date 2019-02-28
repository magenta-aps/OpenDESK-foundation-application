package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;

public class FoundationActionParameter {

    private String name;
    private String type;
    private boolean isMultivalued;
    private boolean isMandatory;
    private String displayLabel;
    private String parameterConstraintName;

    public FoundationActionParameter() {
    }

    public FoundationActionParameter(ParameterDefinition parameterDef) {
        name = parameterDef.getName();
        type = parameterDef.getType().getLocalName();
        isMultivalued = parameterDef.isMultiValued();
        isMandatory = parameterDef.isMandatory();
        displayLabel = parameterDef.getDisplayLabel();
        parameterConstraintName = parameterDef.getParameterConstraintName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMultivalued() {
        return isMultivalued;
    }

    public void setMultivalued(boolean multivalued) {
        isMultivalued = multivalued;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getParameterConstraintName() {
        return parameterConstraintName;
    }

    public void setParameterConstraintName(String parameterConstraintName) {
        this.parameterConstraintName = parameterConstraintName;
    }
}
