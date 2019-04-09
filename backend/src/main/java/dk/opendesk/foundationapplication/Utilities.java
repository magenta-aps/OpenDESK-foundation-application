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
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationField;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationSchema;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterDefinition;
import dk.opendesk.foundationapplication.DAO.FoundationActionParameterValue;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.JSON.ApplicationPropertyDeserializer;
import dk.opendesk.foundationapplication.JSON.ApplicationPropertySerializer;
import dk.opendesk.foundationapplication.JSON.FoundationActionParameterDefinitionDeserializer;
import dk.opendesk.foundationapplication.JSON.FoundationActionParameterDefinitionSerializer;
import dk.opendesk.foundationapplication.JSON.FoundationActionParameterValueDeserializer;
import dk.opendesk.foundationapplication.JSON.FoundationActionParameterValueSerializer;
import dk.opendesk.foundationapplication.JSON.NodeRefDeserializer;
import dk.opendesk.foundationapplication.JSON.NodeRefSerializer;
import dk.opendesk.foundationapplication.beans.ActionBean;
import dk.opendesk.foundationapplication.beans.ApplicationBean;
import dk.opendesk.foundationapplication.beans.AuthorityBean;
import dk.opendesk.foundationapplication.beans.BranchBean;
import dk.opendesk.foundationapplication.beans.BudgetBean;
import dk.opendesk.foundationapplication.beans.WorkflowBean;
import dk.opendesk.foundationapplication.enums.Functional;
import dk.opendesk.foundationapplication.patches.InitialStructure;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 *
 * @author martin
 */
public final class Utilities {
    private Utilities(){}
    
    private static final Logger LOGGER = Logger.getLogger(Utilities.class);
    
    private final static MetricRegistry METRICS = new MetricRegistry();
    
    public static final String FOUNDATION_MODEL_LOCATION="/alfresco/module/foundationapplication/model/foundation-model.xml";

