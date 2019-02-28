/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import dk.opendesk.foundationapplication.patches.InitialStructure;
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
import org.w3c.dom.Document;

/**
 *
 * @author martin
 */
public final class Utilities {
    private Utilities(){};
    
    public static final String FOUNDATION_MODEL_LOCATION="/alfresco/module/foundationapplication/model/foundation-model.xml";
    
    public static final String DATA_TYPE_NAME = "data";
    public static final String DATA_ASSOC_WORKFLOW = "workflows";
    public static final String DATA_ASSOC_BRANCHES = "branches";
    public static final String DATA_ASSOC_BUDGETYEARS = "budgetYears";
    public static final String DATA_ASSOC_APPLICATIONS = "applications";
    public static final String DATA_ASSOC_NEW_APPLICATIONS = "newApplications";
    public static final String DATA_ASSOC_DELETED_APPLICATION = "deletedApplications";
    
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
    public static final String APPLICATION_ASSOC_PROJECT_DESCRIPTION_DOC = "projectDescriptionDoc";
    public static final String APPLICATION_ASSOC_BUDGET_DOC = "budgetDoc";
    public static final String APPLICATION_ASSOC_BOARD_MEMBERS_DOC = "boardMembersDoc";
    public static final String APPLICATION_ASSOC_ARTICLES_OF_ASSOCIATION_DOC = "articlesOfAssociationDoc";
    public static final String APPLICATION_ASSOC_FINANCIAL_ACCOUTING_DOC = "financialAccountingDoc";
    
    public static final String APPLICATION_PARAM_TITLE = "applicationTitle";
    public static final String APPLICATION_PARAM_CATEGORY = "applicationCategory";
    public static final String APPLICATION_PARAM_RECIPIENT = "applicationRecipient";
    public static final String APPLICATION_PARAM_ADDR_ROAD = "applicationAddressRoad";
    public static final String APPLICATION_PARAM_ADDR_NUMBER = "applicationAddressNumber";
    public static final String APPLICATION_PARAM_ADDR_FLOOR = "applicationAddressFloor";
    public static final String APPLICATION_PARAM_ARRD_POSTALCODE = "applicationAddressPostalCode";
    public static final String APPLICATION_PARAM_PERSON_FIRSTNAME = "applicationContactPersonFirstName";
    public static final String APPLICATION_PARAM_PERSON_SURNAME = "applicationContactPersonSurname";
    public static final String APPLICATION_PARAM_PERSON_EMAIL = "applicationContactEmail";
    public static final String APPLICATION_PARAM_PERSON_PHONE = "applicationContactPhoneNumber";
    public static final String APPLICATION_PARAM_SHORT_DESCRIPTION = "applicationShortDescription";
    public static final String APPLICATION_PARAM_START_DATE = "applicationProjectStartDate";
    public static final String APPLICATION_PARAM_END_DATE = "applicationProjectEndDate";
    public static final String APPLICATION_PARAM_APPLIED_AMOUNT = "applicationAppliedAmount";
    public static final String APPLICATION_PARAM_ACCOUNT_REGISTRATION = "applicationAccountRegistration";
    public static final String APPLICATION_PARAM_ACCOUNT_NUMBER = "applicationAccountNumber";
    public static final String APPLICATION_PARAM_CVR = "cvr";
    public static final String APPLICATION_PARAM_SEEN_BY = "applicationSeenBy";

    public static final String APPLICATION_CHANGE = "applicationChange";
    public static final String APPLICATION_CHANGE_CREATED = "applicationCreation";
    public static final String APPLICATION_CHANGE_UPDATE = "applicationUpdate";
    public static final String APPLICATION_CHANGE_DELETED = "applicationDeletion";
    public static final String APPLICATION_CHANGE_UPDATE_EMAIL = "email";
    public static final String APPLICATION_CHANGE_UPDATE_STATE = "stateChange";
    public static final String APPLICATION_CHANGE_UPDATE_PROP = "propertyChange";
    public static final String APPLICATION_CHANGE_UPDATE_BUDGET = "budgetChange";
    public static final String APPLICATION_CHANGE_UPDATE_BRANCH = "branchChange";

    public static final String APPLICATION_EMAILFOLDER = "emailFolder";

    public static final String ACTION_NAME_EMAIL = "email";
    public static final String ACTION_BEAN_NAME_EMAIL = "foundationMail";

    public static final String ASPECT_ON_CREATE = "onCreate";
    public static final String ASPECT_BEFORE_DELETE = "beforeDelete";

    public static final String CONTENT_NAME_SPACE = "http://www.alfresco.org/model/content/1.0";
    
    private static String foundationNameSpace = null;
    
    
    
    
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
    
    
    
    
    
}
