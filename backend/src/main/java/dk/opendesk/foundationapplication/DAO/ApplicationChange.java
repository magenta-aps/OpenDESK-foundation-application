package dk.opendesk.foundationapplication.DAO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.alfresco.service.cmr.repository.NodeRef;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ApplicationChange extends ApplicationReference {

    private String timesStamp;
    private String modifierId;
    private String modifier;
    private List<ApplicationChangeUnit> changes;

    public ApplicationChange() {

    }

    public void addApplicationChange(ApplicationChangeUnit change) {
        changes.add(change);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.timesStamp = sdf.format(date);
        return this;
    }

    @JsonIgnore
    public ApplicationChange setModifierId(NodeRef nodeRef) {
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
                "\ttimesStamp='" + timesStamp + '\n' +
                "\tmodifierId='" + modifierId + '\n' +
                "\tmodifier='" + modifier + '\n' +
                "\tchanges=\n" + changes +
                '\n';
    }
}
