package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.opendesk.foundationapplication.Utilities;
import org.alfresco.service.cmr.repository.NodeRef;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ApplicationChange extends ApplicationReference {

    private String changeType;
    private String timesStamp;
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

    public Serializable getTimesStamp() {
        return timesStamp;
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

    public ApplicationChange setTimesStamp(String timesStamp) {
        this.timesStamp = timesStamp;
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
        this.timesStamp = Utilities.UNIVERAL_DATE_FORMAT.format(date);
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
                "\ttimesStamp = '" + timesStamp + "'\n" +
                "\tmodifierId = '" + modifierId + "'\n" +
                "\tmodifier   = '" + modifier + "'\n" +
                "\tchanges    =\n" + changes + "'\n";
    }
}
