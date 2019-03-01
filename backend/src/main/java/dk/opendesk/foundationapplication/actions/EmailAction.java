package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.repo.model.OpenDeskModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.*;

public class EmailAction extends MailActionExecuter {

    private static final String RECIPIENT = "recipient";
    private static final String EMAIL_TYPE = "emailTemplateType";
    private static ThreadLocal<Pair<MimeMessage,NodeRef>> threadLocal = new ThreadLocal<>();

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }


    /**
     * Send an email message
     *
     * @throws org.alfresco.error.AlfrescoRuntimeException
     */
    @Override
    protected void executeImpl(final Action ruleAction, final NodeRef actionedUponNodeRef) {

        try {
            Application application = foundationBean.getApplication(actionedUponNodeRef);

            Map<String, Serializable> model = new HashMap<>();
            model.put("firstName", application.getContactFirstName());
            model.put("lastName", application.getContactLastName());
            model.put("subject", ruleAction.getParameterValue(PARAM_SUBJECT)); //todo temp subject for temp template
            model.put("body", "Der var en mand der hed " + application.getContactFirstName() ); //todo temp body for temp template
            ruleAction.setParameterValue(PARAM_TEMPLATE_MODEL, (Serializable) model);

            ruleAction.setParameterValue(PARAM_TO, application.getContactEmail());

            //TODO skal det tjekkes at template, subject og from er blevet sat inden eksekvering
            super.executeImpl(ruleAction,actionedUponNodeRef);

        } catch (Exception e) {
            e.printStackTrace();
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
            foundationBean.saveEmailCopy((MimeMessage) message.getFirst(), (NodeRef) message.getSecond());
        } catch (Exception e) {
            e.printStackTrace();
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
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        //paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_IGNORE_SEND_FAILURE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_IGNORE_SEND_FAILURE)));
    }
}
