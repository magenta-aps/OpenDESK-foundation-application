package dk.opendesk.foundationapplication.DAO;

public class FoundationActionParameterValue<T> extends FoundationActionParameterDefinition<T> {

    private T value;

    public FoundationActionParameterValue() {
    }

    public FoundationActionParameterValue(FoundationActionParameterDefinition<T> parameterDefinition, T value) {
        super.setName(parameterDefinition.getName());
        super.setType(parameterDefinition.getType());
        super.setJavaType(parameterDefinition.getJavaType());
        super.setDisplayLabel(parameterDefinition.getDisplayLabel());
        super.setMandatory(parameterDefinition.isMandatory());
        super.setMultiValued(parameterDefinition.isMultiValued());
        this.value = value;
    }

    /*
    public FoundationActionParameterValue(FoundationActionParameter foundActParam, String value) throws Exception {
        super(
                foundActParam.getName(),
                Utilities.getODFName(foundActParam.getType()),
                foundActParam.isMandatory(),
                foundActParam.getDisplayLabel(),
                foundActParam.isMultivalued(),
                foundActParam.getParameterConstraintName()
        );
        this.value = value;
    }
    */

    public T getValue() {
        return value;
    }

    public void setValue(T  value) {
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
