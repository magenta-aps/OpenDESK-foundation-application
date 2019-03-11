/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertiesContainer;
import dk.opendesk.foundationapplication.DAO.ApplicationPropertyValue;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.JSON.ApplicationPropertyDeserializer;
import dk.opendesk.foundationapplication.JSON.ApplicationPropertySerializer;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.beans.BranchBean;
import dk.opendesk.foundationapplication.beans.BudgetBean;
import dk.opendesk.foundationapplication.beans.WorkflowBean;
import dk.opendesk.foundationapplication.patches.InitialStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.w3c.dom.Document;

/**
 *
 * @author martin
 */
public final class Utilities {
    private Utilities(){};
    
    private final static MetricRegistry METRICS = new MetricRegistry();
    
    public static final String FOUNDATION_MODEL_LOCATION="/alfresco/module/foundationapplication/model/foundation-model.xml";
    
    public static final String DATA_TYPE_NAME = "data";
    public static final String DATA_ASSOC_WORKFLOW = "workflows";
    public static final String DATA_ASSOC_BRANCHES = "branches";
    public static final String DATA_ASSOC_BUDGETYEARS = "budgetYears";
    public static final String DATA_ASSOC_APPLICATIONS = "applications";
    public static final String DATA_ASSOC_NEW_APPLICATIONS = "newApplications";
    public static final String DATA_ASSOC_DELETED_APPLICATION = "deletedApplications";
    public static final String DATA_PARAM_LASTID = "latestID";

    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    
    public static final String BRANCH_TYPE_NAME = "applicationBranch"; 
    public static final String BRANCH_ASSOC_WORKFLOW = "branchWorkflow";
    public static final String BRANCH_ASSOC_BUDGETS = "branchBudgets";
    public static final String BRANCH_PARAM_TITLE = "branchTitle";
    
    public static final String WORKFLOW_TYPE_NAME = "applicationWorkflow";
    public static final String WORKFLOW_ASSOC_STATES = "workflowStates";
    public static final String WORKFLOW_ASSOC_ENTRY = "workflowEntry";
    public static final String WORKFLOW_PARAM_TITLE = "workflowTitle";
    
    public static final String STATE_TYPE_NAME = "applicationState";
    public static final String STATE_ASSOC_TRANSITIONS = "workflowTransitions";
    public static final String STATE_ASSOC_ACTIONS = "stateActions";
    public static final String STATE_PARAM_TITLE = "stateTitle";
    public static final String STATE_PARAM_CATEGORY = "stateCategory";
    
    public static final String BUDGET_TYPE_NAME = "applicationBudget";
    public static final String BUDGET_PARAM_TITLE="budgetTitle";
    public static final String BUDGET_PARAM_AMOUNT="budgetAmountAvailable";
    
    public static final String BUDGETYEAR_TYPE_NAME = "applicationBudgetYear";
    public static final String BUDGETYEAR_ASSOC_BUDGETS = "budgets";
    public static final String BUDGETYEAR_PARAM_TITLE="odf:budgetYearTitle";
    public static final String BUDGETYEAR_PARAM_STARTDATE="odf:budgetYearStartDate";
    public static final String BUDGETYEAR_PARAM_ENDDATE="odf:odf:budgetYearEndDate";
    
    public static final String APPLICATION_TYPE_NAME="application";
    public static final String APPLICATION_ASSOC_BUDGET = "applicationBudget";
    public static final String APPLICATION_ASSOC_BRANCH = "applicationBranch";
    public static final String APPLICATION_ASSOC_STATE = "applicationState";   
    public static final String APPLICATION_ASSOC_DOCUMENTS = "documents";
    
    public static final String APPLICATION_PARAM_ID= "applicationID";
    public static final String APPLICATION_PARAM_TITLE = "applicationTitle";
    public static final String APPLICATION_PARAM_SEEN_BY = "applicationSeenBy";

    public static final String APPLICATION_CHANGE = "applicationChange";
    public static final String APPLICATION_CHANGE_CREATED = "applicationCreation";
    public static final String APPLICATION_CHANGE_UPDATE = "applicationUpdate";
    public static final String APPLICATION_CHANGE_DELETED = "applicationDeletion";
    public static final String APPLICATION_CHANGE_UPDATE_EMAIL = "email";
    public static final String APPLICATION_CHANGE_UPDATE_ASSOCIATION = "associationChange";
//    public static final String APPLICATION_CHANGE_UPDATE_STATE = "stateChange";
    public static final String APPLICATION_CHANGE_UPDATE_PROP = "propertyChange";
//    public static final String APPLICATION_CHANGE_UPDATE_BUDGET = "budgetChange";
//    public static final String APPLICATION_CHANGE_UPDATE_BRANCH = "branchChange";

    public static final String APPLICATION_EMAILFOLDER = "emailFolder";

    public static final String ACTION_NAME_ADD_BLOCKS = "addBlocks";
    public static final String ACTION_NAME_ADD_FIELDS = "addFields";
    public static final String ACTION_NAME_EMAIL = "foundationMail";

