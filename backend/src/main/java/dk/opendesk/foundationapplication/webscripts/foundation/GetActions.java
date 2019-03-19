package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.FoundationAction;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.util.ArrayList;
import java.util.List;

import static dk.opendesk.foundationapplication.Utilities.*;

public class GetActions extends JacksonBackedWebscript {


    @Override
    protected List<FoundationAction> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        List<FoundationAction> actions = new ArrayList<>();

        FoundationActionParameterDefinition stateParam = new FoundationActionParameterDefinition<>(ACTION_PARAM_STATE, DataTypeDefinition.TEXT, String.class, true, null);
        FoundationActionParameterDefinition aspectParam = new FoundationActionParameterDefinition<>(ACTION_PARAM_ASPECT, DataTypeDefinition.TEXT, String.class, true, null);

        actions.add(new FoundationAction(ACTION_NAME_EMAIL, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_EMAIL)));
        actions.add(new FoundationAction(ACTION_NAME_ADD_BLOCKS, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_ADD_BLOCKS)));
        actions.add(new FoundationAction(ACTION_NAME_ADD_FIELDS, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_ADD_FIELDS)));
        actions.add(new FoundationAction(ACTION_NAME_CREATE_APPLICANT, stateParam, aspectParam, getActionBean().getActionParameters(ACTION_NAME_CREATE_APPLICANT)));

        return actions;
    }
}
