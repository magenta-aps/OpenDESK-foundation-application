package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.enums.Functional;
import org.alfresco.repo.action.ParameterDefinitionImpl;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;

public class EmailAction extends MailActionExecuter {
    public static final String EXCEPTION_SEND_EMAIL_FAIL = "email.action.send.exception";
    public static final String EXCEPTION_SAVE_EMAIL_FAIL = "email.action.save.exception";

    private static final String RECIPIENT = "recipient";
    private static final String EMAIL_TYPE = "emailTemplateType";
    private static ThreadLocal<Pair<MimeMessage,NodeRef>> threadLocal = new ThreadLocal<>();

    private ApplicationBean applicationBean;
    private ActionBean actionBean;

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setActionBean(ActionBean actionBean) {
        this.actionBean = actionBean;
    }


    /**
     * Send an email message
     *
     * @throws org.alfresco.error.AlfrescoRuntimeException
     */
    @Override
    protected void executeImpl(final Action ruleAction, final NodeRef actionedUponNodeRef) {

        try {
            Application application = applicationBean.getApplication(actionedUponNodeRef);

            Map<String, Serializable> model = new HashMap<>();
            for(ApplicationPropertiesContainer block : application.getBlocks()){
                for(ApplicationPropertyValue field : block.getFields()){
                    model.put(block.getLabel()+":"+field.getLabel(), field.getValue().toString());//Parse instead of tostring
                    model.put(field.getId(), field.getValue().toString());
                }
            }
            model.put("subject", ruleAction.getParameterValue(PARAM_SUBJECT)); //todo temp subject for temp template
            model.put("body", "Bye bye" ); //todo temp body for temp template
            ruleAction.setParameterValue(PARAM_TEMPLATE_MODEL, (Serializable) model);

            ruleAction.setParameterValue(PARAM_TO, application.getFunctionalField(Functional.email_to()).getValue());

            //TODO skal det tjekkes at template, subject og from er blevet sat inden eksekvering
            super.executeImpl(ruleAction,actionedUponNodeRef);

        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_SEND_EMAIL_FAIL, e);
        }

    }

    @Override
    public MimeMessageHelper prepareEmail(final Action ruleAction , final NodeRef actionedUponNodeRef, final Pair<String, Locale> recipient, final Pair<InternetAddress, Locale> sender) {
        MimeMessageHelper mimeMessageHelper = super.prepareEmail(ruleAction, actionedUponNodeRef, recipient, sender);
        threadLocal.set(new Pair<>(mimeMessageHelper.getMimeMessage(),actionedUponNodeRef));
        return mimeMessageHelper;
    }


    @Override
    protected void onSend() {
        Pair message = threadLocal.get();
        try {
            actionBean.saveEmailCopy((MimeMessage) message.getFirst(), (NodeRef) message.getSecond());
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_SAVE_EMAIL_FAIL, e);
        }
        super.onSend();
    }



    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        //paramList.add(new ParameterDefinitionImpl(PARAM_TO, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CC, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CC)));
        paramList.add(new ParameterDefinitionImpl(PARAM_BCC, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_BCC)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TO_MANY), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEXT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        //paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_IGNORE_SEND_FAILURE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_IGNORE_SEND_FAILURE)));
    }
}
