package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;


public class FoundationActionParameterDefinition<T> implements ParameterDefinition{

    private String name;
    private QName type;
    private Class<T> javaType;
    private boolean isMultiValued;
    private boolean isMandatory;
    private String displayLabel;
    private String parameterConstraintName;

    public FoundationActionParameterDefinition() {}

    public FoundationActionParameterDefinition(ParameterDefinition paramDef) {
        if (paramDef.getName() != null) this.name = paramDef.getName();
        if (paramDef.getType() != null) this.type = paramDef.getType();
        this.isMultiValued = paramDef.isMultiValued();
        this.isMandatory = paramDef.isMandatory();
        if (paramDef.getDisplayLabel() != null) this.displayLabel = paramDef.getDisplayLabel();
        if (paramDef.getParameterConstraintName() != null) this.parameterConstraintName = paramDef.getParameterConstraintName();
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

    public void setMultiValued(boolean multiValued) {
        isMultiValued = multiValued;
    }

    public void setMandatory(boolean mandatory) {
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

}
