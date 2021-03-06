package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dk.opendesk.foundationapplication.Utilities.DATE_FORMAT_STRING;

public class ApplicationChangeUnit {

    private String changedField;
    private String oldValue;
    private String newValue;
    private String newValueLink;
    private String changeType;

    public ApplicationChangeUnit() {

    }

    public String getChangedField() {
        return changedField;
    }

    public Serializable  getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getNewValueLink() {
        return newValueLink;
    }


    public ApplicationChangeUnit setOldValue(String oldValue) {
        this.oldValue = oldValue;
        return this;
    }

    public ApplicationChangeUnit setNewValue(String newValue) {
        this.newValue = newValue;
        return this;
    }

    public ApplicationChangeUnit setChangedField(String changedField) {
        this.changedField = changedField;
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setNewValueWithObject(Object newValue) {
        this.newValue = newValue.toString();
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setNewValueWithDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
        this.newValue = sdf.format(date);
        return this;
    }

    public ApplicationChangeUnit setNewValueLink(String link) {
        this.newValueLink = link;
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setOldValueWithDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
        this.oldValue = sdf.format(date);
        return this;
    }

    @JsonIgnore
    public ApplicationChangeUnit setOldValueWithObject(Object oldValue) {
        this.oldValue = oldValue.toString();
        return this;
    }

    public ApplicationChangeUnit setChangeType(String changeType) {
        this.changeType = changeType;
        return this;
    }

    @Override
    public String toString() {
        return "\t\tApplicationChangeUnit\n" +
                "\t\t\tchangedField = '" + changedField + "'\n" +
                "\t\t\toldValue     = '" + oldValue + "'\n" +
                "\t\t\tnewValue     = '" + newValue + "'\n" +
                "\t\t\tnewValueLink = '" + newValueLink + "'\n" +
                "\t\t\tchangeType   = '" + changeType + "'\n";
    }
}
