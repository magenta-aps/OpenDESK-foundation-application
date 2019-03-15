package dk.opendesk.foundationapplication.DAO;

import org.alfresco.service.cmr.action.ParameterDefinition;

import java.util.ArrayList;
import java.util.List;

public class FoundationAction {

    private String name;
    private FoundationActionParameter stateIdParam;
    private FoundationActionParameter aspectParam;
    private List<FoundationActionParameter> params;

    public FoundationAction() {
    }

    public FoundationAction(String name, FoundationActionParameter stateIdParam, FoundationActionParameter aspect, List<ParameterDefinition> parameterDefinitions) {
        this.name = name;
        this.stateIdParam = stateIdParam;
        this.aspectParam = aspect;
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

    public FoundationActionParameter getStateIdParam() {
        return stateIdParam;
    }

    public void setStateIdParam(FoundationActionParameter stateIdParam) {
        this.stateIdParam = stateIdParam;
    }

    public FoundationActionParameter getAspectParam() {
        return aspectParam;
    }

    public void setAspectParam(FoundationActionParameter aspectParam) {
        this.aspectParam = aspectParam;
    }

    public List<FoundationActionParameter> getParams() {
        return params;
    }

    public void setParams(List<FoundationActionParameter> params) {
        this.params = params;
    }

    /*
    @Override
    public String toString() {
        return "FoundationAction\n" +
                "\tname   = '" + name + "'\n'" +
                "\tstateIdParam   =\n " + stateIdParam + "\n" +
                "\taspectParam   =\n " + aspectParam + "\n" +
                "\totherParams (list) =\n" + params;
    }
    */
}
