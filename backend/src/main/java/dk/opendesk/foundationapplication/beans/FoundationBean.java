/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import com.benfante.jslideshare.App;
import dk.opendesk.foundationapplication.DAO.*;
import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import static org.alfresco.repo.action.executer.MailActionExecuter.*;

import dk.opendesk.foundationapplication.enums.StateCategory;
import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.opendesk.repo.model.OpenDeskModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class FoundationBean {

    public final String ONLY_ONE_REFERENCE = "odf.one.ref.requred";
    public final String INVALID_STATE = "odf.bad.state";
    public final String MUST_SPECIFY_STATE = "odf.specify.state";
    public final String INVALID_BRANCH = "odf.bad.branch";

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public NodeRef getDataHome() {
        return Utilities.getDataNode(serviceRegistry);
    }

    public ApplicationReference addNewApplication(Application app) throws Exception {
        NodeRef branchRef = app.getBranchRef() != null ? app.getBranchRef().asNodeRef() : null;
        NodeRef budgetRef = app.getBudget() != null ? app.getBudget().asNodeRef() : null;
        String localName = "Application-" + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        NodeRef newApplication = addNewApplication(branchRef, budgetRef, localName, app.getTitle(), app.getCategory(), app.getRecipient(), app.getAddressRoad(), app.getAddressNumber(), app.getAddressFloor(),
                app.getAddressPostalCode(), app.getContactFirstName(), app.getContactLastName(), app.getContactEmail(), app.getContactPhone(), app.getShortDescription(),
                app.getStartDate(), app.getEndDate(), app.getAmountApplied(), app.getAccountRegistration(), app.getAccountNumber());
        ApplicationReference reference = getApplicationReference(newApplication);
        return reference;
    }

    public NodeRef addNewApplication(NodeRef branchRef, NodeRef budgetRef, String localName, String title, String category, String recipient, String addressRoad, Integer addressNumber, String addressFloor,
            String addressPostalCode, String contactFirstName, String contactLastName, String contactEmail, String contactPhone, String shortDescription,
            Date startDate, Date endDate, Long appliedAmount, String accountRegistration, String accountNumber) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(APPLICATION_PARAM_TITLE), title);
        properties.put(getODFName(APPLICATION_PARAM_CATEGORY), category);
        properties.put(getODFName(APPLICATION_PARAM_RECIPIENT), recipient);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_ROAD), addressRoad);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_NUMBER), addressNumber);
        properties.put(getODFName(APPLICATION_PARAM_ADDR_FLOOR), addressFloor);
        properties.put(getODFName(APPLICATION_PARAM_ARRD_POSTALCODE), addressPostalCode);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_FIRSTNAME), contactFirstName);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_SURNAME), contactLastName);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_EMAIL), contactEmail);
        properties.put(getODFName(APPLICATION_PARAM_PERSON_PHONE), contactPhone);
        properties.put(getODFName(APPLICATION_PARAM_SHORT_DESCRIPTION), shortDescription);
        properties.put(getODFName(APPLICATION_PARAM_START_DATE), startDate);
        properties.put(getODFName(APPLICATION_PARAM_END_DATE), endDate);
        properties.put(getODFName(APPLICATION_PARAM_APPLIED_AMOUNT), appliedAmount);
        properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_REGISTRATION), accountRegistration);
        properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_NUMBER), accountNumber);

        QName applicationTypeQname = getODFName(APPLICATION_TYPE_NAME);
        QName applicationQname = getODFName(localName);
        QName dataAssocApplication = getODFName(DATA_ASSOC_APPLICATIONS);

        NodeRef applicationRef = serviceRegistry.getNodeService().createNode(getDataHome(), dataAssocApplication, applicationQname, applicationTypeQname, properties).getChildRef();
        if (budgetRef != null) {
            serviceRegistry.getNodeService().createAssociation(applicationRef, budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        }
        if (branchRef != null) {
            NodeRef workFlowRef = serviceRegistry.getNodeService().getTargetAssocs(branchRef, getODFName(BRANCH_ASSOC_WORKFLOW)).get(0).getTargetRef();
            List<AssociationRef> workflowEntryRefs = serviceRegistry.getNodeService().getTargetAssocs(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY));
            if (workflowEntryRefs.size() != 1) {
                throw new AlfrescoRuntimeException("Cannot create new application. The workflow on this branch does not have an entry point set");
            }
            serviceRegistry.getNodeService().createAssociation(applicationRef, branchRef, getODFName(APPLICATION_ASSOC_BRANCH));
            serviceRegistry.getNodeService().createAssociation(applicationRef, workflowEntryRefs.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_STATE));
        } else {
            serviceRegistry.getNodeService().createAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
        }

        serviceRegistry.getVersionService().createVersion(applicationRef, null);

        return applicationRef;
    }

    public void updateApplication(Application app) throws Exception {

        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = new HashMap<>();
        if (app.wasTitleSet()) {
            properties.put(getODFName(APPLICATION_PARAM_TITLE), app.getTitle());
        }
        if (app.wasCategorySet()) {
            properties.put(getODFName(APPLICATION_PARAM_CATEGORY), app.getCategory());
        }
        if (app.wasRecipientSet()) {
            properties.put(getODFName(APPLICATION_PARAM_RECIPIENT), app.getRecipient());
        }
        if (app.wasAddressRoadSet()) {
            properties.put(getODFName(APPLICATION_PARAM_ADDR_ROAD), app.getAddressRoad());
        }
        if (app.wasAddressNumberSet()) {
            properties.put(getODFName(APPLICATION_PARAM_ADDR_NUMBER), app.getAddressNumber());
        }
        if (app.wasAddressFloorSet()) {
            properties.put(getODFName(APPLICATION_PARAM_ADDR_FLOOR), app.getAddressFloor());
        }
        if (app.wasAddressPostalCodeSet()) {
            properties.put(getODFName(APPLICATION_PARAM_ARRD_POSTALCODE), app.getAddressPostalCode());
        }
        if (app.wasContactFirstNameSet()) {
            properties.put(getODFName(APPLICATION_PARAM_PERSON_FIRSTNAME), app.getContactFirstName());
        }
        if (app.wasContactLastNameSet()) {
            properties.put(getODFName(APPLICATION_PARAM_PERSON_SURNAME), app.getContactLastName());
        }
        if (app.wasContactEmailSet()) {
            properties.put(getODFName(APPLICATION_PARAM_PERSON_EMAIL), app.getContactEmail());
        }
        if (app.wasContactPhoneSet()) {
            properties.put(getODFName(APPLICATION_PARAM_PERSON_PHONE), app.getContactPhone());
        }
        if (app.wasShortDescriptionSet()) {
            properties.put(getODFName(APPLICATION_PARAM_SHORT_DESCRIPTION), app.getShortDescription());
        }
        if (app.wasStartDateSet()) {
            properties.put(getODFName(APPLICATION_PARAM_START_DATE), app.getStartDate());
        }
        if (app.wasEndDateSet()) {
            properties.put(getODFName(APPLICATION_PARAM_END_DATE), app.getEndDate());
        }
        if (app.wasAmountAppliedSet()) {
            properties.put(getODFName(APPLICATION_PARAM_APPLIED_AMOUNT), app.getAmountApplied());
        }
        if (app.wasAccountRegistrationSet()) {
            properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_REGISTRATION), app.getAccountRegistration());
        }
        if (app.wasAccountNumberSet()) {
            properties.put(getODFName(APPLICATION_PARAM_ACCOUNT_NUMBER), app.getAccountNumber());
        }

        boolean changedWorkflow = false;
        if (app.wasBranchRefSet()) {
            NodeRef newBranchRef = app.getBranchRef().asNodeRef();
            NodeRef currentBranchRef = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BRANCH);

            NodeRef newBranchWorkflow = getSingleTargetAssoc(newBranchRef, BRANCH_ASSOC_WORKFLOW);
            NodeRef currentBranchWorkflow = getSingleTargetAssoc(currentBranchRef, BRANCH_ASSOC_WORKFLOW);

            changedWorkflow = !newBranchWorkflow.equals(currentBranchWorkflow);

            if (changedWorkflow && app.getState() == null) {
                throw new AlfrescoRuntimeException(MUST_SPECIFY_STATE);
            }

            ns.setAssociations(app.asNodeRef(), getODFName(APPLICATION_ASSOC_BRANCH), Collections.singletonList(newBranchRef));

