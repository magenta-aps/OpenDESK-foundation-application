package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.List;

public class ApplicationChangeList extends ApplicationReference {

    private Serializable timesStamp;
    private NodeRef modifier;
    private List<ApplicationChange> changes;

    public ApplicationChangeList() {
    
    }

    public ApplicationChangeList(Serializable timesStamp, NodeRef modifier, List<ApplicationChange> changes) {
        this.timesStamp = timesStamp;
        this.modifier = modifier;
        this.changes = changes;
    }

    public void addApplicationChange(ApplicationChange change) {
        changes.add(change);
    }

    public Serializable getTimesStamp() {
        return timesStamp;
    }

    public NodeRef getModifier() {
        return modifier;
    }

    public List<ApplicationChange> getChanges() {
        return changes;
    }



}
