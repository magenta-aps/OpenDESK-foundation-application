package dk.opendesk.foundationapplication.DAO;

import java.io.Serializable;

public class ApplicationChange {

    private String changedField;
    private Serializable oldValue;
    private Serializable newValue;

    public ApplicationChange(String changedField, Serializable oldValue, Serializable newValue) {
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
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
}
