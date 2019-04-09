package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.alfresco.service.cmr.repository.NodeRef;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.DATE_FORMAT_STRING;

public class ApplicationChange extends ApplicationReference {

    private String changeType;
    private String timeStamp;
    private String modifierId;
    private String modifier;
    private List<ApplicationChangeUnit> changes;

    public ApplicationChange() {

    }

    public void addApplicationChange(ApplicationChangeUnit change) {
        changes.add(change);
    }

    public String getChangeType() {
        return changeType;
    }

    public Serializable getTimeStamp() {
        return timeStamp;
    }

    public String getModifierId() {
        return modifierId;
    }

    public String getModifier() {
        return modifier;
    }

    public List<ApplicationChangeUnit> getChanges() {
        return changes;
    }

    public ApplicationChange setChangeType(String changeType) {
        this.changeType = changeType;
        return this;
    }

    public ApplicationChange setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public ApplicationChange setModifierId(String modifierId) {
        this.modifierId = modifierId;
        return this;
    }

    public ApplicationChange setChanges(List<ApplicationChangeUnit> changes) {
        this.changes = changes;
        return this;
    }

    @JsonIgnore
    public ApplicationChange setTimeStamp(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
        this.timeStamp = sdf.format(date);
        return this;
    }

    @JsonIgnore
    public ApplicationChange setModifierIdWithNodeRef(NodeRef nodeRef) {
        this.modifierId = nodeRef.toString();
        return this;
    }

    public ApplicationChange setModifier(String userName) {
        this.modifier = userName;
        return this;
    }

    @JsonIgnore
    public ApplicationChange setChangeList(List<ApplicationChangeUnit> changes) {
        this.changes = changes;
        return this;
    }

    @Override
    public String toString() {
        return "---\nApplicationChange\n" +
                "\tchangetype = '" + changeType + "'\n" +
                "\ttimeStamp = '" + timeStamp + "'\n" +
                "\tmodifierId = '" + modifierId + "'\n" +
                "\tmodifier   = '" + modifier + "'\n" +
                "\tchanges    =\n" + changes + "'\n";
    }
}
