/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import static org.alfresco.model.ContentModel.*;
import static org.alfresco.repo.action.executer.MailActionExecuter.*;

import dk.opendesk.foundationapplication.enums.StateCategory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import dk.opendesk.repo.model.OpenDeskModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.map.SingletonMap;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class FoundationBean {

    private static final Logger LOGGER = Logger.getLogger(FoundationBean.class);

    public final String ONLY_ONE_REFERENCE = "odf.one.ref.requred";
    public final String INVALID_STATE = "odf.bad.state";
    public final String MUST_SPECIFY_STATE = "odf.specify.state";
    public final String INVALID_BRANCH = "odf.bad.branch";
    public final String ID_IN_USE = "odf.id.used";
    public final String ID_BAD_NODE_TYPE = "odf.node.badtype";

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public NodeRef getDataHome() {
        return Utilities.getDataNode(serviceRegistry);
    }

    public ApplicationReference addNewApplication(String id, NodeRef branchRef, NodeRef budgetRef, String title, ApplicationPropertiesContainer... blocks) throws Exception {
        Application app = new Application();
        app.setId(id);
        app.setTitle(title);
        BranchReference branch = new BranchReference();
        branch.parseRef(branchRef);

        BranchSummary branchSummary = new BranchSummary();
        branchSummary.setNodeRef(branchRef.toString());

        app.setBranchSummary(branchSummary);
        BudgetReference budget = new BudgetReference();
        budget.parseRef(budgetRef);
        app.setBudget(budget);
        app.setBlocks(Arrays.asList(blocks));

        return addNewApplication(app);
    }

    public ApplicationReference addNewApplication(Application application) throws Exception {
        ObjectMapper blockMapper = Utilities.getMapper();
        Map<QName, Serializable> properties = new HashMap<>();
        if (application.getId() != null) {
            ApplicationReference ref = findByNumericID(Integer.parseInt(application.getId()));
            if (ref != null) {
                throw new AlfrescoRuntimeException(ID_IN_USE);
            }
        } else {
            application.setId(Utilities.getNextApplicationID(serviceRegistry) + "");
        }
        properties.put(getODFName(APPLICATION_PARAM_ID), application.getId());
        properties.put(getODFName(APPLICATION_PARAM_TITLE), application.getTitle());

        ArrayList<String> blockStrings = new ArrayList<>();
        List<ApplicationPropertiesContainer> blocks = application.getBlocks();
        if (blocks != null) {
            for (ApplicationPropertiesContainer block : blocks) {
                String blockString = blockMapper.writeValueAsString(block);
                blockStrings.add(blockString);
            }
        }

        properties.put(getODFName(APPLICATION_PARAM_BLOCKS), blockStrings);

        QName applicationTypeQname = getODFName(APPLICATION_TYPE_NAME);
        QName applicationQname = getODFName(application.getTitle());
        QName dataAssocApplication = getODFName(DATA_ASSOC_APPLICATIONS);

        NodeRef applicationRef = serviceRegistry.getNodeService().createNode(getDataHome(), dataAssocApplication, applicationQname, applicationTypeQname, properties).getChildRef();
        if (application.getBudget() != null) {
            serviceRegistry.getNodeService().createAssociation(applicationRef, application.getBudget().asNodeRef(), getODFName(APPLICATION_ASSOC_BUDGET));
        }
        if (application.getBranchSummary() != null) {
            NodeRef workFlowRef = serviceRegistry.getNodeService().getTargetAssocs(application.getBranchSummary().asNodeRef(), getODFName(BRANCH_ASSOC_WORKFLOW)).get(0).getTargetRef();
            List<AssociationRef> workflowEntryRefs = serviceRegistry.getNodeService().getTargetAssocs(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY));
            if (workflowEntryRefs.size() != 1) {
                throw new AlfrescoRuntimeException("Cannot create new application. The workflow on this branch does not have an entry point set");
            }
            serviceRegistry.getNodeService().createAssociation(applicationRef, application.getBranchSummary().asNodeRef(), getODFName(APPLICATION_ASSOC_BRANCH));
            serviceRegistry.getNodeService().createAssociation(applicationRef, workflowEntryRefs.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_STATE));
        } else {
            serviceRegistry.getNodeService().createAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
        }

        serviceRegistry.getVersionService().createVersion(applicationRef, Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_CREATED));

        return getApplicationReference(applicationRef);
    }

    public void updateApplication(Application app) throws Exception {
        String currentUser = getCurrentUserName();
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        if (app.wasTitleSet()) {
            properties.put(getODFName(APPLICATION_PARAM_TITLE), app.getTitle());
        }

        if (app.wasIsSeenSet()) {
            //if isSeen shall be set to true but is currently false
            if (app.getIsSeen() && !isApplicationSeen(app.asNodeRef(), currentUser)) {
                properties.put(getODFName(APPLICATION_PARAM_SEEN_BY), currentUser);
            } //if isSeen shall be set to false but is currently true
            else if (!app.getIsSeen() && isApplicationSeen(app.asNodeRef(), currentUser)) {
                ArrayList<String> seenBy = (ArrayList<String>) ns.getProperty(app.asNodeRef(), getODFName(APPLICATION_PARAM_SEEN_BY));
                seenBy.remove(currentUser);
                properties.put(getODFName(APPLICATION_PARAM_SEEN_BY), seenBy);
            }
        }

        boolean changedWorkflow = false;
        if (app.wasBranchSummarySet()) {
            NodeRef newBranchRef = app.getBranchSummary().asNodeRef();
            NodeRef currentBranchRef = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BRANCH);

            if (!newBranchRef.equals(currentBranchRef)) {
                NodeRef newBranchWorkflow = getSingleTargetAssoc(newBranchRef, BRANCH_ASSOC_WORKFLOW);
                NodeRef currentBranchWorkflow = getSingleTargetAssoc(currentBranchRef, BRANCH_ASSOC_WORKFLOW);

                changedWorkflow = !newBranchWorkflow.equals(currentBranchWorkflow);

                if (changedWorkflow && app.getState() == null) {
                    throw new AlfrescoRuntimeException(MUST_SPECIFY_STATE);
                }

                ns.setAssociations(app.asNodeRef(), getODFName(APPLICATION_ASSOC_BRANCH), Collections.singletonList(newBranchRef));
            }

        }

        if (app.wasStateReferenceSet()) {
            if (app.getState() == null) {
                clearApplicationState(app.asNodeRef());
            } else {
                if (changedWorkflow) {
                    setStateDifferentWorkflow(app);
                } else {
                    setStateSameWorkflow(app);
                }
            }
        }

        if (app.wasBudgetSet()) {
            NodeRef currentBudget = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BUDGET);
            NodeRef currentBranch = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BRANCH);
            List<AssociationRef> branchBudgets = ns.getTargetAssocs(currentBranch, getODFName(BRANCH_ASSOC_BUDGETS));
            if (app.getBudget() == null) {

                ns.removeAssociation(app.asNodeRef(), currentBudget, getODFName(APPLICATION_ASSOC_BUDGET));
            } else {
                NodeRef newBudget = app.getBudget().asNodeRef();
                if (!currentBudget.equals(newBudget)) {
                    boolean found = false;
                    for (AssociationRef branchBudget : branchBudgets) {
                        if (newBudget.equals(branchBudget.getTargetRef())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new AlfrescoRuntimeException(INVALID_BRANCH);
                    }
                    ns.setAssociations(app.asNodeRef(), getODFName(APPLICATION_ASSOC_BUDGET), Collections.singletonList(newBudget));
                }

            }
        }

        List<ApplicationPropertiesContainer> oldBlocks = getApplication(app.asNodeRef()).getBlocks();

        if (app.wasBlocksSet() && app.getBlocks() != null) {
            for (ApplicationPropertiesContainer block : app.getBlocks()) {
                if (block.getId() != null) {
                    ApplicationPropertiesContainer oldBlock = getBlockByID(block.getId(), oldBlocks);
                    if (oldBlock == null) {
                        oldBlocks.add(block);
                    } else {
                        if (block.wasLabelSet()) {
                            oldBlock.setLabel(block.getLabel());
                        }
                        if (block.wasFieldsSet()) {
                            if (block.getFields() == null) {
                                oldBlock.setFields(null);
                            } else {
                                for (ApplicationPropertyValue field : block.getFields()) {
                                    if (field.getId() != null) {
                                        ApplicationPropertyValue oldField = getFieldByID(field.getId(), oldBlock.getFields());
                                        if (oldField == null) {
                                            oldBlock.getFields().add(field);
                                        } else {
                                            if (field.wasLabelSet()) {
                                                oldField.setLabel(field.getLabel());
                                            }
                                            if (field.wasJavaTypeSet()) {
                                                oldField.setJavaType(field.getJavaType());
                                            }
                                            if (field.wasLayoutSet()) {
                                                oldField.setLayout(field.getLayout());
                                            }
                                            if (field.wasTypeSet()) {
                                                oldField.setType(field.getType());
                                            }
                                            if (field.wasDescribesSet()) {
                                                oldField.setDescribes(field.getDescribes());
                                            }
                                            if (field.wasAllowedValuesSet()) {
                                                oldField.setAllowedValues(field.getAllowedValues());
                                            }
                                            if (field.wasValueSet()) {
                                                oldField.setValue(field.getValue());
                                            }
                                        }
                                    } else {
                                        LOGGER.warn("Found field without ID: " + field + " in block: " + block);
                                    }

                                }
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Found block without ID: " + block);
                }

            }
            ObjectMapper mapper = Utilities.getMapper();
            ArrayList<String> blockStrings = new ArrayList<>();
            for (ApplicationPropertiesContainer block : oldBlocks) {
                blockStrings.add(mapper.writeValueAsString(block));
            }
            properties.put(getODFName(APPLICATION_PARAM_BLOCKS), blockStrings);
        }

//        if(app.wasProjectDescriptionDocSet()){
//            updateContent(app.getProjectDescriptionDoc(), app, APPLICATION_ASSOC_PROJECT_DESCRIPTION_DOC);
//        }
//        if(app.wasBudgetDocSet()){
//            updateContent(app.getBudgetDoc(), app, APPLICATION_ASSOC_BUDGET_DOC);
//        }
//        if(app.wasBoardMembersDocSet()){
//            updateContent(app.getBoardMembersDoc(), app, APPLICATION_ASSOC_BOARD_MEMBERS_DOC);
//        }
//        if(app.wasArticlesOfAssociationDocSet()){
//            updateContent(app.getArticlesOfAssociationDoc(), app, APPLICATION_ASSOC_ARTICLES_OF_ASSOCIATION_DOC);
//        }
//        if(app.wasFinancialAccountingDocSet()){
//            updateContent(app.getFinancialAccountingDoc(), app, APPLICATION_ASSOC_FINANCIAL_ACCOUTING_DOC);
//        }
        ns.addProperties(app.asNodeRef(), properties);
    }

    private String getCurrentUserName() {
        return serviceRegistry.getAuthenticationService().getCurrentUserName();
    }

    public boolean isApplicationSeen(NodeRef appRef, String userName) throws Exception {
        List<String> seenByList = (List<String>) serviceRegistry.getNodeService().getProperty(appRef, getODFName(APPLICATION_PARAM_SEEN_BY));
        //Set<String> seenBySet = new HashSet<>(seenByList);
        if (seenByList == null) {
            return false;
        }
        return seenByList.contains(userName);
    }

    private ApplicationPropertiesContainer getBlockByID(String id, List<ApplicationPropertiesContainer> blocks) {
        for (ApplicationPropertiesContainer block : blocks) {
            if (Objects.equals(id, block.getId())) {
                return block;
            }
        }
        return null;
    }

    private ApplicationPropertyValue getFieldByID(String id, List<ApplicationPropertyValue> fields) {
        for (ApplicationPropertyValue field : fields) {
            if (Objects.equals(id, field.getId())) {
                return field;
            }
        }
        return null;
    }

    private void updateContent(Reference toUpdate, Reference parent, String assoc) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        if (toUpdate == null) {
            NodeRef currentProject = getSingleTargetAssoc(parent.asNodeRef(), assoc);
            ns.removeAssociation(parent.asNodeRef(), currentProject, getODFName(assoc));
        } else {
            ns.setAssociations(parent.asNodeRef(), getODFName(assoc), Collections.singletonList(toUpdate.asNodeRef()));
        }
    }

    private void setStateDifferentWorkflow(Application app) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        NodeRef newState = app.getState().asNodeRef();
        NodeRef newBranchRef = app.getBranchSummary().asNodeRef();
        NodeRef newBranchWorkflow = getSingleTargetAssoc(newBranchRef, BRANCH_ASSOC_WORKFLOW);
        List<AssociationRef> newWorkflowStates = ns.getTargetAssocs(newBranchWorkflow, getODFName(WORKFLOW_ASSOC_STATES));
        boolean found = false;
        for (AssociationRef workflowState : newWorkflowStates) {
            if (newState.equals(workflowState.getTargetRef())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new AlfrescoRuntimeException(INVALID_STATE);
        }
        setApplicationState(app.asNodeRef(), newState);
    }

    private void setStateSameWorkflow(Application app) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        NodeRef currentState = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_STATE);
        StateReference newState = app.getState();
        //The state didn't change. Don't continue.
        if (currentState.equals(newState.asNodeRef())) {
            return;
        }

        NodeRef newStateRef = newState.asNodeRef();

        List<AssociationRef> stateTransitions = ns.getTargetAssocs(currentState, getODFName(STATE_ASSOC_TRANSITIONS));
        boolean found = false;
        for (AssociationRef transition : stateTransitions) {
            if (transition.getTargetRef().equals(newStateRef)) {
                found = true;
                break;
            }
        }
        if (found) {
            setApplicationState(app.asNodeRef(), newStateRef);
        } else {
            throw new AlfrescoRuntimeException(INVALID_STATE);
        }

    }

    private void setApplicationState(NodeRef applicationRef, NodeRef stateRef) throws Exception {
        serviceRegistry.getNodeService().removeAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
        serviceRegistry.getNodeService().setAssociations(applicationRef, getODFName(APPLICATION_ASSOC_STATE), Collections.singletonList(stateRef));
    }

    private void clearApplicationState(NodeRef applicationRef) throws Exception {
        serviceRegistry.getNodeService().removeAssociation(getDataHome(), applicationRef, getODFName(APPLICATION_ASSOC_STATE));
        serviceRegistry.getNodeService().setAssociations(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS), Collections.singletonList(applicationRef));
    }

    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception {
        return serviceRegistry.getNodeService().getTargetAssocs(applicationRef, getODFName(APPLICATION_ASSOC_BUDGET)).get(0).getTargetRef();
    }

    public NodeRef getApplicationState(NodeRef applicationRef) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationRef);

        QName applicationStateName = getODFName(APPLICATION_ASSOC_STATE);
        List<AssociationRef> states = serviceRegistry.getNodeService().getTargetAssocs(applicationRef, applicationStateName);
        //The association is singular, it is never a list
        return states != null && !states.isEmpty() ? states.get(0).getTargetRef() : null;
    }

    public List<BudgetYear> getCurrentBudgetYears() throws Exception {
        Instant now = Instant.now();
        List<BudgetYearSummary> budgetYears = getBudgetYearSummaries();
        List<BudgetYear> currentBudgets = new ArrayList<>();
        for (BudgetYearSummary budgetYear : budgetYears) {
            Instant budgetStartDate = budgetYear.getStartDate().toInstant();
            Instant budgetEndDate = budgetYear.getEndDate().toInstant();
            if (now.isAfter(budgetStartDate) && now.isBefore(budgetEndDate)) {
                currentBudgets.add(getBudgetYear(budgetYear.asNodeRef()));
            }
        }
        return currentBudgets;
    }

    public NodeRef addNewBudgetYear(String localName, String title, Date startDate, Date endDate) throws Exception {
        QName budgetYearsQname = getODFName(DATA_ASSOC_BUDGETYEARS);
        QName budgetYearTypeQname = getODFName(BUDGETYEAR_TYPE_NAME);
        QName budgetYearQname = getODFName(localName);

        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(getODFName(BUDGETYEAR_PARAM_TITLE), title);
        budgetParams.put(getODFName(BUDGETYEAR_PARAM_STARTDATE), startDate);
        budgetParams.put(getODFName(BUDGETYEAR_PARAM_ENDDATE), endDate);

        return serviceRegistry.getNodeService().createNode(getDataHome(), budgetYearsQname, budgetYearQname, budgetYearTypeQname, budgetParams).getChildRef();

    }

