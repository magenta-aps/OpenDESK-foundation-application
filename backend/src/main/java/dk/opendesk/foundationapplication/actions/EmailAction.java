package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.enums.Functional;

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
import java.util.ArrayList;
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

        Application application;
        try {
            application = applicationBean.getApplication(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_SEND_EMAIL_FAIL, e);
        }

        Map<String, Serializable> model = (HashMap) ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL);
        if (model == null) {
            model = new HashMap<>();
        }
        for(ApplicationBlock block : application.getBlocks()){
            for(ApplicationFieldValue field : block.getFields()){
                if(field.isSingleValue()){
                    model.put(block.getLabel()+"_"+field.getLabel(), field.getSingleValue().toString());//todo Parse instead of tostring
                    model.put("id"+field.getId(), field.getSingleValue().toString());
                }else{
                    ArrayList<Object> values = field.getValue();
                    for(int i = 0 ; i<values.size() ; i++){
                        model.put(block.getLabel()+"_"+field.getLabel()+"_"+i, values.get(i).toString());//todo Parse instead of tostring
                    model.put("id"+field.getId()+"_"+i, values.get(i).toString());
                    }
                }
                
            }
        }
        model.put("subject", ruleAction.getParameterValue(PARAM_SUBJECT));


        ruleAction.setParameterValue(PARAM_TEMPLATE_MODEL, (Serializable) model);
        ruleAction.setParameterValue(PARAM_TO, application.getFunctionalField(Functional.email_to()).getSingleValue());

        //todo Hvad med alle de andre parametre?

        super.executeImpl(ruleAction,actionedUponNodeRef);


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
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_CC, DataTypeDefinition.TEXT, String.class, false, getParamDisplayLabel(PARAM_CC)));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_BCC, DataTypeDefinition.TEXT, String.class, false, getParamDisplayLabel(PARAM_BCC)));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_TO_MANY, DataTypeDefinition.ANY, null, false, getParamDisplayLabel(PARAM_TO_MANY), true));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_SUBJECT, DataTypeDefinition.TEXT, String.class, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_TEXT, DataTypeDefinition.TEXT, String.class, false, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_FROM, DataTypeDefinition.TEXT, String.class, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, NodeRef.class, true, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, HashMap.class, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));
        paramList.add(new FoundationActionParameterDefinition<>(PARAM_IGNORE_SEND_FAILURE, DataTypeDefinition.BOOLEAN, Boolean.class, false, getParamDisplayLabel(PARAM_IGNORE_SEND_FAILURE)));
    }
}
