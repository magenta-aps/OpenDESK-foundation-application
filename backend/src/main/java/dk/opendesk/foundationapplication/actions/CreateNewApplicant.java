package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
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
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_SUBJECT;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE_MODEL;


public class CreateNewApplicant extends ActionExecuterAbstractBase {

    private static final String EXCEPTION_CREATE_APPLICANT_FAIL = "createNewApplicant.action.exception";

    private ApplicationBean applicationBean;
    private PersonBean personBean;
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setPersonBean(PersonBean personBean) {
        this.personBean = personBean;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        Application application;
        try {
            application = applicationBean.getApplication(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_CREATE_APPLICANT_FAIL, e);
        }

        Map<QName, Serializable> personProps = personBean.createPersonProperties(
                "",
                application.firstName().getSingleValue(),
                application.lastName().getSingleValue(),
                application.emailTo().getSingleValue(),
                application.phoneNumber().getSingleValue()
        );

        String password = personBean.createPerson(personProps);

        HashMap<String, Serializable> model = (HashMap) action.getParameterValue(PARAM_TEMPLATE_MODEL);
        if (model == null) {
            model = new HashMap<>();
        }
        model.put("userName", personProps.get(getCMName("userName")));
        model.put("password", password);

        Map<String, Serializable> emailActionParams = new HashMap<>();
        emailActionParams.put(PARAM_SUBJECT, action.getParameterValue(PARAM_SUBJECT));
        emailActionParams.put(PARAM_TEMPLATE, action.getParameterValue(PARAM_TEMPLATE));
        emailActionParams.put(PARAM_TEMPLATE_MODEL, model);
        Action emailAction = serviceRegistry.getActionService().createAction(ACTION_NAME_EMAIL, emailActionParams);
        serviceRegistry.getActionService().executeAction(emailAction, actionedUponNodeRef);


    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));

    }
}
