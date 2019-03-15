package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.FoundationAction;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameter;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.ArrayList;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.*;

public class GetActions extends JacksonBackedWebscript {


    @Override
    protected List<FoundationAction> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<FoundationAction> actions = new ArrayList<>();

        FoundationActionParameter stateParam = new FoundationActionParameter(new ParameterDefinitionImpl(ACTION_PARAM_STATE, DataTypeDefinition.TEXT, true, null));
        FoundationActionParameter aspectParam = new FoundationActionParameter(new ParameterDefinitionImpl(ACTION_PARAM_ASPECT, DataTypeDefinition.TEXT, true, null));

        actions.add(new FoundationAction(ACTION_NAME_EMAIL, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_EMAIL)));
        actions.add(new FoundationAction(ACTION_NAME_ADD_BLOCKS, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_ADD_BLOCKS)));
        actions.add(new FoundationAction(ACTION_NAME_ADD_FIELDS, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_ADD_FIELDS)));
        actions.add(new FoundationAction(ACTION_NAME_CREATE_APPLICANT, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_CREATE_APPLICANT)));

        return actions;
    }
}
