package dk.opendesk.foundationapplication.actions;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
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

import static dk.opendesk.foundationapplication.actions.AddFieldsToApplicationAction.EXCEPTION_FIELD_OVERLAP;

public class AddBlocksToApplicationAction extends ActionExecuterAbstractBase {

    public static final String EXCEPTION_ADD_BLOCKS_FAIL = "addBlocks.action.exception";
    public static final String EXCEPTION_BLOCK_OVERLAP = "Block with same id already exists";
    public static final String PARAM_BLOCKS = "blockList";

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        ApplicationSummary application;
        try {
            application = foundationBean.getApplicationSummary(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_BLOCKS_FAIL, e);
        }

        List<ApplicationPropertiesContainer> oldBlocks = application.getBlocks();
        List<ApplicationPropertiesContainer> newBlocks = (List<ApplicationPropertiesContainer>) action.getParameterValue(PARAM_BLOCKS);

        //checking if the new blocks already exists
        for (ApplicationPropertiesContainer oldBlock : oldBlocks) {
            for (ApplicationPropertiesContainer newBlock : newBlocks) {
                if (oldBlock.getId().equals(newBlock.getId())) {
                    throw new AlfrescoRuntimeException(EXCEPTION_BLOCK_OVERLAP);
                }
                //checking if the new fields already exists
                List<ApplicationPropertyValue> newBlockFields = newBlock.getFields();
                List<ApplicationPropertyValue> oldBlockFields = oldBlock.getFields();
                if (newBlockFields == null | oldBlockFields == null) {
                    continue;
                }
                for (ApplicationPropertyValue oldBlockField : oldBlockFields) {
                    for (ApplicationPropertyValue newBlockField : newBlockFields) {
                        if (oldBlockField.getId().equals(newBlockField.getId())) {
                            throw new AlfrescoRuntimeException(EXCEPTION_FIELD_OVERLAP);
                        }
                    }
                }
            }
        }

        //adding the new blocks
        Application change = new Application();
        change.parseRef(actionedUponNodeRef);
        change.setBlocks(newBlocks);
        try {
            foundationBean.updateApplication(change);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_BLOCKS_FAIL, e);
        }

    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_BLOCKS, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_BLOCKS)));
    }
}