    public static final String ASPECT_ON_CREATE = "onCreate";
    public static final String ASPECT_BEFORE_DELETE = "beforeDelete";

    public static final String CONTENT_NAME_SPACE = "http://www.alfresco.org/model/content/1.0";

    public static final String APPLICATION_PARAM_BLOCKS = "applicationBlocks";

    private static String foundationNameSpace = null;

    static{
        final JmxReporter reporter = JmxReporter.forRegistry(METRICS).build();
        reporter.start();
    }
    
    public static MetricRegistry getMetrics(){
        return METRICS;
    }
    
    public static String getFoundationModelNameSpace() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(InitialStructure.class.getResourceAsStream(FOUNDATION_MODEL_LOCATION));

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();


        XPathExpression expr = xpath.compile("string(/model/namespaces/namespace[@prefix=\"odf\"]/@uri)");
        String namespace = (String)expr.evaluate(doc, XPathConstants.STRING);
        return namespace;
    }
    
    public static NodeRef getDataNode(ServiceRegistry sr){
        return getDataNode(sr.getNodeService(), sr.getSearchService(), sr.getNamespaceService());
    }
     
    
    public static NodeRef getDataNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService){
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        List<NodeRef> refs = searchService.selectNodes(rootRef, InitialStructure.DATA_PATH, null, namespaceService, false);
        if (refs.size() > 1) {
            throw new AlfrescoRuntimeException("Failed to create structure: Returned multiple refs for " + InitialStructure.DATA_PATH);
        }
        if (refs.size() == 0) {
            throw new  AlfrescoRuntimeException("Failed to create structure: No refs returned for " + InitialStructure.DATA_PATH);
        }
        
        return refs.get(0);
    }
    
    public static void wipeData(ServiceRegistry serviceRegistry) throws Exception {
        NodeService nodeService = serviceRegistry.getNodeService();

        ActionBean actionBean = new ActionBean();
        actionBean.setServiceRegistry(serviceRegistry);
        ApplicationBean applicationBean = new ApplicationBean();
        applicationBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);
        
        actionBean.setApplicationBean(applicationBean);
        
        applicationBean.setActionBean(actionBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);
        
        branchBean.setApplicationBean(applicationBean);
        branchBean.setBudgetBean(budgetBean);
        
        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setWorkflowBean(workflowBean);
        
        workflowBean.setApplicationBean(applicationBean);
        
        
        NodeRef dataRef = applicationBean.getDataHome();

        for (NodeRef workflow : workflowBean.getWorkflows()) {
            nodeService.removeChild(dataRef, workflow);
        }

        for (NodeRef budget : budgetBean.getBudgetYearRefs()) {
            nodeService.removeChild(dataRef, budget);
        }

        for (NodeRef branch : branchBean.getBranches()) {
            nodeService.removeChild(dataRef, branch);
        }

        for (ApplicationSummary application : applicationBean.getApplicationSummaries()) {
            nodeService.removeChild(dataRef, application.asNodeRef());
        }
        for (ApplicationSummary application : applicationBean.getDeletedApplicationSummaries()) {
            nodeService.removeChild(dataRef, application.asNodeRef());
        }
    }
    
    public static NodeRef getEmailTemplateDir(ServiceRegistry sr){
        return getEmailTemplateDir(sr.getNodeService(), sr.getSearchService(), sr.getNamespaceService());
    }
    
    public static NodeRef getEmailTemplateDir(NodeService nodeService, SearchService searchService, NamespaceService namespaceService){
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        List<NodeRef> refs = searchService.selectNodes(rootRef, InitialStructure.MAIL_TEMPLATE_PATH, null, namespaceService, false);
        if (refs.size() > 1) {
            throw new AlfrescoRuntimeException("Failed to create structure: Returned multiple refs for " + InitialStructure.DATA_PATH);
        }
        if (refs.size() == 0) {
            throw new  AlfrescoRuntimeException("Failed to create structure: No refs returned for " + InitialStructure.MAIL_TEMPLATE_PATH);
        }
        
        return refs.get(0);
    }
    
    public static synchronized QName getODFName(String name) throws Exception{
        if(foundationNameSpace == null){
            foundationNameSpace = getFoundationModelNameSpace();
        }
        return QName.createQName(foundationNameSpace, name);
    }

    public static QName getCMName(String name) {
        return QName.createQName(CONTENT_NAME_SPACE, name);
    }
    
    public static boolean stringExists(String s){
        return s != null && !s.isEmpty();
    }
    
    public static synchronized Integer getCurrentApplicationID(ServiceRegistry sr) throws Exception{
        NodeRef dataNode = getDataNode(sr);
        Integer currentID = (Integer)sr.getNodeService().getProperty(dataNode, getODFName(DATA_PARAM_LASTID));
        return currentID;
    }
    
    
    
    public static synchronized Integer getNextApplicationID(ServiceRegistry sr) throws Exception{
        NodeRef dataNode = getDataNode(sr);
        Integer currentID = (Integer)sr.getNodeService().getProperty(dataNode, getODFName(DATA_PARAM_LASTID));
        Integer nextID = currentID++;
        sr.getNodeService().addProperties(dataNode, Collections.singletonMap(getODFName(DATA_PARAM_LASTID), nextID));
        return nextID;
    }
    
    public static ObjectMapper getMapper(){
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ApplicationPropertyValue.class, new ApplicationPropertySerializer());
        module.addDeserializer(ApplicationPropertyValue.class, new ApplicationPropertyDeserializer());
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }




    public static ApplicationChangeBuilder buildChange(Application toChange) {
        return new ApplicationChangeBuilder(toChange);
    }

    public static class ApplicationChangeBuilder {

        private final Application original;
        private final Application change = new Application();

        public ApplicationChangeBuilder(Application original) {
            this.original = original;
            change.setId(original.getId());
            change.parseRef(original.asNodeRef());
        }

        public FieldChangeBuilder changeField(String fieldId) {
            return new FieldChangeBuilder(fieldId);
        }
        
        public ApplicationChangeBuilder setBudget(NodeRef budget){
            BudgetReference budgetRef = new BudgetReference();
            budgetRef.parseRef(budget);
            return setBudget(budgetRef);
        }
        
        public ApplicationChangeBuilder setBudget(BudgetReference budget){
            change.setBudget(budget);
            return this;
        }
        
        public ApplicationChangeBuilder setState(NodeRef state){
            StateReference stateRef = new StateReference();
            stateRef.parseRef(state);
            return setState(stateRef);
        }
        
        public ApplicationChangeBuilder setState(StateReference state){
            change.setState(state);
            return this;
        }
        
        
        
        public ApplicationChangeBuilder setBranch(NodeRef branch){
            BranchSummary branchRef = new BranchSummary();
            branchRef.parseRef(branch);
            return setBranch(branchRef);
        }
        
        public ApplicationChangeBuilder setBranch(BranchSummary branch){
            change.setBranchSummary(branch);
            return this;
        }
        
        public ApplicationChangeBuilder setTitle(String title){
            change.setTitle(title);
            return this;
        }
        

        public Application build() {
            return change;
        }

        public class FieldChangeBuilder {

            private final ApplicationPropertyValue value;

            public FieldChangeBuilder(String fieldID) {
                Pair<ApplicationPropertiesContainer, ApplicationPropertyValue> existing = findField(change, fieldID);
                if (existing != null) {
                    value = existing.getSecond();
                } else {
                    Pair<ApplicationPropertiesContainer, ApplicationPropertyValue> originalVal = findField(original, fieldID);
                    ApplicationPropertyValue changeVal = new ApplicationPropertyValue();
                    changeVal.setId(originalVal.getSecond().getId());

                    boolean found = false;
                    if (change.getBlocks() != null) {
                        for (ApplicationPropertiesContainer block : change.getBlocks()) {
                            if (originalVal.getFirst().getId().equals(block.getId())) {
                                block.getFields().add(changeVal);
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        ApplicationPropertiesContainer changeBlock = new ApplicationPropertiesContainer();
                        changeBlock.setId(originalVal.getFirst().getId());
                        changeBlock.setFields(new ArrayList<>());
                        changeBlock.getFields().add(changeVal);
                        change.setBlocks(Arrays.asList(new ApplicationPropertiesContainer[]{changeBlock}));
                    }

                    value = changeVal;

                }
            }

            public FieldChangeBuilder setLabel(String newLabel) {
                value.setLabel(newLabel);
                return this;
            }

            public FieldChangeBuilder setLayout(String newLayout) {
                value.setLayout(newLayout);
                return this;
            }

            public FieldChangeBuilder setType(String newType) {
                value.setType(newType);
                return this;
            }

            public FieldChangeBuilder setJavaType(Class newType) {
                value.setJavaType(newType);
                return this;
            }

            public FieldChangeBuilder setDescribes(String newDescribes) {
                value.setDescribes(newDescribes);
                return this;
            }

            public FieldChangeBuilder setDescribes(List newAllowedValues) {
                value.setAllowedValues(newAllowedValues);
                return this;
            }

            public FieldChangeBuilder setValue(Object newValue) {
                value.setValue(newValue);
                value.setJavaType(newValue.getClass());
                return this;
            }

            public ApplicationChangeBuilder done() {
                return ApplicationChangeBuilder.this;
            }

            protected final Pair<ApplicationPropertiesContainer, ApplicationPropertyValue> findField(Application target, String id) {
                if (target.getBlocks() == null) {
                    return null;
                }
                for (ApplicationPropertiesContainer block : target.getBlocks()) {
                    for (ApplicationPropertyValue field : block.getFields()) {
                        if (id.equals(field.getId())) {
                            return new Pair<>(block, field);
                        }
                    }
                }
                return null;
            }

        }

    }

}
