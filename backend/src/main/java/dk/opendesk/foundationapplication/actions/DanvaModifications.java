package dk.opendesk.foundationapplication.actions;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.Utilities;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_EXCEL;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_PDF;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_WORD;


public class DanvaModifications extends ActionExecuterAbstractBase {


    public static final String EXCEPTION_DANVA_MODIFICATION_FAIL = "danva.modification.action.exception";
    public static final String EXCEPTION_MOVE_BLOCK = "danva.modification.move.block";

    private ApplicationBean applicationBean;
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }


    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        Application application;
        try {
            application = applicationBean.getApplication(actionedUponNodeRef);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_DANVA_MODIFICATION_FAIL, e);
        }

        List<ApplicationBlock> blocks = application.getBlocks();

        //finding the blocks
        ApplicationBlock periodBlock = applicationBean.getBlockByID("project_period", blocks);
        ApplicationBlock budgetBlock = applicationBean.getBlockByID("budget", blocks);
        ApplicationBlock accountabilityBlock = applicationBean.getBlockByID("accountability", blocks);
        ApplicationBlock fileBlock = applicationBean.getBlockByID("files", blocks);

        //moving blocks
        try {
            applicationBean.deleteBlock(application,periodBlock);
            applicationBean.deleteBlock(application,budgetBlock);
            applicationBean.deleteBlock(application,accountabilityBlock);
            applicationBean.addBlockToApplication(application,periodBlock);
            applicationBean.addBlockToApplication(application,budgetBlock);
            applicationBean.addBlockToApplication(application,accountabilityBlock);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_MOVE_BLOCK, e);
        }

        Application change = new Application();
        change.parseRef(actionedUponNodeRef);
        change.setBlocks(blocks);
        try {
            applicationBean.updateApplication(change);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_DANVA_MODIFICATION_FAIL, e);
        }

        //adding allowed mime types to the file fields
        for (ApplicationFieldValue field : fileBlock.getFields()) {
            if (!field.getId().startsWith("header")) {
                NodeRef fieldRef = field.asNodeRef();
                try {
                    ArrayList<String> allowedMimeTypes = new ArrayList<>(Arrays.asList(MIMETYPE_PDF, MIMETYPE_WORD, MIMETYPE_EXCEL, MIMETYPE_OPENXML_WORDPROCESSING, MIMETYPE_OPENDOCUMENT_SPREADSHEET ));
                    serviceRegistry.getNodeService().setProperty(fieldRef, Utilities.getODFName("allowedMimeTypes"),allowedMimeTypes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }
}
