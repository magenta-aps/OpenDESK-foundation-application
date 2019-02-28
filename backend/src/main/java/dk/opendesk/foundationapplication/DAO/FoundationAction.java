package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;

import java.util.ArrayList;
import java.util.List;

public class FoundationAction {

    private String name;
    private List<FoundationActionParameter> params;

    public FoundationAction() {
    }

    public FoundationAction(String name, List<ParameterDefinition> parameterDefinitions) {
        this.name = name;
        params = new ArrayList<>();
        for (ParameterDefinition paramDef : parameterDefinitions) {
            params.add(new FoundationActionParameter(paramDef));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FoundationActionParameter> getParams() {
        return params;
    }

    public void setParams(List<FoundationActionParameter> params) {
        this.params = params;
    }
}
