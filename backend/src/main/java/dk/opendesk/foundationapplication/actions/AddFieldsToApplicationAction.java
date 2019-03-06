package dk.opendesk.foundationapplication.actions;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.Utilities.ApplicationChangeBuilder;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Collections;
import java.util.List;

public class AddFieldsToApplicationAction extends ActionExecuterAbstractBase {

    public static final String EXCEPTION_ADD_FIELDS_FAIL = "addFields.action.exception";
    public static final String EXCEPTION_FIELD_OVERLAP = "Field with same id already exists";
    public static final String EXCEPTION_BLOCK_NOT_FOUND = "Block not found";
    public static final String PARAM_FIELDS = "fieldsList";
    public static final String PARAM_BLOCK_ID = "blockId";

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        Application application;
        try {
            application = foundationBean.getApplication(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_FIELDS_FAIL, e);
        }
        String blockId = (String) action.getParameterValue(PARAM_BLOCK_ID);

        //finding the block
        ApplicationPropertiesContainer oldBlock = null;
        for (ApplicationPropertiesContainer b : application.getBlocks()) {
            if (blockId.equals(b.getId())) {
                oldBlock = b;
            }
        }
        if (oldBlock == null) {
            throw new AlfrescoRuntimeException(EXCEPTION_BLOCK_NOT_FOUND);
        }

        //checking if the fields already exists
        List<ApplicationPropertyValue> newFields = (List<ApplicationPropertyValue>) action.getParameterValue(PARAM_FIELDS);
        List<ApplicationPropertyValue> oldFields = oldBlock.getFields();

        if (oldFields != null) {
            for (ApplicationPropertyValue newField : newFields) {
                for (ApplicationPropertyValue oldField : oldFields) {
                    if (newField.getId().equals(oldField.getId())) {
                        throw new AlfrescoRuntimeException(EXCEPTION_FIELD_OVERLAP);
                    }
                }
            }
        }

        //adding the fields
        ApplicationPropertiesContainer newBlock = new ApplicationPropertiesContainer();
        newBlock.setId(blockId);
        newBlock.setFields(newFields);

        Application change = new Application();
        change.parseRef(actionedUponNodeRef);
        change.setBlocks(Collections.singletonList(newBlock));
        try {
            foundationBean.updateApplication(change);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_FIELDS_FAIL, e);
        }


    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_BLOCK_ID, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_BLOCK_ID)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FIELDS, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_FIELDS)));
    }
}
