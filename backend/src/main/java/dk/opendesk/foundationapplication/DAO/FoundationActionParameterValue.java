package dk.opendesk.foundationapplication.DAO;

public class FoundationActionParameterValue<T> extends FoundationActionParameterDefinition<T> {

    private T value;

    public FoundationActionParameterValue() {
    }

    public FoundationActionParameterValue(FoundationActionParameterDefinition<T> parameterDefinition) {
        super.setName(parameterDefinition.getName());
        super.setType(parameterDefinition.getType());
        super.setJavaType(parameterDefinition.getJavaType());
        super.setDisplayLabel(parameterDefinition.getDisplayLabel());
        super.setMandatory(parameterDefinition.isMandatory());
        super.setMultiValued(parameterDefinition.isMultiValued());
    }

    public FoundationActionParameterValue(FoundationActionParameterDefinition<T> parameterDefinition, T value) {
        this(parameterDefinition);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T  value) {
        this.value = value;
    }

}
