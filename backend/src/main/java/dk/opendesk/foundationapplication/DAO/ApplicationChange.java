package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonIgnore;

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

    @JsonIgnore
    public ApplicationChange setModifier(String userName) {
        this.modifier = userName;
        return this;
    }

    @JsonIgnore
    public ApplicationChange setChangeList(List<ApplicationChangeUnit> changes) {
        this.changes = changes;
        return this;
    }

}
