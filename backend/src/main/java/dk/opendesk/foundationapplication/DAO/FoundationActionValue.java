package dk.opendesk.foundationapplication.DAO;

import java.util.List;

public class FoundationActionValue {

    private String name;
    private FoundationActionParameterValue stateIdParam;
    private FoundationActionParameterValue aspectParam;
    private List<FoundationActionParameterValue> params;

    public FoundationActionValue() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FoundationActionParameterValue getStateIdParam() {
        return stateIdParam;
    }

    public void setStateIdParam(FoundationActionParameterValue stateIdParam) {
        this.stateIdParam = stateIdParam;
    }

    public FoundationActionParameterValue getAspectParam() {
        return aspectParam;
    }

    public void setAspectParam(FoundationActionParameterValue aspectParam) {
        this.aspectParam = aspectParam;
    }

    public List<FoundationActionParameterValue> getParams() {
        return params;
    }

    public void setParams(List<FoundationActionParameterValue> params) {
        this.params = params;
    }
}