//    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception{
//        QName applicationBudgetName = getODFName(APPLICATION_ASSOC_BUDGET);
//        List<AssociationRef> budgets = serviceRegistry.getNodeService().getTargetAssocs(applicationRef, applicationBudgetName);
//        //The association is singular, it is never a list
//        return budgets != null && !budgets.isEmpty() ? budgets.get(0).getTargetRef() : null;
//    }
    public NodeRef addNewBudget(NodeRef budgetYear, String localName, String title, Long amount) throws Exception {
        QName budgetYearBudgetsQname = getODFName(BUDGETYEAR_ASSOC_BUDGETS);
        QName budgetTypeQname = getODFName(BUDGET_TYPE_NAME);
        QName budgetQname = getODFName(localName);

        Map<QName, Serializable> budgetParams = new HashMap<>();
        budgetParams.put(getODFName(BUDGET_PARAM_TITLE), title);
        budgetParams.put(getODFName(BUDGET_PARAM_AMOUNT), amount);

        return serviceRegistry.getNodeService().createNode(budgetYear, budgetYearBudgetsQname, budgetQname, budgetTypeQname, budgetParams).getChildRef();

    }

//    public Long getBudgetAllocatedFunding(NodeRef budgetRef) throws Exception {
//        List<AssociationRef> refs = serviceRegistry.getNodeService().getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
//        long totalAllocatedFunding = 0;
//        for (AssociationRef ref : refs) {
//            Long amount = (Long) serviceRegistry.getNodeService().getProperty(ref.getSourceRef(), getODFName(APPLICATION_PARAM_APPLIED_AMOUNT));
//            totalAllocatedFunding = totalAllocatedFunding + amount;
//        }
//        return totalAllocatedFunding;
//    }
//
//    public Long getBudgetTotalFunding(NodeRef budgetRef) throws Exception {
//        Long totalAmount = (Long) serviceRegistry.getNodeService().getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT));
//        return totalAmount;
//    }
//
//    public Long getBudgetRemainingFunding(NodeRef budgetRef) throws Exception {
//        Long totalAmount = getBudgetTotalFunding(budgetRef);
//        Long usedAmount = getBudgetAllocatedFunding(budgetRef);
//        return totalAmount - usedAmount;
//    }
    public BudgetReference getBudgetReference(NodeRef budgetRef) throws Exception {
        ensureType(getODFName(BUDGET_TYPE_NAME), budgetRef);

        BudgetReference ref = new BudgetReference();
        ref.parseRef(budgetRef);
        ref.setTitle(getProperty(budgetRef, BUDGET_PARAM_TITLE, String.class));
        return ref;
    }

    public NodeRef addNewBranch(String localName, String title) throws Exception {
        NodeRef dataHome = getDataHome();
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        QName branchTypeQname = getODFName(BRANCH_TYPE_NAME);
        QName branchQname = getODFName(localName);
        QName branchTitle = getODFName(BRANCH_PARAM_TITLE);
        Map<QName, Serializable> branchParams = new HashMap<>();
        branchParams.put(branchTitle, title);

        return serviceRegistry.getNodeService().createNode(dataHome, dataBranchesQname, branchQname, branchTypeQname, branchParams).getChildRef();
    }

    public WorkflowReference getWorkflowReference(NodeRef reference) throws Exception {
        ensureType(getODFName(WORKFLOW_TYPE_NAME), reference);

        WorkflowReference ref = new WorkflowReference();
        ref.parseRef(reference);
        ref.setTitle(getProperty(reference, WORKFLOW_PARAM_TITLE, String.class));
        return ref;
    }

    public NodeRef addNewWorkflow(String localName, String title) throws Exception {
        NodeRef dataHome = getDataHome();
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        QName workFlowTypeQname = getODFName(WORKFLOW_TYPE_NAME);
        QName workFlowQname = getODFName(localName);

        QName workflowTitle = getODFName(WORKFLOW_PARAM_TITLE);
        Map<QName, Serializable> workflowParams = new HashMap<>();
        workflowParams.put(workflowTitle, title);

        return serviceRegistry.getNodeService().createNode(dataHome, dataWorkflowsQname, workFlowQname, workFlowTypeQname, workflowParams).getChildRef();
    }

    public void setWorkflowEntryPoint(NodeRef workFlowRef, NodeRef workflowStateRef) throws Exception {
        serviceRegistry.getNodeService().setAssociations(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY), Collections.singletonList(workflowStateRef));
    }

    public NodeRef addNewWorkflowState(NodeRef workFlowRef, String localName, String title, StateCategory category) throws Exception {
        QName workFlowStatesQname = getODFName(WORKFLOW_ASSOC_STATES);
        QName stateTypeQname = getODFName(STATE_TYPE_NAME);
        QName stateQName = getODFName(localName);
        QName stateTitle = getODFName(STATE_PARAM_TITLE);
        QName stateCategory = getODFName(STATE_PARAM_CATEGORY);
        Map<QName, Serializable> stateParams = new HashMap<>();
        stateParams.put(stateTitle, title);
        if (category != null) {
            stateParams.put(stateCategory, category.getCategoryName());
        }
        return serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateQName, stateTypeQname, stateParams).getChildRef();

        /*
        NodeRef newWorkflowState = serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateQName, stateTypeQname, stateParams).getChildRef();

        ChildAssociationRef createStateActionsNode = serviceRegistry.getNodeService().createNode(newWorkflowState, getODFName(STATE_ASSOC_ACTIONS), QName.createQName(CONTENT_NAME_SPACE,"createStateActionsNode"), QName.createQName(CONTENT_NAME_SPACE,"cmobject"));
        ChildAssociationRef deleteStateActionsNode = serviceRegistry.getNodeService().createNode(newWorkflowState, getODFName(STATE_ASSOC_ACTIONS), QName.createQName(CONTENT_NAME_SPACE,"deleteStateActionsNode"), QName.createQName(CONTENT_NAME_SPACE,"cmobject"));

        return newWorkflowState;
         */
    }

    public AssociationRef createWorkflowTransition(NodeRef stateFrom, NodeRef stateTo) throws Exception {
        QName stateTransitionsQname = getODFName(STATE_ASSOC_TRANSITIONS);
        return serviceRegistry.getNodeService().createAssociation(stateFrom, stateTo, stateTransitionsQname);
    }

    public AssociationRef addBranchWorkflow(NodeRef branchRef, NodeRef workflowRef) throws Exception {
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        return serviceRegistry.getNodeService().createAssociation(branchRef, workflowRef, branchWorkflowQname);
    }

    public AssociationRef addBranchBudget(NodeRef branchRef, NodeRef budgetRef) throws Exception {
        QName branchBudgetsQname = getODFName(BRANCH_ASSOC_BUDGETS);
        return serviceRegistry.getNodeService().createAssociation(branchRef, budgetRef, branchBudgetsQname);
    }

    public NodeRef getBranchWorkflow(NodeRef branchRef) throws Exception {
        QName branchWorkflowQname = getODFName(BRANCH_ASSOC_WORKFLOW);
        List<AssociationRef> workflows = serviceRegistry.getNodeService().getTargetAssocs(branchRef, branchWorkflowQname);
        //The workflow association is singular, it is never a list
        return workflows != null && !workflows.isEmpty() ? workflows.get(0).getTargetRef() : null;
    }

    public List<NodeRef> getWorkflows() throws Exception {
        QName dataWorkflowsQname = getODFName(DATA_ASSOC_WORKFLOW);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> workflowAssocs = serviceRegistry.getNodeService().getChildAssocs(dataHome, dataWorkflowsQname, null);
        List<NodeRef> workflows = new ArrayList<>(workflowAssocs.size());
        for (ChildAssociationRef ref : workflowAssocs) {
            workflows.add(ref.getChildRef());
        }
        return workflows;
    }

    public List<NodeRef> getBudgetYearRefs() throws Exception {
        QName budgetYearsQName = getODFName(DATA_ASSOC_BUDGETYEARS);
        List<ChildAssociationRef> budgetAssocs = serviceRegistry.getNodeService().getChildAssocs(getDataHome(), budgetYearsQName, null);
        List<NodeRef> budgetYears = new ArrayList<>(budgetAssocs.size());
        for (ChildAssociationRef ref : budgetAssocs) {
            budgetYears.add(ref.getChildRef());
        }
        return budgetYears;
    }

    public List<NodeRef> getBudgetRefs(NodeRef budgetYear) throws Exception {
        QName budgetYearBudgets = getODFName(BUDGETYEAR_ASSOC_BUDGETS);
        List<ChildAssociationRef> budgetAssocs = serviceRegistry.getNodeService().getChildAssocs(budgetYear, budgetYearBudgets, null);
        List<NodeRef> budgets = new ArrayList<>(budgetAssocs.size());
        for (ChildAssociationRef ref : budgetAssocs) {
            budgets.add(ref.getChildRef());
        }
        return budgets;
    }

    public List<BudgetYearSummary> getBudgetYearSummaries() throws Exception {
        List<BudgetYearSummary> summaries = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for (ChildAssociationRef budgetYear : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_BUDGETYEARS), null)) {
            summaries.add(getBudgetYearSummary(budgetYear.getChildRef()));
        }
        return summaries;
    }

    public BudgetYearReference getBudgetYearReference(NodeRef budgetYearRef) throws Exception {
        ensureType(getODFName(BUDGETYEAR_TYPE_NAME), budgetYearRef);

        BudgetYearReference reference = new BudgetYearReference();
        reference.parseRef(budgetYearRef);
        reference.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        return reference;
    }

    public BudgetYearSummary getBudgetYearSummary(NodeRef budgetYearRef) throws Exception {
        ensureType(getODFName(BUDGETYEAR_TYPE_NAME), budgetYearRef);

        BudgetYearSummary summary = new BudgetYearSummary();
        summary.parseRef(budgetYearRef);
        summary.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        summary.setStartDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_STARTDATE, Date.class));
        summary.setEndDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_ENDDATE, Date.class));

        Long totalAmount = 0l;
        List<BudgetSummary> budgets = getBudgetSummaries(summary);
        for (BudgetSummary budget : budgets) {
            totalAmount += budget.getAmountTotal();
        }

        summary.setAmountTotal(totalAmount);
        return summary;
    }

    public BudgetYear getBudgetYear(NodeRef budgetYearRef) throws Exception {
        ensureType(getODFName(BUDGETYEAR_TYPE_NAME), budgetYearRef);

        NodeService ns = serviceRegistry.getNodeService();

        BudgetYear budgetYear = new BudgetYear();
        budgetYear.parseRef(budgetYearRef);
        budgetYear.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        budgetYear.setStartDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_STARTDATE, Date.class));
        budgetYear.setEndDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_ENDDATE, Date.class));

        List<Budget> budgets = new ArrayList<>();

        for (ChildAssociationRef budgetRef : ns.getChildAssocs(budgetYearRef, getODFName(BUDGETYEAR_ASSOC_BUDGETS), null)) {
            budgets.add(getBudget(budgetRef.getChildRef()));
        }

        Long amountTotal = 0l;
        Long amountAccepted = 0l;
        Long amountNominated = 0l;
        Long amountAvailable = 0l;
        Long amountClosed = 0l;
        Long amountApplied = 0l;

        for (Budget budget : budgets) {
            amountTotal += budget.getAmountTotal();
            amountAccepted += budget.getAmountAccepted();
            amountNominated += budget.getAmountNominated();
            amountAvailable += budget.getAmountAvailable();
            amountClosed += budget.getAmountClosed();
            amountApplied += budget.getAmountApplied();
        }

        budgetYear.setAmountTotal(amountTotal);
        budgetYear.setAmountAccepted(amountAccepted);
        budgetYear.setAmountNominated(amountNominated);
        budgetYear.setAmountAvailable(amountAvailable);
        budgetYear.setAmountClosed(amountClosed);
        budgetYear.setAmountApplied(amountApplied);

        return budgetYear;
    }

    public Budget getBudget(NodeRef budgetRef) throws Exception {
        ensureType(getODFName(BUDGET_TYPE_NAME), budgetRef);

        NodeService ns = serviceRegistry.getNodeService();
        Budget budget = new Budget();
        budget.parseRef(budgetRef);
        budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
        budget.setAmountTotal((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));

        Long amountAccepted = 0l;
        Long amountNominated = 0l;
        Long amountClosed = 0l;
        Long ammountApplied = 0l;
        List<ApplicationReference> applications = new ArrayList<>();
        List<AssociationRef> applicationRefs = ns.getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        for (AssociationRef applicationRef : applicationRefs) {
            ApplicationSummary application = getApplicationSummary(applicationRef.getSourceRef());
            ApplicationReference appRef = new ApplicationReference();
            appRef.setNodeRef(application.getNodeRef());
            appRef.setTitle(application.getTitle());
            applications.add(appRef);
            ApplicationPropertyValue<Long> value = application.totalAmount();
            if (value == null) {
                continue;
            }
            Long applicationAmount = value.getValue();
            NodeRef applicationStateRef = getApplicationState(applicationRef.getSourceRef());
            if (applicationStateRef == null) {
                continue;
            }
            State state = getState(applicationStateRef);
            StateCategory category = state.getCategory();
            if (category == null) {
                ammountApplied += applicationAmount;
            } else {
                switch (category) {
                    case ACCEPTED:
                        amountAccepted += applicationAmount;
                        break;
                    case NOMINATED:
                        amountNominated += applicationAmount;
                        break;
                    case CLOSED:
                        amountClosed += applicationAmount;
                        break;
                }
            }

        }
        Long amountAvailable = budget.getAmountTotal() - amountAccepted - amountClosed;

        budget.setAmountAccepted(amountAccepted);
        budget.setAmountNominated(amountNominated);
        budget.setAmountClosed(amountClosed);
        budget.setAmountApplied(ammountApplied);
        budget.setAmountAvailable(amountAvailable);

        budget.setApplications(applications);

        return budget;
    }

    public List<BudgetSummary> getBudgetSummaries(BudgetYearReference budgetYear) throws Exception {

        List<BudgetSummary> summaries = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for (NodeRef budgetRef : getBudgetRefs(budgetYear.asNodeRef())) {
            BudgetSummary budget = new BudgetSummary();
            budget.parseRef(budgetRef);
            budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
            budget.setAmountTotal((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));
            summaries.add(budget);
        }
        return summaries;
    }

    public List<NodeRef> getBranches() throws Exception {
        QName dataBranchesQname = getODFName(DATA_ASSOC_BRANCHES);
        NodeRef dataHome = getDataHome();
        List<ChildAssociationRef> branchAssocs = serviceRegistry.getNodeService().getChildAssocs(dataHome, dataBranchesQname, null);
        List<NodeRef> branches = new ArrayList<>(branchAssocs.size());
        for (ChildAssociationRef ref : branchAssocs) {
            branches.add(ref.getChildRef());
        }
        return branches;
    }

    public List<BranchSummary> getBranchSummaries() throws Exception {
        List<NodeRef> refs = getBranches();
        List<BranchSummary> branches = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for (NodeRef branchRef : refs) {
            branches.add(getBranchSummary(branchRef));
        }
        return branches;
    }

    public BranchReference getBranchReference(NodeRef branchRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);

        BranchReference branchReference = new BranchReference();
        branchReference.parseRef(branchRef);
        branchReference.setTitle(getProperty(branchRef, BRANCH_PARAM_TITLE, String.class));
        return branchReference;
    }

    public BranchSummary getBranchSummary(NodeRef branchRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);

        NodeService ns = serviceRegistry.getNodeService();
        BranchSummary summary = new BranchSummary();
        summary.parseRef(branchRef);
        summary.setTitle((String) ns.getProperty(branchRef, getODFName(BRANCH_PARAM_TITLE)));
        NodeRef workflowRef = getBranchWorkflow(branchRef);
        if (workflowRef != null) {
            WorkflowReference workflow = new WorkflowReference();
            workflow.parseRef(workflowRef);
            workflow.setTitle((String) ns.getProperty(workflowRef, getODFName(WORKFLOW_PARAM_TITLE)));
            summary.setWorkflowRef(workflow);
        }
        return summary;
    }

    public Branch getBranch(NodeRef branchRef) throws Exception {
        ensureType(getODFName(BRANCH_TYPE_NAME), branchRef);

        NodeService ns = serviceRegistry.getNodeService();
        Branch branch = new Branch();
        branch.parseRef(branchRef);
        branch.setTitle(getProperty(branchRef, BRANCH_PARAM_TITLE, String.class));
        NodeRef workflowRef = getBranchWorkflow(branchRef);
        if (workflowRef != null) {
            WorkflowReference workflow = new WorkflowReference();
            workflow.parseRef(workflowRef);
            workflow.setTitle((String) ns.getProperty(workflowRef, getODFName(WORKFLOW_PARAM_TITLE)));
            branch.setWorkflowRef(workflow);
        }
        branch.setSummaries(getBranchApplications(branchRef));
        List<BudgetReference> budgets = new ArrayList<>();
        for (AssociationRef ref : ns.getTargetAssocs(branchRef, getODFName(BRANCH_ASSOC_BUDGETS))) {
            budgets.add(getBudgetReference(ref.getTargetRef()));
        }
        branch.setBudgets(budgets);
        return branch;
    }

    public List<ApplicationSummary> getBranchApplications(NodeRef branchRef) throws Exception {

        List<AssociationRef> applicationRefs = serviceRegistry.getNodeService().getSourceAssocs(branchRef, getODFName(APPLICATION_ASSOC_BRANCH));
        List<ApplicationSummary> applications = new ArrayList<>();
        for (AssociationRef ref : applicationRefs) {
            NodeRef appRef = ref.getSourceRef();
            applications.add(getApplicationSummary(appRef));
        }
        return applications;

    }

    public ApplicationReference getApplicationReference(NodeRef applicationRef) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationRef);

        ApplicationReference reference = new ApplicationReference();
        reference.parseRef(applicationRef);
        reference.setId(getProperty(applicationRef, APPLICATION_PARAM_ID, String.class));
        reference.setTitle(getProperty(applicationRef, APPLICATION_PARAM_TITLE, String.class));
        return reference;
    }

    public List<ApplicationSummary> getApplicationSummaries() throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (ChildAssociationRef applicationRef : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_APPLICATIONS), null)) {
            toReturn.add(getApplicationSummary(applicationRef.getChildRef()));
        }
        return toReturn;
    }

    public List<ApplicationSummary> getDeletedApplicationSummaries() throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (ChildAssociationRef applicationRef : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_DELETED_APPLICATION), null)) {
            toReturn.add(getApplicationSummary(applicationRef.getChildRef()));
        }
        return toReturn;
    }

    public List<ApplicationSummary> getNewApplicationSummaries() throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (AssociationRef applicationRef : ns.getTargetAssocs(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS))) {
            toReturn.add(getApplicationSummary(applicationRef.getTargetRef()));
        }
        return toReturn;
    }

    public ApplicationSummary getApplicationSummary(NodeRef applicationSummary) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationSummary);

        ObjectMapper mapper = Utilities.getMapper();
        ApplicationSummary app = new ApplicationSummary();
        app.parseRef(applicationSummary);

        NodeRef branchRef = getSingleTargetAssoc(applicationSummary, APPLICATION_ASSOC_BRANCH);
        if (branchRef != null) {
            BranchSummary branchSummary = getBranchSummary(branchRef);
            app.setBranchSummary(branchSummary);
        }
        app.setId(getProperty(applicationSummary, APPLICATION_PARAM_ID, String.class));
        app.setTitle(getProperty(applicationSummary, APPLICATION_PARAM_TITLE, String.class));
        app.setIsSeen(isApplicationSeen(applicationSummary, getCurrentUserName()));
        List<String> blockStrings = getProperty(applicationSummary, APPLICATION_PARAM_BLOCKS, List.class);
        List<ApplicationPropertiesContainer> blocks = new ArrayList<>();

        if (blockStrings != null) {
            for (String blockString : blockStrings) {
                blocks.add(mapper.readValue(blockString, ApplicationPropertiesContainer.class));
            }
        }

        app.setBlocks(blocks);

        return app;
    }

    public Application getApplication(NodeRef applicationRef) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationRef);

        ObjectMapper mapper = Utilities.getMapper();
        Application application = new Application();
        application.parseRef(applicationRef);
        application.setTitle(getProperty(applicationRef, APPLICATION_PARAM_TITLE, String.class));
        application.setIsSeen(isApplicationSeen(applicationRef, getCurrentUserName()));
        List<String> blockStrings = getProperty(applicationRef, APPLICATION_PARAM_BLOCKS, List.class);
        List<ApplicationPropertiesContainer> blocks = new ArrayList<>();

        for (String blockString : blockStrings) {
            blocks.add(mapper.readValue(blockString, ApplicationPropertiesContainer.class));
        }
        application.setBlocks(blocks);

        NodeRef branchRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BRANCH);
        NodeRef budgetRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BUDGET);
        NodeRef stateRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_STATE);

        if (branchRef != null) {
            application.setBranchSummary(getBranchSummary(branchRef));
        }
        if (budgetRef != null) {
            BudgetReference budget = new BudgetReference();
            budget.parseRef(budgetRef);
            application.setBudget(budget);
        }
        if (stateRef != null) {
            StateReference state = new StateReference();
            state.parseRef(stateRef);
            application.setState(state);
            NodeRef workflowRef = getSingleParentAssoc(stateRef, WORKFLOW_ASSOC_STATES);
            if (workflowRef != null) {
                WorkflowReference workflow = new WorkflowReference();
                workflow.parseRef(workflowRef);
                application.setWorkflow(workflow);
            }
        }