    public static final DateFormat UNIVERAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ENGLISH);
    
    public static final String DATA_TYPE_NAME = "data";
    public static final String DATA_ASSOC_WORKFLOW = "workflows";
    public static final String DATA_ASSOC_BRANCHES = "branches";
    public static final String DATA_ASSOC_BUDGETYEARS = "budgetYears";
    public static final String DATA_ASSOC_APPLICATIONS = "applications";
    public static final String DATA_ASSOC_NEW_APPLICATIONS = "newApplications";
    public static final String DATA_ASSOC_DELETED_APPLICATION = "deletedApplications";
    public static final String DATA_ASSOC_STATIC_FIELDS = "staticFields";
    public static final String DATA_ASSOC_SCHEMAS = "applicationSchemas";
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
    
    public static final String APPLICATIONSCHEMA_TYPE_NAME="applicationSchema";
    public static final String APPLICATIONSCHEMA_ASSOCS_BLOCKS="applicationSchemaBlocks";
    
    public static final String APPLICATIONSCHEMA_PARAM_ID="applicationSchemaID";
    public static final String APPLICATIONSCHEMA_PARAM_TITLE="applicationSchemaTitle";
    
    public static final String APPLICATION_TYPE_NAME="application";
    public static final String APPLICATION_ASSOC_BUDGET = "applicationBudget";
    public static final String APPLICATION_ASSOC_BRANCH = "applicationBranch";
    public static final String APPLICATION_ASSOC_BLOCKS = "applicationBlocks";
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

    public static final String APPLICATION_FOLDER_EMAIL = "emailFolder";
    public static final String APPLICATION_FOLDER_DOCUMENT = "documentFolder";

    public static final String APPLICATION_FOLDER_DOCUMENT_TEMP = "tempDocumentFolder";

    
    public static final String BLOCK_PARAM_ID = "applicationBlockId";
    public static final String BLOCK_PARAM_COLLAPSIBLE = "applicationBlockCollapsible";
    public static final String BLOCK_PARAM_REPEATABLE = "applicationBlockRepeatable";
    public static final String BLOCK_PARAM_LABEL = "applicationBlockLabel";
    public static final String BLOCK_PARAM_ICON = "applicationBlockIcon";
    public static final String BLOCK_PARAM_LAYOUT = "applicationBlockLayout";
    
    public static final String FIELD_TYPE_NAME = "applicationField";
    public static final String FIELD_PARAM_OPTIONS = "applicationFieldOptions";
    public static final String FIELD_PARAM_VALUE = "applicationFieldValue";
    public static final String FIELD_ASSOC_STATICDATA = "applicationFieldStaticData";
    
    public static final String STATICFIELD_TYPE_NAME = "applicationStaticField";
    public static final String STATICFIELD_PARAM_ID = "applicationStaticFieldID";
    public static final String STATICFIELD_PARAM_LABEL = "applicationStaticFieldLabel";
    public static final String STATICFIELD_PARAM_HINT = "applicationStaticFieldHint";
    public static final String STATICFIELD_PARAM_COMPONENT = "applicationStaticFieldComponent";
    public static final String STATICFIELD_PARAM_LAYOUT = "applicationStaticFieldLayout";
    public static final String STATICFIELD_PARAM_WRAPPER = "applicationStaticFieldWrapper";
    public static final String STATICFIELD_PARAM_CONTROLLED_BY = "applicationStaticFieldControlledBy";
    public static final String STATICFIELD_PARAM_DESCRIBES = "applicationStaticFieldDescribes";
    public static final String STATICFIELD_PARAM_TYPE = "applicationStaticFieldType";
    public static final String STATICFIELD_PARAM_VALIDATION = "applicationStaticFieldValidation";
    
    
    
    public static final String BLOCKSPEC_TYPE_NAME = "applicationBlockSpecification";
    public static final String BLOCKSPEC_ASSOC_FIELDS = "applicationBlockSpecFields";
    
    public static final String BLOCKIMPL_TYPE_NAME = "applicationBlockImpl";
    public static final String BLOCKIMPL_ASSOC_FIELDS= "applicationBlockImplFields";


    public static final String ACTION_NAME_ADD_BLOCKS = "addBlocks";
    public static final String ACTION_NAME_ADD_FIELDS = "addFields";
    public static final String ACTION_NAME_EMAIL = "foundationMail";
    public static final String ACTION_NAME_CREATE_APPLICANT = "createApplicant";
    public static final String ACTION_PARAM_STATE = "stateRef";
    public static final String ACTION_PARAM_ASPECT = "aspect";

    public static final String ASPECT_ON_CREATE = "onCreate";
    public static final String ASPECT_BEFORE_DELETE = "beforeDelete";

    public static final String CONTENT_NAME_SPACE = "http://www.alfresco.org/model/content/1.0";

    public static final String HEALTH_CHECK_STRUCTURE = "structure";

    public static final String EXCEPTION_EMAIL_TEMPLATE_FOLDER = "utilities.getOdfEmailTemplateFolder.exception";

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
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        BranchBean branchBean = new BranchBean();
        branchBean.setServiceRegistry(serviceRegistry);
        BudgetBean budgetBean = new BudgetBean();
        budgetBean.setServiceRegistry(serviceRegistry);
        WorkflowBean workflowBean = new WorkflowBean();
        workflowBean.setServiceRegistry(serviceRegistry);
        
        actionBean.setApplicationBean(applicationBean);
        
        applicationBean.setActionBean(actionBean);
        applicationBean.setAuthBean(authBean);
        applicationBean.setBranchBean(branchBean);
        applicationBean.setBudgetBean(budgetBean);
        applicationBean.setWorkflowBean(workflowBean);
        
        branchBean.setApplicationBean(applicationBean);
        branchBean.setAuthBean(authBean);
        branchBean.setBudgetBean(budgetBean);
        branchBean.setWorkflowBean(workflowBean);
        
        budgetBean.setApplicationBean(applicationBean);
        budgetBean.setAuthBean(authBean);
        budgetBean.setWorkflowBean(workflowBean);
        
        workflowBean.setApplicationBean(applicationBean);
        workflowBean.setAuthBean(authBean);
                
        NodeRef dataRef = applicationBean.getDataHome();
        
        AuthorityService as = serviceRegistry.getAuthorityService();
        for(String group : AuthorityBean.getAllCreatedGroups(as)){
            if(as.authorityExists(group)){
                LOGGER.debug("Deleting group: "+group);
                as.deleteAuthority(group, true);
            }else{
                LOGGER.debug("Group missing: "+group);
            }
        }
        

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
        for (ApplicationField applicationField : applicationBean.getApplicationFieldSpecs()) {
            nodeService.removeChild(dataRef, applicationField.asNodeRef());
        }
    }

    public static NodeRef getOdfEmailTemplateFolder(ServiceRegistry sr) {
        List<ChildAssociationRef> childAssociationRefs = sr.getNodeService().getChildAssocs(getEmailTemplateDir(sr), ContentModel.ASSOC_CONTAINS,  Utilities.getCMName(InitialStructure.MAIL_TEMPLATE_FOLDER_NAME));
        if (childAssociationRefs.size() != 1) {
            throw new AlfrescoRuntimeException(EXCEPTION_EMAIL_TEMPLATE_FOLDER);
        }
        return childAssociationRefs.get(0).getChildRef();
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
        Integer nextID = ++currentID;
        sr.getNodeService().addProperties(dataNode, Collections.singletonMap(getODFName(DATA_PARAM_LASTID), nextID));
        return nextID;
    }
    
    public static ObjectMapper getMapper(){
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ApplicationFieldValue.class, new ApplicationPropertySerializer());
        module.addDeserializer(ApplicationFieldValue.class, new ApplicationPropertyDeserializer());
        module.addSerializer(FoundationActionParameterDefinition.class, new FoundationActionParameterDefinitionSerializer());
        module.addDeserializer(FoundationActionParameterDefinition.class, new FoundationActionParameterDefinitionDeserializer());
        module.addSerializer(FoundationActionParameterValue.class, new FoundationActionParameterValueSerializer());
        module.addDeserializer(FoundationActionParameterValue.class, new FoundationActionParameterValueDeserializer());
        module.addSerializer(NodeRef.class, new NodeRefSerializer());
        module.addDeserializer(NodeRef.class, new NodeRefDeserializer());
        mapper.registerModule(module);
        mapper.setDateFormat(UNIVERAL_DATE_FORMAT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static ApplicationChangeBuilder buildChange(Application toChange) {
        return new ApplicationChangeBuilder(toChange);
    }
    
        public static StaticFieldChangeBuilder buildFieldChange(ApplicationBean bean) throws Exception {
        return new StaticFieldChangeBuilder(bean);
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

        public BlockChangeBuilder changeBlock(String blockId) {
            return new BlockChangeBuilder(blockId);
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


        public class BlockChangeBuilder {

            private final ApplicationBlock block;

            public BlockChangeBuilder(String blockId) {
                ApplicationBlock block = findBlock(change, blockId);
                if (block == null) {
                    block = findBlock(original, blockId);
                    ApplicationBlock newBlock = new ApplicationBlock();
                    newBlock.setId(block.getId());
                    List<ApplicationBlock> blocks = change.getBlocks();
                    if (blocks == null) {
                        blocks = new ArrayList<>();
                        change.setBlocks(blocks);
                    }
                    blocks.add(newBlock);
                    this.block = newBlock;
                }
                else {
                    this.block = block;
                }

            }

            public BlockChangeBuilder setLabel(String newLabel) {
                block.setLabel(newLabel);
                return this;
            }

            public BlockChangeBuilder setLayout(String newLayout) {
                block.setLayout(newLayout);
                return this;
            }

            public BlockChangeBuilder setFields(List<ApplicationFieldValue> newFields) {
                block.setFields(newFields);
                return this;
            }

            public BlockChangeBuilder setIcon(String newIcon) {
                block.setIcon(newIcon);
                return this;
            }

            public BlockChangeBuilder setCollapsible(Boolean newCollapsible) {
                block.setCollapsible(newCollapsible);
                return this;
            }

            public BlockChangeBuilder setRepeatable(Boolean newRepeatable) {
                block.setRepeatable(newRepeatable);
                return this;
            }

            public ApplicationChangeBuilder done() {
                return ApplicationChangeBuilder.this;
            }

            protected final ApplicationBlock findBlock(Application target, String id) {
                if (target.getBlocks() == null) {
                    return null;
                }
                for (ApplicationBlock block : target.getBlocks()) {
                    if (id.equals(block.getId())) {
                        return block;
                    }
                }
                return null;
            }
        }

        public class FieldChangeBuilder {

            private final ApplicationFieldValue value;

            public FieldChangeBuilder(String fieldID) {
                Pair<ApplicationBlock, ApplicationFieldValue> existing = findField(change, fieldID);
                if (existing != null) {
                    value = existing.getSecond();
                } else {
                    Pair<ApplicationBlock, ApplicationFieldValue> originalVal = findField(original, fieldID);
                    ApplicationFieldValue changeVal = new ApplicationFieldValue();
                    changeVal.setId(originalVal.getSecond().getId());

                    boolean found = false;
                    if (change.getBlocks() != null) {
                        for (ApplicationBlock block : change.getBlocks()) {
                            if (originalVal.getFirst().getId().equals(block.getId())) {
                                if(block.getFields() == null) {
                                    block.setFields(new ArrayList<>());
                                }
                                block.getFields().add(changeVal);
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        ApplicationBlock changeBlock = new ApplicationBlock();
                        changeBlock.setId(originalVal.getFirst().getId());
                        changeBlock.setFields(new ArrayList<>());
                        changeBlock.getFields().add(changeVal);
                        change.setBlocks(Arrays.asList(new ApplicationBlock[]{changeBlock}));
                    }

                    value = changeVal;

                }
            }

            public FieldChangeBuilder setValue(ArrayList newValue) throws ClassNotFoundException {
                if (newValue != null && !newValue.isEmpty()) {
                    value.setValue(newValue);

                    Class type = newValue.get(0).getClass();
                    for (Object listValue : newValue) {
                        if (!type.isAssignableFrom(listValue.getClass())) {
                            throw new RuntimeException("Contents of the list must contain the same class of elements " + newValue);
                        }
                    }
                }

                return this;
            }
            
            public FieldChangeBuilder setValue(Object newValue) throws ClassNotFoundException {
                value.setValue(new ListBuilder<>(new ArrayList<>()).add(newValue).build());
                value.setType(newValue.getClass().getCanonicalName());
                return this;
            }
            
            public FieldChangeBuilder setOptions(ArrayList newOptions){
                value.setOptions(newOptions);
                return this;
            }


            public ApplicationChangeBuilder done() {
                return ApplicationChangeBuilder.this;
            }

            protected final Pair<ApplicationBlock, ApplicationFieldValue> findField(Application target, String id) {
                if (target.getBlocks() == null) {
                    return null;
                }
                for (ApplicationBlock block : target.getBlocks()) {
                    if(block.getFields() == null) {
                        return null;
                    }
                    for (ApplicationFieldValue field : block.getFields()) {
                        if (id.equals(field.getId())) {
                            return new Pair<>(block, field);
                        }
                    }
                }
                return null;
            }

        }

    }
    
    public static class StaticFieldChangeBuilder {
        private final List<ApplicationField> currentFields = new ArrayList<>(); 
        private final List<ApplicationField> change = new ArrayList<>();

        public StaticFieldChangeBuilder(ApplicationBean bean) throws Exception {
            currentFields.addAll(bean.getApplicationFieldSpecs());
        }

        public FieldChangeBuilder changeField(String fieldId) {
            return new FieldChangeBuilder(fieldId);
        }
        

        public List<ApplicationField> build() {
            return change;
        }

        public class FieldChangeBuilder {

            private final ApplicationField value;

            public FieldChangeBuilder(String fieldID) {
                ApplicationField existing = findField(fieldID);
                value = existing;
                change.add(value);
            }

            public FieldChangeBuilder setLabel(String newLabel) {
                value.setLabel(newLabel);
                return this;
            }

            public FieldChangeBuilder setLayout(String newLayout) {
                value.setLayout(newLayout);
                return this;
            }

            public FieldChangeBuilder setComponent(String newComponent) {
                value.setComponent(newComponent);
                return this;
            }

            public FieldChangeBuilder setType(Class newType) throws ClassNotFoundException {
                value.setType(newType.getCanonicalName());
                return this;
            }

            public FieldChangeBuilder setDescribes(Functional newFunctional) {
                value.setDescribes(newFunctional.getFriendlyName());
                return this;
            }


            public FieldChangeBuilder setHint(String newHint) {
                value.setHint(newHint);
                return this;
            }

            public FieldChangeBuilder setWrapper(String newWrapper) {
                value.setWrapper(newWrapper);
                return this;
            }

            public FieldChangeBuilder setValidation(String newValidation) {
                value.setValidation(newValidation);
                return this;
            }

            public FieldChangeBuilder setReadOnly(Boolean newReadOnly) {
                value.setReadOnly(newReadOnly);
                return this;
            }

            public StaticFieldChangeBuilder done() {
                return StaticFieldChangeBuilder.this;
            }

            protected final ApplicationField findField(String id) {
                
                for (ApplicationField field : currentFields) {
                        if (id.equals(field.getId())) {
                            return field;
                        }
                }
                return null;
            }

        }

    }

}
