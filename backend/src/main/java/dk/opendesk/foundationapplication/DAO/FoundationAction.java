package dk.opendesk.foundationapplication.DAO;

import java.util.List;

public class FoundationAction {

    private String name;
    private FoundationActionParameterDefinition stateIdParam;
    private FoundationActionParameterDefinition aspectParam;
    private List<FoundationActionParameterDefinition> params;

    public FoundationAction() {
    }

    public FoundationAction(String name, FoundationActionParameterDefinition stateIdParam, FoundationActionParameterDefinition aspect, List<FoundationActionParameterDefinition> params) {
        this.name = name;
        this.stateIdParam = stateIdParam;
        this.aspectParam = aspect;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FoundationActionParameterDefinition getStateIdParam() {
        return stateIdParam;
    }

    public void setStateIdParam(FoundationActionParameterDefinition stateIdParam) {
        this.stateIdParam = stateIdParam;
    }

    public FoundationActionParameterDefinition getAspectParam() {
        return aspectParam;
    }

    public void setAspectParam(FoundationActionParameterDefinition aspectParam) {
        this.aspectParam = aspectParam;
    }

    public List<FoundationActionParameterDefinition> getParams() {
        return params;
    }

    public void setParams(List<FoundationActionParameterDefinition> params) {
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