//        NodeRef projectDesc = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_PROJECT_DESCRIPTION_DOC);
//        NodeRef budgetDoc = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BUDGET_DOC);
//        NodeRef boardMembers = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BOARD_MEMBERS_DOC);
//        NodeRef articlesOfAssociation = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_ARTICLES_OF_ASSOCIATION_DOC);
//        NodeRef financialAccouting = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_FINANCIAL_ACCOUTING_DOC);
        return application;

    }

    public ApplicationReference findByNumericID(Integer id) throws Exception {
        ResultSet set = serviceRegistry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "LANGUAGE_LUCENE", "TYPE:\"odf:application\" AND @odf:applicationID:\"" + id + "\"");
        if (set.length() == 0) {
            return null;
        }
        ChildAssociationRef ref = set.getRow(0).getChildAssocRef();
        return getApplicationReference(ref.getChildRef());
    }

    public void updateBudget(Budget budget) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        if (budget.wasTitleSet()) {
            properties.put(getODFName(BUDGET_PARAM_TITLE), budget.getTitle());
        }
        if (budget.wasAmountTotalSet()) {
            properties.put(getODFName(BUDGET_PARAM_AMOUNT), budget.getAmountTotal());
        }
        ns.addProperties(budget.asNodeRef(), properties);
    }

    public void updateBranch(BranchSummary branch) throws Exception {//BranchSummary is intentional. We don't want to update applications on the branch in this method, because applications doesn't actually belong to branches.
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        if (branch.wasTitleSet()) {
            properties.put(getODFName(BRANCH_PARAM_TITLE), branch.getTitle());
        }
        ns.addProperties(branch.asNodeRef(), properties);
        if (branch.getWorkflowRef() != null) {
            ns.setAssociations(branch.asNodeRef(), getODFName(BRANCH_ASSOC_WORKFLOW), Collections.singletonList(branch.getWorkflowRef().asNodeRef()));
        }
    }

    public List<WorkflowSummary> getWorkflowSummaries() throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<WorkflowSummary> summaries = new ArrayList<>();
        for (ChildAssociationRef ref : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_WORKFLOW), null)) {
            summaries.add(getWorkflowSummary(ref.getChildRef()));
        }
        return summaries;
    }

    public WorkflowSummary getWorkflowSummary(NodeRef workflowRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        WorkflowSummary summary = new WorkflowSummary();
        summary.parseRef(workflowRef);
        summary.setTitle(getProperty(workflowRef, WORKFLOW_PARAM_TITLE, String.class));
        NodeRef stateRef = getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY);
        if (stateRef != null) {
            summary.setEntry(getStateReference(stateRef));
        }

        List<StateReference> stateReferences = new ArrayList<>();
        for (ChildAssociationRef state : ns.getChildAssocs(workflowRef, getODFName(WORKFLOW_ASSOC_STATES), null)) {
            stateReferences.add(getStateReference(state.getChildRef()));
        }
        summary.setStates(stateReferences);
        return summary;
    }

    public Workflow getWorkflow(NodeRef workflowRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        Workflow workflow = new Workflow();
        workflow.parseRef(workflowRef);
        workflow.setTitle(getProperty(workflowRef, WORKFLOW_PARAM_TITLE, String.class));

        NodeRef entryRef = getSingleTargetAssoc(workflowRef, WORKFLOW_ASSOC_ENTRY);
        workflow.setEntry(getStateReference(entryRef));

        List<StateSummary> states = new ArrayList<>();
        for (ChildAssociationRef stateRef : ns.getChildAssocs(workflowRef, getODFName(WORKFLOW_ASSOC_STATES), null)) {
            states.add(getStateSummary(stateRef.getChildRef()));
        }
        workflow.setStates(states);

        return workflow;

    }

    public State getState(NodeRef stateRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        State state = new State();
        state.parseRef(stateRef);
        state.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        List<StateReference> transitions = new ArrayList<>();
        for (AssociationRef transitionRef : ns.getTargetAssocs(stateRef, getODFName(STATE_ASSOC_TRANSITIONS))) {
            transitions.add(getStateReference(transitionRef.getTargetRef()));
        }
        state.setReferences(transitions);
        List<ApplicationReference> applications = new ArrayList<>();
        for (AssociationRef applicationRef : ns.getSourceAssocs(stateRef, getODFName(APPLICATION_ASSOC_STATE))) {
            applications.add(getApplicationReference(applicationRef.getSourceRef()));
        }
        state.setApplications(applications);
        state.setCategory(StateCategory.getFromName(getProperty(stateRef, STATE_PARAM_CATEGORY, String.class)));
        return state;
    }

    public StateSummary getStateSummary(NodeRef stateRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        StateSummary summary = new StateSummary();
        summary.parseRef(stateRef);
        summary.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        List<StateReference> transitions = new ArrayList<>();
        for (AssociationRef transitionRef : ns.getTargetAssocs(stateRef, getODFName(STATE_ASSOC_TRANSITIONS))) {
            transitions.add(getStateReference(transitionRef.getTargetRef()));
        }
        summary.setReferences(transitions);
        summary.setCategory(StateCategory.getFromName(getProperty(stateRef, STATE_PARAM_CATEGORY, String.class)));
        return summary;
    }

    public StateReference getStateReference(NodeRef stateRef) throws Exception {
        StateReference reference = new StateReference();
        reference.parseRef(stateRef);
        reference.setTitle(getProperty(stateRef, STATE_PARAM_TITLE, String.class));
        return reference;
    }

    public <T> T getProperty(NodeRef ref, String name, Class<T> Type) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        return (T) ns.getProperty(ref, getODFName(name));
    }

    public NodeRef getSingleTargetAssoc(NodeRef sourceRef, String assocName) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<AssociationRef> refs = ns.getTargetAssocs(sourceRef, getODFName(assocName));
        if (refs != null && refs.size() > 1) {
            throw new AlfrescoRuntimeException(ONLY_ONE_REFERENCE);
        }
        if (refs != null && !refs.isEmpty()) {
            return refs.get(0).getTargetRef();
        } else {
            return null;
        }
    }

    public NodeRef getSingleSourceAssoc(NodeRef targetRef, String assocName) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<AssociationRef> refs = ns.getSourceAssocs(targetRef, getODFName(assocName));
        if (refs != null && refs.size() > 1) {
            throw new AlfrescoRuntimeException(ONLY_ONE_REFERENCE);
        }
        if (refs != null && !refs.isEmpty()) {
            return refs.get(0).getSourceRef();
        } else {
            return null;
        }
    }

    public NodeRef getSingleParentAssoc(NodeRef childRef, String assocName) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<ChildAssociationRef> refs = ns.getParentAssocs(childRef, getODFName(assocName), new QNamePattern() {
            @Override
            public boolean isMatch(QName qname) {
                return true;
            }
        });
        if (refs != null && refs.size() > 1) {
            throw new AlfrescoRuntimeException(ONLY_ONE_REFERENCE);
        }
        if (refs != null && !refs.isEmpty()) {
            return refs.get(0).getParentRef();
        } else {
            return null;
        }
    }

    public void ensureType(QName expectedType, NodeRef ref) {
        QName actualType = serviceRegistry.getNodeService().getType(ref);
        if (!expectedType.equals(actualType)) {
            throw new AlfrescoRuntimeException(ID_BAD_NODE_TYPE, new Object[]{expectedType, actualType});
        }
    }

    public List<ParameterDefinition> getActionParameters(String actionBeanName) {
        ActionDefinition actionDefinition = serviceRegistry.getActionService().getActionDefinition(actionBeanName);
        return actionDefinition.getParameterDefinitions();
    }

    public ActionDefinition getAction(String actionName) {
        return serviceRegistry.getActionService().getActionDefinition(actionName);
    }

    public void saveAction(String actionName, NodeRef stateRef, QName aspect, Map<String, Serializable> params) {
        Action action = serviceRegistry.getActionService().createAction(actionName, params);
        serviceRegistry.getActionService().saveAction(stateRef, action);
        serviceRegistry.getNodeService().addAspect(action.getNodeRef(), aspect, null);
    }

    public List<JSONAction> getActions(NodeRef stateRef) {
        List<Action> actions = serviceRegistry.getActionService().getActions(stateRef);
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
        Action action = serviceRegistry.getActionService().createAction("foundationMail");

        String query = "PATH:\"" + OpenDeskModel.TEMPLATE_OD_FOLDER + "cm:" + templateName + "\"";
        ResultSet resultSet = serviceRegistry.getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
                SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.getNodeRefs().size() == 0) {
            throw new Exception("Template not found");
        }
        if (resultSet.getNodeRefs().size() > 1) {
            throw new Exception("Multiple templates found");
        }
        return resultSet.getNodeRef(0);
    }

    public List<ApplicationChange> getApplicationHistory(NodeRef appRef) throws Exception {
        List<ApplicationChange> changes = new ArrayList<>();

        VersionHistory history = serviceRegistry.getVersionService().getVersionHistory(appRef);
        Version current = history.getHeadVersion();

        while (current != null) {
            Version predecessor = history.getPredecessor(current);
            changes.add(getVersionDifference(predecessor, current));
            current = predecessor;
        }

        for (NodeRef ref : getApplicationEmails(appRef)) {
            ApplicationChangeUnit unit = new ApplicationChangeUnit()
                    .setNewValue(ref)
                    .setChangeType(APPLICATION_CHANGE_UPDATE_EMAIL)
                    .setNewValueLink("/foundation/application/" + appRef.getId() + "/email/" + ref.getId()); //TODO er dette det rigtige link?
            Date timeStamp = serviceRegistry.getFileFolderService().getFileInfo(ref).getCreatedDate();
            changes.add(new ApplicationChange().setTimeStamp(timeStamp).setChangeList(Collections.singletonList(unit)));
        }

        return changes;
    }

    public ApplicationChange getVersionDifference(Version oldVersion, Version newVersion) throws Exception {
        if (newVersion == null) {
            throw new Exception("newVersion must not be null");
        }

        String changeType = (String) newVersion.getVersionProperty(APPLICATION_CHANGE);

        Date timeStamp = newVersion.getFrozenModifiedDate();
        String modifier = newVersion.getFrozenModifier();
        NodeRef modifierId = serviceRegistry.getPersonService().getPerson(modifier);

        Application newApp = getApplication(newVersion.getFrozenStateNodeRef());
        Application oldApp = (oldVersion == null) ? null : getApplication(oldVersion.getFrozenStateNodeRef());

        return new ApplicationChange().setChangeType(changeType).setTimeStamp(timeStamp).setModifier(modifier).setModifierId(modifierId).setChangeList(getApplicationDifference(oldApp, newApp));
    }

    public List<ApplicationChangeUnit> getApplicationDifference(Application oldVersion, Application newVersion) {

        List<ApplicationChangeUnit> changes = new ArrayList<>();

        Map<String, ApplicationPropertyValue> oldVersionProperties = getApplicationFields(oldVersion);
        Map<String, ApplicationPropertyValue> newVersionProperties = getApplicationFields(newVersion);

        for (String key : oldVersionProperties.keySet()) {
            ApplicationPropertyValue oldValueField = oldVersionProperties.get(key);
            ApplicationPropertyValue newValueField = newVersionProperties.get(key);
            if (newValueField != null) {
                if (!Objects.equals(oldValueField.getValue(), newValueField.getValue())) {
                    changes.add(new ApplicationChangeUnit().setChangedField(oldValueField.getLabel()).setOldValue(oldValueField.getValue()).setNewValue(newValueField.getValue()).setChangeType(APPLICATION_CHANGE_UPDATE_PROP));
                }
            } else {
                changes.add(new ApplicationChangeUnit().setChangedField(oldValueField.getLabel()).setOldValue(oldValueField.getValue()).setChangeType(APPLICATION_CHANGE_UPDATE_PROP));
            }
            newVersionProperties.remove(key);
        }

        for (String key : newVersionProperties.keySet()) {
            ApplicationPropertyValue newValueField = newVersionProperties.get(key);
            changes.add(new ApplicationChangeUnit().setChangedField(newValueField.getLabel()).setNewValue(newValueField.getValue()).setChangeType(APPLICATION_CHANGE_UPDATE_PROP));
        }

        resolveChangedAssociation(oldVersion.getBranchSummary(), newVersion.getBranchSummary(), "Branch", changes);
        resolveChangedAssociation(oldVersion.getBudget(), newVersion.getBudget(), "Budget", changes);
        resolveChangedAssociation(oldVersion.getState(), newVersion.getState(), "State", changes);
        resolveChangedAssociation(oldVersion.getWorkflow(), newVersion.getWorkflow(), "Workflow", changes);

        return changes;
    }

    public Map<String, ApplicationPropertyValue> getApplicationFields(Application application) {
        Map<String, ApplicationPropertyValue> toReturn = new HashMap<>();
        for (ApplicationPropertiesContainer block : application.getBlocks()) {
            for (ApplicationPropertyValue field : block.getFields()) {
                toReturn.put(field.getId(), field);
            }
        }
        return toReturn;
    }

    public <E extends Reference> void resolveChangedAssociation(E oldType, E newType, String associationName, List<ApplicationChangeUnit> changes) {
        if (!Objects.equals(oldType, newType)) {
            changes.add(new ApplicationChangeUnit().setChangedField(associationName).setOldValue(oldType.getTitle()).setNewValue(newType.getTitle()).setChangeType(APPLICATION_CHANGE_UPDATE_ASSOCIATION));
        }
    }

    public NodeRef getOrCreateFolder(NodeRef applicationRef, String folderName) {
        NodeRef folder = null;
        try {
            folder = serviceRegistry.getNodeService().getChildByName(applicationRef, getODFName(folderName), "cm:" + folderName);
            if (folder == null) {
                ChildAssociationRef childRef = serviceRegistry.getNodeService().createNode(applicationRef, getODFName(folderName), getCMName(folderName), getCMName("Folder"));
                folder = childRef.getChildRef();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return folder;
    }

    /**
     * Saves a email message on the given application
     *
     * @param mimeMessage The message to be saved
     * @param applicationRef NodeRef to the application that the email shall be
     * saved on
     */
    public void saveEmailCopy(MimeMessage mimeMessage, NodeRef applicationRef) {

        NodeRef emailFolderRef = null;
        try {
            emailFolderRef = getOrCreateEmailFolder(applicationRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //setting filename
        String fileName = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");  //todo: Hvad vil vi have filen til at hedde?
            fileName = sdf.format(mimeMessage.getSentDate()) + ".txt";
        } catch (Exception e) {
            e.printStackTrace();
        }

        //creating the file
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getCMName("name"), fileName);

        NodeRef fileRef = serviceRegistry.getNodeService().createNode(emailFolderRef, ASSOC_CONTAINS,
                getCMName("emailFolder"), TYPE_CONTENT, properties).getChildRef();

        //writing to file
        ContentWriter contentWriter = serviceRegistry.getContentService().getWriter(fileRef, PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        try (OutputStream s = contentWriter.getContentOutputStream(); PrintWriter printWriter = new PrintWriter(s)) {
            Enumeration headers = mimeMessage.getAllHeaderLines();
            while (headers.hasMoreElements()) {
                printWriter.println(headers.nextElement());
            }
            printWriter.println();
            printWriter.println(mimeMessage.getContent());
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
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
        List<NodeRef> emailRefs = getApplicationEmails(applicationRef);
        for (NodeRef ref : emailRefs) {
            if (ref.equals(emailRef)) {
                ContentReader reader = serviceRegistry.getFileFolderService().getReader(ref);
                return reader.getContentString();
            }
        }
        throw new Exception("The requested email was not found");
    }

    /**
     * Gets a list of NodeRefs on all emails saved on a given application
     *
     * @param applicationRef The NodeRef for the Application
     * @return A List of email NodeRefs
     */
    public List<NodeRef> getApplicationEmails(NodeRef applicationRef) {
        NodeRef emailFolder = null;
        try {
            emailFolder = getOrCreateEmailFolder(applicationRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<NodeRef> emailRefs = new ArrayList<>();

        for (ChildAssociationRef ref : serviceRegistry.getNodeService().getChildAssocs(emailFolder)) {
            emailRefs.add(ref.getChildRef());
        }

        return emailRefs;
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
        NodeRef emailFolderRef = null;

        List<ChildAssociationRef> childAssociationRefs = serviceRegistry.getNodeService().getChildAssocs(applicationRef, Utilities.getODFName(APPLICATION_EMAILFOLDER), null);

        if (childAssociationRefs.size() == 0) {
            emailFolderRef = serviceRegistry.getNodeService().createNode(applicationRef, getODFName(APPLICATION_EMAILFOLDER), getCMName(APPLICATION_EMAILFOLDER), TYPE_FOLDER).getChildRef();
        } else if (childAssociationRefs.size() == 1) {
            emailFolderRef = childAssociationRefs.get(0).getChildRef();
        } else {
            throw new Exception("More than one email folder created on application " + applicationRef);
        }

        return emailFolderRef;
    }

    public void deleteApplication(NodeRef applicationRef) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();

        //removing associations
        ns.setAssociations(applicationRef, getODFName(BRANCH_TYPE_NAME), new ArrayList<>());
        ns.setAssociations(applicationRef, getODFName(BUDGET_TYPE_NAME), new ArrayList<>());
        ns.setAssociations(applicationRef, getODFName(STATE_TYPE_NAME), new ArrayList<>());

        //getting the parent
        List<ChildAssociationRef> parentRefs = ns.getParentAssocs(applicationRef, getODFName(DATA_ASSOC_APPLICATIONS), qname -> true);
        if (parentRefs.size() == 0) {
            throw new Exception(applicationRef + "has no parent");
        }
        if (parentRefs.size() != 1) {
            throw new Exception(applicationRef + "has more than one parent");
        }
        NodeRef parentRef = parentRefs.get(0).getParentRef();

        //moving application to odf:deletedApplications
        ns.moveNode(applicationRef, parentRef, getODFName(DATA_ASSOC_DELETED_APPLICATION), null);

        //creating version in application history
        serviceRegistry.getVersionService().createVersion(applicationRef, Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_DELETED));
    }
}
