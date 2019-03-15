package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;

import java.io.Serializable;

public class FoundationActionParameterValue extends FoundationActionParameter {

    private Serializable value;

    public FoundationActionParameterValue() {
        super();
    }

    public FoundationActionParameterValue(ParameterDefinition parameterDefinition, String value) {
        super(parameterDefinition);
        this.value = value;
    }

    public FoundationActionParameterValue(FoundationActionParameter foundActParam, String value) {
        super(
                foundActParam.getName(),
                foundActParam.getType(),
                foundActParam.isMultivalued(),
                foundActParam.isMandatory(),
                foundActParam.getDisplayLabel(),
                foundActParam.getParameterConstraintName()
        );
        this.value = value;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /*
    @Override
    public String toString() {
        return "\tFoundationActionParameterValue\n" +
                "\t\tname='" + getName() + "'\n" +
                "\t\ttype='" + getType() + "'\n" +
                "\t\tisMultivalued=" + isMultivalued() + "\n" +
                "\t\tisMandatory=" + isMandatory() + "\n" +
                "\t\tdisplayLabel='" + getDisplayLabel() + "'\n" +
                "\t\tparameterConstraintName='" + getParameterConstraintName() + "'\n" +
                "\t\tvalue='" + value + "'\n";
    }
    */
}
