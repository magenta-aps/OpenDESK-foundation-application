package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public class TestAction extends ActionExecuterAbstractBase {

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        action.setParameterValue("executed", "true");
        System.out.println("\t\texecuteImpl gets executed!");

    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl("executed", DataTypeDefinition.TEXT,false, getParamDisplayLabel("executed"))); //ville gerne have mandatory = true hvis muligt
    }
}
