/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationChange;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.DAO.JSONAction;
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.APPLICATION_CHANGE;
import static dk.opendesk.foundationapplication.Utilities.APPLICATION_EMAILFOLDER;
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static dk.opendesk.foundationapplication.Utilities.getODFName;
import dk.opendesk.repo.model.OpenDeskModel;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class ActionBean extends FoundationBean{
    private ApplicationBean applicationBean;

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    
    
    public List<ParameterDefinition> getActionParameters(String actionBeanName) {
        ActionDefinition actionDefinition = getServiceRegistry().getActionService().getActionDefinition(actionBeanName);
        return actionDefinition.getParameterDefinitions();
    }

    public ActionDefinition getAction(String actionName) {
        return getServiceRegistry().getActionService().getActionDefinition(actionName);
    }

    public void saveAction(String actionName, NodeRef stateRef, QName aspect, List<FoundationActionParameterValue> params) {
        HashMap<String, Serializable> paramMap = new HashMap<>();
        for (FoundationActionParameterValue param : params) {
            paramMap.put(param.getName(), param.getValue());
        }
        saveAction(actionName, stateRef, aspect, paramMap);
    }

    public void saveAction(String actionName, NodeRef stateRef, QName aspect, Map<String, Serializable> params) {
        Action action = getServiceRegistry().getActionService().createAction(actionName, params);
        getServiceRegistry().getActionService().saveAction(stateRef, action);
        getServiceRegistry().getNodeService().addAspect(action.getNodeRef(), aspect, null);
    }

    public List<JSONAction> getActions(NodeRef stateRef) {
        List<Action> actions = getServiceRegistry().getActionService().getActions(stateRef);
        List<JSONAction> jsonActions = new ArrayList<>();
        for (Action action : actions) {
            jsonActions.add(new JSONAction(action));
        }
        return jsonActions;
    }

    /**
     * Gets the NodeRef for a template from a template name
     *
     * @param templateName filename of a template located in the template folder
     * @return template NodeRef
     */
    public NodeRef getEmailTemplate(String templateName) throws Exception {
        Action action = getServiceRegistry().getActionService().createAction("foundationMail");

        String query = "PATH:\"" + OpenDeskModel.TEMPLATE_OD_FOLDER + "cm:" + templateName + "\"";
        ResultSet resultSet = getServiceRegistry().getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
                SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.getNodeRefs().size() == 0) {
            throw new Exception("Template not found");
        }
        if (resultSet.getNodeRefs().size() > 1) {
            throw new Exception("Multiple templates found");
        }
        return resultSet.getNodeRef(0);
    }
    public ApplicationChange getVersionDifference(Version oldVersion, Version newVersion) throws Exception {
        if (newVersion == null) {
            throw new Exception("newVersion must not be null");
        }

        String changeType = (String) newVersion.getVersionProperty(APPLICATION_CHANGE);

        Date timeStamp = newVersion.getFrozenModifiedDate();
        String modifier = newVersion.getFrozenModifier();
        NodeRef modifierId = getServiceRegistry().getPersonService().getPerson(modifier);

        Application newApp = applicationBean.getApplication(newVersion.getFrozenStateNodeRef());
        Application oldApp = (oldVersion == null) ? null : applicationBean.getApplication(oldVersion.getFrozenStateNodeRef());

        return new ApplicationChange().setChangeType(changeType).setTimeStamp(timeStamp).setModifier(modifier).setModifierId(modifierId).setChangeList(applicationBean.getApplicationDifference(oldApp, newApp));
    }
    
        /**
     * Saves a email message on the given application
     *
     * @param mimeMessage The message to be saved
     * @param applicationRef NodeRef to the application that the email shall be
     * saved on
     */
    public void saveEmailCopy(MimeMessage mimeMessage, NodeRef applicationRef) throws Exception {

        NodeRef emailFolderRef = getOrCreateEmailFolder(applicationRef);

        //setting filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");  //todo: Hvad vil vi have filen til at hedde?
        String fileName = sdf.format(mimeMessage.getSentDate()) + ".txt";

        //creating the file
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getCMName("name"), fileName);

        NodeRef fileRef = getServiceRegistry().getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("emailFolder"), TYPE_CONTENT, properties).getChildRef();

        //writing to file
        ContentWriter contentWriter = getServiceRegistry().getContentService().getWriter(fileRef, PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        try (OutputStream s = contentWriter.getContentOutputStream(); PrintWriter printWriter = new PrintWriter(s)) {
            Enumeration headers = mimeMessage.getAllHeaderLines();
            while (headers.hasMoreElements()) {
                printWriter.println(headers.nextElement());
            }
            printWriter.println();
            printWriter.println(mimeMessage.getContent());
        } catch (IOException | MessagingException e) {
            throw e;
        }

    }

    /**
     * Gets a given email from a given application
     *
     * @param applicationRef application NodeRef
     * @param emailRef Email NodeRef
     * @return content of email
     * @throws Exception if the email is not found
     */
    public String getEmail(NodeRef applicationRef, NodeRef emailRef) throws Exception {
        List<NodeRef> emailRefs = applicationBean.getApplicationEmails(applicationRef);
        for (NodeRef ref : emailRefs) {
            if (ref.equals(emailRef)) {
                ContentReader reader = getServiceRegistry().getFileFolderService().getReader(ref);
                return reader.getContentString();
            }
        }
        throw new Exception("The requested email was not found");
    }
    
        /**
     * Gets the email folder for an application or creates it if it does not
     * exists.
     *
     * @param applicationRef Application nodeRef
     * @return Email folder nodeRef
     * @throws Exception if there are more than one email folder on the
     * application.
     */
    public NodeRef getOrCreateEmailFolder(NodeRef applicationRef) throws Exception {
        NodeRef emailFolderRef;

        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(applicationRef, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);

        if (childAssociationRefs.size() == 0) {
            emailFolderRef = getServiceRegistry().getNodeService().createNode(applicationRef, getODFName(APPLICATION_EMAILFOLDER), getCMName(APPLICATION_EMAILFOLDER), TYPE_FOLDER).getChildRef();
        } else if (childAssociationRefs.size() == 1) {
            emailFolderRef = childAssociationRefs.get(0).getChildRef();
        } else {
            throw new Exception("More than one email folder created on application " + applicationRef);
        }

        return emailFolderRef;
    }
    
}
