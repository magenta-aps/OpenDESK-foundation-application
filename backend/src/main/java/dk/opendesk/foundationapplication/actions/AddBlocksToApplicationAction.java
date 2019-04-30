package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationBlockSpecification;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.MultiFieldDataValue;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

import static dk.opendesk.foundationapplication.actions.AddFieldsToApplicationAction.EXCEPTION_FIELD_OVERLAP;
import dk.opendesk.foundationapplication.beans.ApplicationBean;

public class AddBlocksToApplicationAction extends ActionExecuterAbstractBase {

    public static final String EXCEPTION_ADD_BLOCKS_FAIL = "addBlocks.action.exception";
    public static final String EXCEPTION_BLOCK_OVERLAP = "Block with same id already exists";
    public static final String PARAM_BLOCKS = "blockList";

    private ApplicationBean applicationBean;

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        ApplicationSummary application;
        try {
            application = applicationBean.getApplicationSummary(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_BLOCKS_FAIL, e);
        }

        List<ApplicationBlock> oldBlocks = application.getBlocks();
        List<ApplicationBlockSpecification> newBlocks = (List<ApplicationBlockSpecification>) action.getParameterValue(PARAM_BLOCKS);

        //checking if the new blocks already exists
        for (ApplicationBlock oldBlock : oldBlocks) {
            for (ApplicationBlockSpecification newBlock : newBlocks) {
                if (oldBlock.getId().equals(newBlock.getId())) {
                    throw new AlfrescoRuntimeException(EXCEPTION_BLOCK_OVERLAP);
                }
                //checking if the new fields already exists
                List<MultiFieldDataValue> newBlockFields = newBlock.getFields();
                List<ApplicationFieldValue> oldBlockFields = oldBlock.getFields();
                if (newBlockFields == null | oldBlockFields == null) {
                    continue;
                }
                for (ApplicationFieldValue oldBlockField : oldBlockFields) {
                    for (MultiFieldDataValue newBlockField : newBlockFields) {
                        if (oldBlockField.getId().equals(newBlockField.getId())) {
                            throw new AlfrescoRuntimeException(EXCEPTION_FIELD_OVERLAP);
                        }
                    }
                }
            }
        }

        //adding the new blocks

        try {
            for(ApplicationBlockSpecification newBlock : newBlocks){
                applicationBean.addBlockToApplication(application, newBlock);
            }
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_BLOCKS_FAIL, e);
        }

    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_BLOCKS, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_BLOCKS)));
    }
}
