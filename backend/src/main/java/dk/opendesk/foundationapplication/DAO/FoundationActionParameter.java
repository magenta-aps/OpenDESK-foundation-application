package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;


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

    public FoundationActionParameter(String name, QName type, boolean isMandatory, String displayLabel) {
        this.name = name;
        this.type = type.getLocalName();
        this.isMandatory = isMandatory;
        this.displayLabel = displayLabel;
        this.isMultivalued = false;
    }
    public FoundationActionParameter(String name, QName type, boolean isMandatory, String displayLabel, boolean isMultivalued, String parameterConstraintName) {
        this.name = name;
        this.type = type.getLocalName();
        this.isMandatory = isMandatory;
        this.displayLabel = displayLabel;
        this.isMultivalued = isMultivalued;
        this.parameterConstraintName = parameterConstraintName;
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

    /*
    @Override
    public String toString() {
        return "\tFoundationActionParameter\n" +
                "\t\tname='" + name + "'\n" +
                "\t\ttype='" + type + "'\n" +
                "\t\tisMultivalued=" + isMultivalued + "\n" +
                "\t\tisMandatory=" + isMandatory + "\n" +
                "\t\tdisplayLabel='" + displayLabel + "'\n" +
                "\t\tparameterConstraintName='" + parameterConstraintName + "'\n";
    }
    */
}
