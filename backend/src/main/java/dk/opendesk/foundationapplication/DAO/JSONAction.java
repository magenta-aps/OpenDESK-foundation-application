package dk.opendesk.foundationapplication.DAO;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class JSONAction extends Reference {

    private Optional<String> id;
    private Optional<String> name;
    private Optional<Map<String, Serializable>> parameters;

    public JSONAction() {
    }

    public String getId() {
        return get(id);
    }

    public boolean wasIdSet(){
        return wasSet(id);
    }

    public void setId(String id) {
        this.id = optional(id);
    }

    public String getName() {
        return get(name);
    }

    public boolean wasNameSet(){
        return wasSet(name);
    }

    public void setName(String name) {
        this.name = optional(name);
    }

    public Map<String,Serializable> getParameters() {
        return get(parameters);
    }

    public boolean wasParametersSet(){
        return wasSet(parameters);
    }

    public void setParameters(Map<String,Serializable> parameters) {
        this.parameters = optional(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JSONAction that = (JSONAction) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, parameters);
    }
}
