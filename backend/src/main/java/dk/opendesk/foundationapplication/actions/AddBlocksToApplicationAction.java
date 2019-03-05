package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public class AddBlocksToApplicationAction extends ActionExecuterAbstractBase {

    public static final String EXCEPTION_ADD_BLOCKS_FAIL = "addBlocks.action.exception";
    public static final String PARAM_BLOCKS = "blockList";

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        try {
            ApplicationSummary application = foundationBean.getApplicationSummary(actionedUponNodeRef);
            List<ApplicationPropertiesContainer> newBlocks = (List<ApplicationPropertiesContainer>) action.getParameterValue(PARAM_BLOCKS);
            newBlocks.addAll(application.getBlocks());

            Application change = new Application();
            change.parseRef(actionedUponNodeRef);
            change.setBlocks(newBlocks);
            foundationBean.updateApplication(change);

        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_BLOCKS_FAIL);
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_BLOCKS, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_BLOCKS)));
    }
}
