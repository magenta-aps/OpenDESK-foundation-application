package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;


public class FoundationActionParameterDefinition<T> implements ParameterDefinition{

    private String name;
    private QName type;
    private Class<T> javaType;
    private Boolean isMultiValued;
    private Boolean isMandatory;
    private String displayLabel;
    private String parameterConstraintName;

    public FoundationActionParameterDefinition() {}

    /**
     * Constructor
     *
     * @param name          the name of the parameter
     * @param javaType  the type of the parameter
     * @param displayLabel  the display label
     */
    public FoundationActionParameterDefinition(
            String name,
            QName type,
            Class<T> javaType,
            boolean isMandatory,
            String displayLabel)
    {
        this.name = name;
        this.type = type;
        this.javaType = javaType;
        this.displayLabel = displayLabel;
        this.isMandatory = isMandatory;
        this.isMultiValued = false;
    }

    /**
     * Constructor
     *
     * @param name          the name of the parameter
     * @param javaType  the type of the parameter
     * @param displayLabel  the display label
     */
    public FoundationActionParameterDefinition(
            String name,
            QName type,
            Class<T> javaType,
            boolean isMandatory,
            String displayLabel,
            boolean isMultiValued)
    {
        this(name, type, javaType, isMandatory, displayLabel);
        this.isMultiValued = isMultiValued;
    }

    /**
     * Constructor
     *
     * @param name String
     * @param javaType String
     * @param isMandatory boolean
     * @param displayLabel String
     * @param isMultiValued boolean
     * @param parameterConstraintName String
     */
    public FoundationActionParameterDefinition (
            String name,
            QName type,
            Class<T> javaType,
            boolean isMandatory,
            String displayLabel,
            boolean isMultiValued,
            String parameterConstraintName)
    {
        this(name, type, javaType, isMandatory, displayLabel, isMultiValued);
        this.parameterConstraintName = parameterConstraintName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public void setJavaType(Class<T> javaType) {
        this.javaType = javaType;
    }

    public void setMultiValued(Boolean multiValued) {
        isMultiValued = multiValued;
    }

    public void setMandatory(Boolean mandatory) {
        isMandatory = mandatory;
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public void setParameterConstraintName(String parameterConstraintName) {
        this.parameterConstraintName = parameterConstraintName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public QName getType() {
        return type;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public boolean isMultiValued() {
        return isMultiValued;
    }

    @Override
    public boolean isMandatory() {
        return isMandatory;
    }

    @Override
    public String getDisplayLabel() {
        return displayLabel;
    }

    @Override
    public String getParameterConstraintName() {
        return parameterConstraintName;
    }

    /*
    }

    public FoundationActionParameterDefinition(ParameterDefinition parameterDef) {
        name = parameterDef.getName();
        type = parameterDef.getType().getLocalName();
        isMultivalued = parameterDef.isMultiValued();
        isMandatory = parameterDef.isMandatory();
        displayLabel = parameterDef.getDisplayLabel();
        parameterConstraintName = parameterDef.getParameterConstraintName();
    }

    public FoundationActionParameterDefinition(String name, QName type, boolean isMandatory, String displayLabel) {
        this.name = name;
        this.type = type.getLocalName();
        this.isMandatory = isMandatory;
        this.displayLabel = displayLabel;
        this.isMultivalued = false;
    }
    public FoundationActionParameterDefinition(String name, QName type, boolean isMandatory, String displayLabel, boolean isMultivalued, String parameterConstraintName) {
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

    public Boolean isMultivalued() {
        return isMultivalued;
    }

    public void setMultivalued(boolean multivalued) {
        isMultivalued = multivalued;
    }

    public Boolean isMandatory() {
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
