package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.repo.beans.PersonBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.opendesk.foundationapplication.Utilities.ACTION_NAME_EMAIL;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_SUBJECT;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE_MODEL;

public class CreateNewApplicant extends ActionExecuterAbstractBase {

    private static final String EXCEPTION_CREATE_APPLICANT_FAIL = "createNewApplicant.action.exception";

    private ApplicationBean applicationBean;
    private ActionBean actionBean;
    private PersonBean personBean;
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPersonBean(PersonBean personBean) {
        this.personBean = personBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setActionBean(ActionBean actionBean) {
        this.actionBean = actionBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        Application application;
        try {
            application = applicationBean.getApplication(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_CREATE_APPLICANT_FAIL, e);
        }

        Map<QName, Serializable> props = personBean.createPersonProperties(
                "",
                application.firstName().getValue(),
                application.lastName().getValue(),
                application.emailTo().getValue(),
                application.phoneNumber().getValue()
        );

        String password = personBean.createPerson(props);

        HashMap<String, Serializable> model = new HashMap<>();
        model.put("userName", props.get("userName"));
        model.put("password", password);

        Map<String, Serializable> params = new HashMap<>();
        params.put(PARAM_SUBJECT, action.getParameterValue(PARAM_SUBJECT));
        params.put(PARAM_TEMPLATE, action.getParameterValue(PARAM_TEMPLATE));
        params.put(PARAM_TEMPLATE_MODEL, model);
        Action emailAction = serviceRegistry.getActionService().createAction(ACTION_NAME_EMAIL, params);
        serviceRegistry.getActionService().executeAction(emailAction, actionedUponNodeRef);


    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));

    }
}
