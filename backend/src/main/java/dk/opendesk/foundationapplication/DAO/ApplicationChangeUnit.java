package dk.opendesk.foundationapplication.DAO;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ApplicationChangeUnit {

    private String changedField;
    private String oldValue;
    private String newValue;
    private String changeType;

    public ApplicationChangeUnit() {

    }

    public String getChangedField() {
        return changedField;
    }

    public Serializable  getOldValue() {
        return oldValue;
    }

    public Serializable getNewValue() {
        return newValue;
    }

    public String getChangeType() {
        return changeType;
    }


    @JsonIgnore
    public ApplicationChangeUnit setChangedField(String changedField) {
        this.changedField = changedField;
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setNewValue(Object newValue) {
        this.newValue = newValue.toString();
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setNewValue(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.newValue = sdf.format(date);
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setOldValue(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.oldValue = sdf.format(date);
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setOldValue(Object oldValue) {
        this.oldValue = oldValue.toString();
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setChangeType(String changeType) {
        this.changeType = changeType;
        return this;
    }
}
