package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.math3.stat.inference.TestUtils;

import java.util.List;

public class AddFieldsToApplicationAction extends ActionExecuterAbstractBase {

    public static final String PARAM_FIELDS = "fieldsList";
    public static final String PARAM_BLOCK_ID = "blockId";

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        List<ApplicationPropertyValue> fields = (List<ApplicationPropertyValue>) action.getParameterValue(PARAM_FIELDS);
        //Application change = Utilities.buildChange();

    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        //paramList.add(new ParameterDefinitionImpl(PARAM_BLOCK_ID, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_BLOCK_ID)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FIELDS, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_FIELDS)));
    }
}