//            if(app.wasStateReferenceSet()){
//                if(app.getState() == null){
//                    clearApplicationState(app.asNodeRef());
//                }else{
//                    
//                }
//            }
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
            NodeRef currentBranch = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BRANCH);
            List<AssociationRef> branchBudgets = ns.getTargetAssocs(currentBranch, getODFName(BRANCH_ASSOC_BUDGETS));
            if (app.getBudget() == null) {
                NodeRef currentBudget = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BUDGET);
                ns.removeAssociation(app.asNodeRef(), currentBudget, getODFName(APPLICATION_ASSOC_BUDGET));
            } else {
                NodeRef newBudget = app.getBudget().asNodeRef();
                boolean found = false;
                for (AssociationRef branchBudget : branchBudgets) {
                    if (newBudget.equals(branchBudget.getTargetRef())) {
                        found = true;
                        break;
                    }
                }
                if(!found){
                    throw new AlfrescoRuntimeException(INVALID_BRANCH);
                }
                ns.setAssociations(app.asNodeRef(), getODFName(APPLICATION_ASSOC_BUDGET), Collections.singletonList(newBudget));
            }
        }
        
        if(app.wasProjectDescriptionDocSet()){
            updateContent(app.getProjectDescriptionDoc(), app, APPLICATION_ASSOC_PROJECT_DESCRIPTION_DOC);
        }
        if(app.wasBudgetDocSet()){
            updateContent(app.getBudgetDoc(), app, APPLICATION_ASSOC_BUDGET_DOC);
        }
        if(app.wasBoardMembersDocSet()){
            updateContent(app.getBoardMembersDoc(), app, APPLICATION_ASSOC_BOARD_MEMBERS_DOC);
        }
        if(app.wasArticlesOfAssociationDocSet()){
            updateContent(app.getArticlesOfAssociationDoc(), app, APPLICATION_ASSOC_ARTICLES_OF_ASSOCIATION_DOC);
        }
        if(app.wasFinancialAccountingDocSet()){
            updateContent(app.getFinancialAccountingDoc(), app, APPLICATION_ASSOC_FINANCIAL_ACCOUTING_DOC);
        }
        
        ns.addProperties(app.asNodeRef(), properties);

        serviceRegistry.getVersionService().createVersion(app.asNodeRef(), null);

    }
    
    private void updateContent(Reference toUpdate, Reference parent, String assoc) throws Exception{
        NodeService ns = serviceRegistry.getNodeService();
        if(toUpdate == null){
            NodeRef currentProject = getSingleTargetAssoc(parent.asNodeRef(), assoc);
            ns.removeAssociation(parent.asNodeRef(), currentProject, getODFName(assoc));
        }else{
            ns.setAssociations(parent.asNodeRef(), getODFName(assoc), Collections.singletonList(toUpdate.asNodeRef()));
        }
    }

    private void setStateDifferentWorkflow(Application app) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        NodeRef newState = app.getState().asNodeRef();
        NodeRef newBranchRef = app.getBranchRef().asNodeRef();
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

    public void setApplicationState(NodeRef applicationRef, NodeRef stateRef) throws Exception {
        serviceRegistry.getNodeService().removeAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
        serviceRegistry.getNodeService().setAssociations(applicationRef, getODFName(APPLICATION_ASSOC_STATE), Collections.singletonList(stateRef));
    }

    public void clearApplicationState(NodeRef applicationRef) throws Exception {
        serviceRegistry.getNodeService().removeAssociation(getDataHome(), applicationRef, getODFName(APPLICATION_ASSOC_STATE));
        serviceRegistry.getNodeService().setAssociations(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS), Collections.singletonList(applicationRef));
    }

    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception {
        return serviceRegistry.getNodeService().getTargetAssocs(applicationRef, getODFName(APPLICATION_ASSOC_BUDGET)).get(0).getTargetRef();
    }

    public NodeRef getApplicationState(NodeRef applicationRef) throws Exception {
        QName applicationStateName = getODFName(APPLICATION_ASSOC_STATE);
        List<AssociationRef> states = serviceRegistry.getNodeService().getTargetAssocs(applicationRef, applicationStateName);
        //The association is singular, it is never a list
        return states != null && !states.isEmpty() ? states.get(0).getTargetRef() : null;
    }
    
    public List<BudgetYear> getCurrentBudgetYears() throws Exception{
        Instant now = Instant.now();
        List<BudgetYearSummary> budgetYears = getBudgetYearSummaries();
        List<BudgetYear> currentBudgets = new ArrayList<>();
        for(BudgetYearSummary budgetYear : budgetYears){
            Instant budgetStartDate = budgetYear.getStartDate().toInstant();
            Instant budgetEndDate = budgetYear.getEndDate().toInstant();
            if(now.isAfter(budgetStartDate) && now.isBefore(budgetEndDate)){
                currentBudgets.add(getBudgetYear(budgetYear.asNodeRef()));
            }
        }
        return currentBudgets;
    }
    
    public NodeRef addNewBudgetYear(String localName, String title, Date startDate, Date endDate) throws Exception{
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

    public Long getBudgetAllocatedFunding(NodeRef budgetRef) throws Exception {
        List<AssociationRef> refs = serviceRegistry.getNodeService().getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BUDGET));
        long totalAllocatedFunding = 0;
        for (AssociationRef ref : refs) {
            Long amount = (Long) serviceRegistry.getNodeService().getProperty(ref.getSourceRef(), getODFName(APPLICATION_PARAM_APPLIED_AMOUNT));
            totalAllocatedFunding = totalAllocatedFunding + amount;
        }
        return totalAllocatedFunding;
    }

    public Long getBudgetTotalFunding(NodeRef budgetRef) throws Exception {
        Long totalAmount = (Long) serviceRegistry.getNodeService().getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT));
        return totalAmount;
    }

    public Long getBudgetRemainingFunding(NodeRef budgetRef) throws Exception {
        Long totalAmount = getBudgetTotalFunding(budgetRef);
        Long usedAmount = getBudgetAllocatedFunding(budgetRef);
        return totalAmount - usedAmount;
    }

    public Application getApplication(NodeRef applicationRef) throws Exception {
        Application application = new Application();
        application.parseRef(applicationRef);
        application.setTitle(getProperty(applicationRef, APPLICATION_PARAM_TITLE, String.class));
        application.setAccountRegistration(getProperty(applicationRef, APPLICATION_PARAM_ACCOUNT_REGISTRATION, String.class));
        application.setAccountNumber(getProperty(applicationRef, APPLICATION_PARAM_ACCOUNT_NUMBER, String.class));
        application.setAddressRoad(getProperty(applicationRef, APPLICATION_PARAM_ADDR_ROAD, String.class));
        application.setAddressNumber(getProperty(applicationRef, APPLICATION_PARAM_ADDR_NUMBER, Integer.class));
        application.setAddressFloor(getProperty(applicationRef, APPLICATION_PARAM_ADDR_FLOOR, String.class));
        application.setAddressPostalCode(getProperty(applicationRef, APPLICATION_PARAM_ARRD_POSTALCODE, String.class));
        application.setContactFirstName(getProperty(applicationRef, APPLICATION_PARAM_PERSON_FIRSTNAME, String.class));
        application.setContactFirstName(getProperty(applicationRef, APPLICATION_PARAM_PERSON_SURNAME, String.class));
        application.setContactEmail(getProperty(applicationRef, APPLICATION_PARAM_PERSON_EMAIL, String.class));
        application.setContactPhone(getProperty(applicationRef, APPLICATION_PARAM_PERSON_PHONE, String.class));
        application.setAmountApplied(getProperty(applicationRef, APPLICATION_PARAM_APPLIED_AMOUNT, Long.class));
        application.setCategory(getProperty(applicationRef, APPLICATION_PARAM_CATEGORY, String.class));
        application.setStartDate(getProperty(applicationRef, APPLICATION_PARAM_START_DATE, Date.class));
        application.setEndDate(getProperty(applicationRef, APPLICATION_PARAM_END_DATE, Date.class));
        application.setRecipient(getProperty(applicationRef, APPLICATION_PARAM_RECIPIENT, String.class));
        application.setShortDescription(getProperty(applicationRef, APPLICATION_PARAM_SHORT_DESCRIPTION, String.class));
        application.setCvr(getProperty(applicationRef, APPLICATION_PARAM_CVR, String.class));

        NodeRef branchRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BRANCH);
        NodeRef budgetRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BUDGET);
        NodeRef stateRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_STATE);
        if (branchRef != null) {
            BranchReference branch = new BranchReference();
            branch.parseRef(branchRef);
            application.setBranchRef(branch);
        }
        if (budgetRef != null) {
            BudgetReference budget = new BudgetReference();
            budget.parseRef(budgetRef);
            application.setBudget(budget);
        }
        if (stateRef != null) {
            application.setState(getStateReference(stateRef));
        }
        
        NodeRef projectDesc = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_PROJECT_DESCRIPTION_DOC);
        NodeRef budgetDoc = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BUDGET_DOC);
        NodeRef boardMembers = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BOARD_MEMBERS_DOC);
        NodeRef articlesOfAssociation = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_ARTICLES_OF_ASSOCIATION_DOC);
        NodeRef financialAccouting = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_FINANCIAL_ACCOUTING_DOC);
        
        if(projectDesc != null){
            application.setProjectDescriptionDoc(Reference.from(projectDesc));
        }
        if(budgetDoc != null){
            application.setBudgetDoc(Reference.from(budgetDoc));
        }
        if(boardMembers != null){
            application.setBoardMembersDoc(Reference.from(boardMembers));
        }
        if(articlesOfAssociation != null){
            application.setArticlesOfAssociationDoc(Reference.from(articlesOfAssociation));
        }
        if(financialAccouting != null){
            application.setFinancialAccountingDoc(Reference.from(financialAccouting));
        }

        return application;

    }

    public BudgetReference getBudgetReference(NodeRef budgetRef) throws Exception {
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
        stateParams.put(stateCategory, category.getCategoryName());

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
        for(ChildAssociationRef budgetYear : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_BUDGETYEARS), null)){
            summaries.add(getBudgetYearSummary(budgetYear.getChildRef()));
        }
        return summaries;
    }
    
    public BudgetYearReference getBudgetYearReference(NodeRef budgetYearRef) throws Exception {
        BudgetYearReference reference = new BudgetYearReference();
        reference.parseRef(budgetYearRef);
        reference.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        return reference;
    }
    
    public BudgetYearSummary getBudgetYearSummary(NodeRef budgetYearRef) throws Exception {
        BudgetYearSummary summary = new BudgetYearSummary();
        summary.parseRef(budgetYearRef);
        summary.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        summary.setStartDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_STARTDATE, Date.class));
        summary.setEndDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_ENDDATE, Date.class));
        return summary;
    }
    
    public BudgetYear getBudgetYear(NodeRef budgetYearRef) throws Exception{
        BudgetYear budgetYear = new BudgetYear();
        budgetYear.parseRef(budgetYearRef);
        budgetYear.setTitle(getProperty(budgetYearRef, BUDGETYEAR_PARAM_TITLE, String.class));
        budgetYear.setStartDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_STARTDATE, Date.class));
        budgetYear.setEndDate(getProperty(budgetYearRef, BUDGETYEAR_PARAM_ENDDATE, Date.class));
        
        List<Budget> budgets = getBudgets(budgetYear);
        Long amountTotal = 0l;
        Long amountAccepted= 0l;
        Long amountNominated= 0l;
        Long amountAvailable= 0l;
        for(Budget budget : budgets){
            amountTotal =+ budget.getAmountTotal();
            amountAccepted =+ budget.getAmountAccepted();
            amountNominated =+ budget.getAmountNominated();
            amountAvailable =+ budget.getAmountAvailable();
        }
        
        budgetYear.setAmountTotal(amountTotal);
        budgetYear.setAmountAccepted(amountAccepted);
        budgetYear.setAmountNominated(amountNominated);
        budgetYear.setAmountAvailable(amountAvailable);
        
        return budgetYear;
    }
    
    public List<Budget> getBudgets(BudgetYearReference budgetYear) throws Exception {
        List<Budget> budgets = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for (NodeRef budgetRef : getBudgetRefs(budgetYear.asNodeRef())) {
            Budget budget = new Budget();
            budget.parseRef(budgetRef);
            budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
            budget.setAmountTotal((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));
            
            List<ApplicationReference> applications = new ArrayList<>();
            for (AssociationRef applicationRef : ns.getSourceAssocs(budgetRef, getODFName(APPLICATION_ASSOC_BRANCH))) {
                applications.add(getApplicationReference(applicationRef.getSourceRef()));
                State state = getState(getApplicationState(applicationRef.getSourceRef()));
            }
            
            budgets.add(budget);
        }
        return budgets;
    }
    
    public List<BudgetSummary> getBudgetSummaries(BudgetYearReference budgetYear) throws Exception {
        List<BudgetSummary> budgets = new ArrayList<>();
        NodeService ns = serviceRegistry.getNodeService();
        for (NodeRef budgetRef : getBudgetRefs(budgetYear.asNodeRef())) {
            BudgetSummary budget = new BudgetSummary();
            budget.parseRef(budgetRef);
            budget.setTitle((String) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_TITLE)));
            budget.setAmountTotal((Long) ns.getProperty(budgetRef, getODFName(BUDGET_PARAM_AMOUNT)));
            budgets.add(budget);
        }
        return budgets;
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
            branches.add(summary);

        }

        return branches;
    }

    public BranchReference getBranchReference(NodeRef branchRef) throws Exception {
        BranchReference branchReference = new BranchReference();
        branchReference.parseRef(branchRef);
        branchReference.setTitle(getProperty(branchRef, BRANCH_PARAM_TITLE, String.class));
        return branchReference;
    }

    public Branch getBranch(NodeRef branchRef) throws Exception {
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
            NodeRef appRef = ref.getTargetRef();
            applications.add(getApplicationSummary(appRef));
        }
        return applications;

    }

    public ApplicationReference getApplicationReference(NodeRef applicationRef) throws Exception {
        ApplicationReference reference = new ApplicationReference();
        reference.parseRef(applicationRef);
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

    public List<ApplicationSummary> getNewApplicationSummaries() throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (AssociationRef applicationRef : ns.getTargetAssocs(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS))) {
            toReturn.add(getApplicationSummary(applicationRef.getTargetRef()));
        }
        return toReturn;
    }

    public ApplicationSummary getApplicationSummary(NodeRef applicationSummary) throws Exception {
        ApplicationSummary app = new ApplicationSummary();
        app.parseRef(applicationSummary);

        BranchReference branchReference = getBranchReference(applicationSummary);
        app.setBranchRef(branchReference);
        app.setTitle(getProperty(applicationSummary, APPLICATION_PARAM_TITLE, String.class));
        app.setAmountApplied(getProperty(applicationSummary, APPLICATION_PARAM_APPLIED_AMOUNT, Long.class));
        app.setCategory(getProperty(applicationSummary, APPLICATION_PARAM_CATEGORY, String.class));
        app.setStartDate(getProperty(applicationSummary, APPLICATION_PARAM_START_DATE, Date.class));
        app.setEndDate(getProperty(applicationSummary, APPLICATION_PARAM_END_DATE, Date.class));
        app.setRecipient(getProperty(applicationSummary, APPLICATION_PARAM_RECIPIENT, String.class));
        app.setShortDescription(getProperty(applicationSummary, APPLICATION_PARAM_SHORT_DESCRIPTION, String.class));
        app.setCvr(getProperty(applicationSummary, APPLICATION_PARAM_CVR, String.class));


        return app;
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

    public List<ParameterDefinition> getActionParameters(String actionName) {
        ActionDefinition actionDefinition = serviceRegistry.getActionService().getActionDefinition(actionName);
        return actionDefinition.getParameterDefinitions();
    }

    public ActionDefinition getAction(String actionName) {
        return serviceRegistry.getActionService().getActionDefinition(actionName);
    }

    public void saveAction(String actionName, NodeRef stateRef, QName aspect, Map<String, Serializable> params) {
        Action action = serviceRegistry.getActionService().createAction(actionName, params);
        serviceRegistry.getActionService().saveAction(stateRef,action);
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


    public Action configureEmailAction(String templateName, String subject, String fromAddress) {
        Action action = serviceRegistry.getActionService().createAction("foundationMail");

        String query = "PATH:\"" + OpenDeskModel.TEMPLATE_OD_FOLDER + "cm:" + templateName + "\"";
        ResultSet resultSet = serviceRegistry.getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
                SearchService.LANGUAGE_LUCENE, query);
        NodeRef templateRef = resultSet.getNodeRef(0);

        action.setParameterValue(PARAM_TEMPLATE, templateRef);
        action.setParameterValue(PARAM_SUBJECT, subject);
        action.setParameterValue(PARAM_FROM, fromAddress);

        return action;
    }

}
