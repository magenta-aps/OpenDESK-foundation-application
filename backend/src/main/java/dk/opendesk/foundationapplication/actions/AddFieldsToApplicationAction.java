package dk.opendesk.foundationapplication.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class AddFieldsToApplicationAction extends ActionExecuterAbstractBase {

    public static final String EXCEPTION_ADD_FIELDS_FAIL = "addFields.action.exception";
    public static final String EXCEPTION_FIELD_OVERLAP = "Field with same id already exists";
    public static final String EXCEPTION_BLOCK_NOT_FOUND = "Block not found";
    public static final String PARAM_FIELDS = "fieldsList";
    public static final String PARAM_BLOCK_ID = "blockId";

        private ApplicationBean applicationBean;

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        Application application;
        try {
            application = applicationBean.getApplication(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_ADD_FIELDS_FAIL, e);
        }
        String blockId = (String) action.getParameterValue(PARAM_BLOCK_ID);

        //finding the block
        ApplicationBlock oldBlock = null;
        for (ApplicationBlock b : application.getBlocks()) {
            if (blockId.equals(b.getId())) {
                oldBlock = b;
            }
        }
        if (oldBlock == null) {
            throw new AlfrescoRuntimeException(EXCEPTION_BLOCK_NOT_FOUND);
        }

        //checking if the fields already exists
        List<ApplicationFieldValue> newFields = new ArrayList<>();
        for (LinkedHashMap field : (ArrayList<LinkedHashMap<String, String>>) action.getParameterValue(PARAM_FIELDS)) {
            ApplicationFieldValue newField = new ApplicationFieldValue();
            for (String key : (Set<String>) field.keySet()) {
                switch (key) {
                    case "id":
                        newField.setId((String) field.get(key));
                        her er jeg igang 
                }
            }
        }
        List<ApplicationFieldValue> oldFields = oldBlock.getFields();

        if (oldFields != null) {
            for (ApplicationFieldValue newField : newFields) {
                for (ApplicationFieldValue oldField : oldFields) {
                    if (newField.getId().equals(oldField.getId())) {
                        throw new AlfrescoRuntimeException(EXCEPTION_FIELD_OVERLAP);
                    }
                }
            }
        }

        //adding the fields
        ApplicationBlock newBlock = new ApplicationBlock();
        newBlock.setId(blockId);
        newBlock.setFields(newFields);

        Application change = new Application();
        change.parseRef(actionedUponNodeRef);
        change.setBlocks(Collections.singletonList(newBlock));
        try {
            applicationBean.updateApplication(change);
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
