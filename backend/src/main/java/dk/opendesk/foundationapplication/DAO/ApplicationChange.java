package dk.opendesk.foundationapplication.DAO;

import java.io.Serializable;

public class ApplicationChange {

    private String changedField;
    private Serializable oldValue;
    private Serializable newValue;
    private String changeType;

    public ApplicationChange() {

    }

    public ApplicationChange(String changedField, Serializable oldValue, Serializable newValue, String changeType) {
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
    }

    public String getChangedField() {
        return changedField;
    }

    public Serializable getOldValue() {
        return oldValue;
    }

    public Serializable getNewValue() {
        return newValue;
    }

    public String getChangeType() {
        return changeType;
    }
}
