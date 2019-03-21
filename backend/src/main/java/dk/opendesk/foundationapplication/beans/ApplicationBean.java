/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationChange;
import dk.opendesk.foundationapplication.DAO.ApplicationChangeUnit;
import dk.opendesk.foundationapplication.DAO.ApplicationBlock;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import dk.opendesk.foundationapplication.Utilities;

import java.io.Serializable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.alfresco.model.ContentModel.TYPE_FOLDER;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;

import static dk.opendesk.foundationapplication.Utilities.*;

/**
 *
 * @author martin
 */
public class ApplicationBean extends FoundationBean{
    
    private ActionBean actionBean;
    private BranchBean branchBean;
    private BudgetBean budgetBean;
    private WorkflowBean workflowBean;

    public void setActionBean(ActionBean actionBean) {
        this.actionBean = actionBean;
    }

    public void setBranchBean(BranchBean branchBean) {
        this.branchBean = branchBean;
    }

    public void setBudgetBean(BudgetBean budgetBean) {
        this.budgetBean = budgetBean;
    }

    public void setWorkflowBean(WorkflowBean workflowBean) {
        this.workflowBean = workflowBean;
    }
    
    public ApplicationReference addNewApplication(String id, NodeRef branchRef, NodeRef budgetRef, String title, ApplicationBlock... blocks) throws Exception {
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
            application.setId(Utilities.getNextApplicationID(getServiceRegistry()) + "");
        }
        properties.put(getODFName(APPLICATION_PARAM_ID), application.getId());
        properties.put(getODFName(APPLICATION_PARAM_TITLE), application.getTitle());

        ArrayList<String> blockStrings = new ArrayList<>();
        List<ApplicationBlock> blocks = application.getBlocks();
        if (blocks != null) {
            for (ApplicationBlock block : blocks) {
                String blockString = blockMapper.writeValueAsString(block);
                blockStrings.add(blockString);
            }
        }

        properties.put(getODFName(APPLICATION_PARAM_BLOCKS), blockStrings);

        QName applicationTypeQname = getODFName(APPLICATION_TYPE_NAME);
        QName applicationQname = getODFName(application.getTitle());
        QName dataAssocApplication = getODFName(DATA_ASSOC_APPLICATIONS);

        NodeRef applicationRef = getServiceRegistry().getNodeService().createNode(getDataHome(), dataAssocApplication, applicationQname, applicationTypeQname, properties).getChildRef();
        if (application.getBudget() != null) {
            getServiceRegistry().getNodeService().createAssociation(applicationRef, application.getBudget().asNodeRef(), getODFName(APPLICATION_ASSOC_BUDGET));
        }
        if (application.getBranchSummary() != null) {
            NodeRef workFlowRef = getServiceRegistry().getNodeService().getTargetAssocs(application.getBranchSummary().asNodeRef(), getODFName(BRANCH_ASSOC_WORKFLOW)).get(0).getTargetRef();
            List<AssociationRef> workflowEntryRefs = getServiceRegistry().getNodeService().getTargetAssocs(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY));
            if (workflowEntryRefs.size() != 1) {
                throw new AlfrescoRuntimeException("Cannot create new application. The workflow on this branch does not have an entry point set");
            }
            getServiceRegistry().getNodeService().createAssociation(applicationRef, application.getBranchSummary().asNodeRef(), getODFName(APPLICATION_ASSOC_BRANCH));
            getServiceRegistry().getNodeService().createAssociation(applicationRef, workflowEntryRefs.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_STATE));
        } else {
            getServiceRegistry().getNodeService().createAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
        }

        getServiceRegistry().getVersionService().createVersion(applicationRef, Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_CREATED));

        return getApplicationReference(applicationRef);
    }

    public void updateApplication(Application app) throws Exception {
        String currentUser = getCurrentUserName();
        NodeService ns = getServiceRegistry().getNodeService();
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

        List<ApplicationBlock> oldBlocks = getApplication(app.asNodeRef()).getBlocks();

        if (app.wasBlocksSet() && app.getBlocks() != null) {
            for (ApplicationBlock block : app.getBlocks()) {
                if (block.getId() != null) {
                    ApplicationBlock oldBlock = getBlockByID(block.getId(), oldBlocks);
                    if (oldBlock == null) {
                        oldBlocks.add(block);
                    } else {
                        if (block.wasLabelSet()) {
                            oldBlock.setLabel(block.getLabel());
                        }
                        if (block.wasLayoutSet()) {
                            oldBlock.setLayout(block.getLayout());
                        }
                        if (block.wasIconSet()) {
                            oldBlock.setIcon(block.getIcon());
                        }
                        if (block.wasCollapsibleSet()) {
                            oldBlock.setCollapsible(block.getCollapsible());
                        }
                        if (block.wasRepeatableSet()) {
                            oldBlock.setRepeatable(block.getRepeatable());
                        }
                        //todo
                        if (block.wasFieldsSet()) {
                            if (block.getFields() == null) {
                                oldBlock.setFields(null);
                            } else {
                                for (ApplicationFieldValue field : block.getFields()) {
                                    if (field.getId() != null) {
                                        ApplicationFieldValue oldField = getFieldByID(field.getId(), oldBlock.getFields());
                                        if (oldField == null) {
                                            oldBlock.getFields().add(field);
                                        } else {
                                            if (field.wasLabelSet()) {
                                                oldField.setLabel(field.getLabel());
                                            }
                                            if (field.wasTypeSet()) {
                                                oldField.setType(field.getType());
                                            }
                                            if (field.wasLayoutSet()) {
                                                oldField.setLayout(field.getLayout());
                                            }
                                            if (field.wasComponentSet()) {
                                                oldField.setComponent(field.getComponent());
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
                                            if (field.wasHintSet()) {
                                                oldField.setHint(field.getHint());
                                            }
                                            if (field.wasWrapperSet()) {
                                                oldField.setWrapper(field.getWrapper());
                                            }
                                            if (field.wasValidationSet()) {
                                                oldField.setValidation(field.getValidation());
                                            }
                                            if (field.wasPermissionsSet()) {
                                                oldField.setPermissions(field.getPermissions());
                                            }
                                            if (field.wasReadOnlySet()) {
                                                oldField.setReadOnly(field.getReadOnly());
                                            }
                                            //todo
                                        }
                                    } else {
                                        getLogger().warn("Found field without ID: " + field + " in block: " + block);
                                    }

                                }
                            }
                        }
                    }
                } else {
                    getLogger().warn("Found block without ID: " + block);
                }

            }
            ObjectMapper mapper = Utilities.getMapper();
            ArrayList<String> blockStrings = new ArrayList<>();
            for (ApplicationBlock block : oldBlocks) {
                blockStrings.add(mapper.writeValueAsString(block));
            }
            properties.put(getODFName(APPLICATION_PARAM_BLOCKS), blockStrings);
        }

        ns.addProperties(app.asNodeRef(), properties);

        getServiceRegistry().getVersionService().createVersion(app.asNodeRef(), Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_UPDATE));
    }
    
    private ApplicationBlock getBlockByID(String id, List<ApplicationBlock> blocks) {
        for (ApplicationBlock block : blocks) {
            if (Objects.equals(id, block.getId())) {
                return block;
            }
        }
        return null;
    }

    private ApplicationFieldValue getFieldByID(String id, List<ApplicationFieldValue> fields) {
        for (ApplicationFieldValue field : fields) {
            if (Objects.equals(id, field.getId())) {
                return field;
            }
        }
        return null;
    }

    private void setStateDifferentWorkflow(Application app) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
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
        NodeService ns = getServiceRegistry().getNodeService();
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
        getServiceRegistry().getNodeService().removeAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
        getServiceRegistry().getNodeService().setAssociations(applicationRef, getODFName(APPLICATION_ASSOC_STATE), Collections.singletonList(stateRef));
    }

    private void clearApplicationState(NodeRef applicationRef) throws Exception {
        getServiceRegistry().getNodeService().removeAssociation(getDataHome(), applicationRef, getODFName(APPLICATION_ASSOC_STATE));
        getServiceRegistry().getNodeService().setAssociations(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS), Collections.singletonList(applicationRef));
    }

    public NodeRef getApplicationBudget(NodeRef applicationRef) throws Exception {
        return getServiceRegistry().getNodeService().getTargetAssocs(applicationRef, getODFName(APPLICATION_ASSOC_BUDGET)).get(0).getTargetRef();
    }

    public NodeRef getApplicationState(NodeRef applicationRef) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationRef);

        QName applicationStateName = getODFName(APPLICATION_ASSOC_STATE);
        List<AssociationRef> states = getServiceRegistry().getNodeService().getTargetAssocs(applicationRef, applicationStateName);
        //The association is singular, it is never a list
        return states != null && !states.isEmpty() ? states.get(0).getTargetRef() : null;
    }
    
        public boolean isApplicationSeen(NodeRef appRef, String userName) throws Exception {
        List<String> seenByList = (List<String>) getServiceRegistry().getNodeService().getProperty(appRef, getODFName(APPLICATION_PARAM_SEEN_BY));
        //Set<String> seenBySet = new HashSet<>(seenByList);
        if (seenByList == null) {
            return false;
        }
        return seenByList.contains(userName);
    }

    public ApplicationReference getApplicationReference(NodeRef applicationRef) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationRef);

        ApplicationReference reference = new ApplicationReference();
        reference.parseRef(applicationRef);
        reference.setId(getProperty(applicationRef, APPLICATION_PARAM_ID, String.class));
        reference.setTitle(getProperty(applicationRef, APPLICATION_PARAM_TITLE, String.class));
        reference.setIsSeen(isApplicationSeen(applicationRef, getCurrentUserName()));
        return reference;
    }

    public List<ApplicationSummary> getApplicationSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (ChildAssociationRef applicationRef : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_APPLICATIONS), null)) {
            toReturn.add(getApplicationSummary(applicationRef.getChildRef()));
        }
        return toReturn;
    }

    public List<ApplicationSummary> getDeletedApplicationSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (ChildAssociationRef applicationRef : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_DELETED_APPLICATION), null)) {
            toReturn.add(getApplicationSummary(applicationRef.getChildRef()));
        }
        return toReturn;
    }

    public List<ApplicationSummary> getNewApplicationSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
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
            BranchSummary branchSummary = branchBean.getBranchSummary(branchRef);
            app.setBranchSummary(branchSummary);
        }
        app.setId(getProperty(applicationSummary, APPLICATION_PARAM_ID, String.class));
        app.setTitle(getProperty(applicationSummary, APPLICATION_PARAM_TITLE, String.class));
        app.setIsSeen(isApplicationSeen(applicationSummary, getCurrentUserName()));
        List<String> blockStrings = getProperty(applicationSummary, APPLICATION_PARAM_BLOCKS, List.class);
        List<ApplicationBlock> blocks = new ArrayList<>();

        if (blockStrings != null) {
            for (String blockString : blockStrings) {
                blocks.add(mapper.readValue(blockString, ApplicationBlock.class));
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
        List<ApplicationBlock> blocks = new ArrayList<>();

        for (String blockString : blockStrings) {
            blocks.add(mapper.readValue(blockString, ApplicationBlock.class));
        }
        application.setBlocks(blocks);

        NodeRef branchRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BRANCH);
        NodeRef budgetRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BUDGET);
        NodeRef stateRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_STATE);

        if (branchRef != null) {
            application.setBranchSummary(branchBean.getBranchSummary(branchRef));
        }
        if (budgetRef != null) {
            BudgetReference budget = budgetBean.getBudgetReference(budgetRef);
            application.setBudget(budget);
        }
        if (stateRef != null) {
            StateReference state = workflowBean.getStateReference(stateRef);
            application.setState(state);
            NodeRef workflowRef = getSingleParentAssoc(stateRef, WORKFLOW_ASSOC_STATES);
            if (workflowRef != null) {
                WorkflowReference workflow = workflowBean.getWorkflowReference(workflowRef);
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
        ResultSet set = getServiceRegistry().getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "LANGUAGE_LUCENE", "TYPE:\"odf:application\" AND @odf:applicationID:\"" + id + "\"");
        if (set.length() == 0) {
            return null;
        }
        ChildAssociationRef ref = set.getRow(0).getChildAssocRef();
        return getApplicationReference(ref.getChildRef());
    }
    
    public List<ApplicationChangeUnit> getApplicationDifference(Application oldVersion, Application newVersion) {

        List<ApplicationChangeUnit> changes = new ArrayList<>();

        Map<String, ApplicationFieldValue> newVersionProperties = getApplicationFields(newVersion);

        if (oldVersion != null) {
            Map<String, ApplicationFieldValue> oldVersionProperties = getApplicationFields(oldVersion);

            for (String key : oldVersionProperties.keySet()) {
                ApplicationFieldValue oldValueField = oldVersionProperties.get(key);
                ApplicationFieldValue newValueField = newVersionProperties.get(key);
                if (newValueField != null) {
                    if (!Objects.equals(oldValueField.getValue(), newValueField.getValue())) {
                        changes.add(new ApplicationChangeUnit().setChangedField(oldValueField.getLabel()).setOldValueWithObject(oldValueField.getValue()).setNewValueWithObject(newValueField.getValue()).setChangeType(APPLICATION_CHANGE_UPDATE_PROP));
                    }
                } else {
                    changes.add(new ApplicationChangeUnit().setChangedField(oldValueField.getLabel()).setOldValueWithObject(oldValueField.getValue()).setChangeType(APPLICATION_CHANGE_UPDATE_PROP));
                }
                newVersionProperties.remove(key);
            }
        }

        for (String key : newVersionProperties.keySet()) {
            ApplicationFieldValue newValueField = newVersionProperties.get(key);
            changes.add(new ApplicationChangeUnit().setChangedField(newValueField.getLabel()).setNewValueWithObject(newValueField.getValue()).setChangeType(APPLICATION_CHANGE_UPDATE_PROP));
        }

        if (oldVersion != null) {
            resolveChangedAssociation(oldVersion.getBranchSummary(), newVersion.getBranchSummary(), "Branch", changes);
            resolveChangedAssociation(oldVersion.getBudget(), newVersion.getBudget(), "Budget", changes);
            resolveChangedAssociation(oldVersion.getState(), newVersion.getState(), "State", changes);
            resolveChangedAssociation(oldVersion.getWorkflow(), newVersion.getWorkflow(), "Workflow", changes);
        } else {
            resolveChangedAssociation(null, newVersion.getBranchSummary(), "Branch", changes);
            resolveChangedAssociation(null, newVersion.getBudget(), "Budget", changes);
            resolveChangedAssociation(null, newVersion.getState(), "State", changes);
            resolveChangedAssociation(null, newVersion.getWorkflow(), "Workflow", changes);
        }

        return changes;
    }
    
    public Map<String, ApplicationFieldValue> getApplicationFields(Application application){
        if (application == null) {
            return null;
        }
        Map<String, ApplicationFieldValue> toReturn = new HashMap<>();
        for (ApplicationBlock block : application.getBlocks()) {
            for (ApplicationFieldValue field : block.getFields()) {
                toReturn.put(field.getId(), field);
            }
        }
        return toReturn;
    }

    
    public <E extends Reference> void resolveChangedAssociation(E oldType, E newType, String associationName, List<ApplicationChangeUnit> changes){
        if(!Objects.equals(oldType, newType)){
            ApplicationChangeUnit changeUnit = new ApplicationChangeUnit().setChangedField(associationName).setChangeType(APPLICATION_CHANGE_UPDATE_ASSOCIATION);
            if (oldType != null) {
                changeUnit.setOldValue(oldType.getTitle());
            }
            if (newType != null) {
                changeUnit.setNewValue(newType.getTitle());
            }
            changes.add(changeUnit);
        }
    }
    
     public void deleteApplication(NodeRef applicationRef) throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();

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
        getServiceRegistry().getVersionService().createVersion(applicationRef, Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_DELETED));
    }
     
         /**
     * Gets a list of NodeRefs on all emails saved on a given application
     *
     * @param applicationRef The NodeRef for the Application
     * @return A List of email NodeRefs
     */
    public List<NodeRef> getApplicationEmails(NodeRef applicationRef) throws Exception {
        NodeRef emailFolder = getOrCreateEmailFolder(applicationRef);

        List<NodeRef> emailRefs = new ArrayList<>();

        for (ChildAssociationRef ref : getServiceRegistry().getNodeService().getChildAssocs(emailFolder)) {
            emailRefs.add(ref.getChildRef());
        }

        return emailRefs;
    }
    
    public List<ApplicationChange> getApplicationHistory(NodeRef appRef) throws Exception {
        List<ApplicationChange> changes = new ArrayList<>();

        //getting versions
        VersionHistory history = getServiceRegistry().getVersionService().getVersionHistory(appRef);
        Version current = history.getHeadVersion();

        while (current != null) {
            Version predecessor = history.getPredecessor(current);
            changes.add(getVersionDifference(predecessor, current));
            current = predecessor;
        }

        //getting emails
        for (NodeRef ref : getApplicationEmails(appRef)) {
            ApplicationChangeUnit unit = new ApplicationChangeUnit()
                    .setNewValueWithObject(ref)
                    .setChangeType(APPLICATION_CHANGE_UPDATE_EMAIL)
                    .setNewValueLink("/foundation/application/" + appRef.getId() + "/email/" + ref.getId()); //TODO er dette det rigtige link?
            Date timeStamp = getServiceRegistry().getFileFolderService().getFileInfo(ref).getCreatedDate();
            changes.add(new ApplicationChange().setChangeType(APPLICATION_CHANGE_UPDATE_EMAIL).setTimeStamp(timeStamp).setChangeList(Collections.singletonList(unit)));
        }

        //sort oldest first
        changes.sort((o1, o2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
            Date date1 = sdf.parse((String)o1.getTimesStamp(),new ParsePosition(0));
            Date date2 = sdf.parse((String)o2.getTimesStamp(),new ParsePosition(0));
            return date1.compareTo(date2);
        });

        return changes;
    }


    public ApplicationChange getVersionDifference(Version oldVersion, Version newVersion) throws Exception {
        if (newVersion == null) {
            throw new Exception("newVersion must not be null");
        }

        String changeType = (String) newVersion.getVersionProperty(APPLICATION_CHANGE);

        Date timeStamp = newVersion.getFrozenModifiedDate();
        String modifier = newVersion.getFrozenModifier();
        NodeRef modifierId = getServiceRegistry().getPersonService().getPerson(modifier);

        Application newApp = getApplication(newVersion.getFrozenStateNodeRef());
        Application oldApp = (oldVersion == null) ? null : getApplication(oldVersion.getFrozenStateNodeRef());

        return new ApplicationChange().setChangeType(changeType).setTimeStamp(timeStamp).setModifier(modifier).setModifierIdWithNodeRef(modifierId).setChangeList(getApplicationDifference(oldApp, newApp));
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
        return getOrCreateSingleFolder(applicationRef, "email");
    }


    /**
     * Gets the document folder for an application or creates it if it does not
     * exists.
     *
     * @param applicationRef Application nodeRef
     * @return Document folder nodeRef
     * @throws Exception if there are more than one document folder on the
     * application.
     */
    public NodeRef getOrCreateDocumentFolder(NodeRef applicationRef) throws Exception {
        return getOrCreateSingleFolder(applicationRef, "doc");
    }


    private NodeRef getOrCreateSingleFolder(NodeRef parentRef, String folderType) throws Exception {
        NodeRef folderRef;
        String folderName;

        switch (folderType) {
            case "email":
                folderName = APPLICATION_FOLDER_EMAIL;
                break;
            case "doc":
                folderName = APPLICATION_FOLDER_DOCUMENT;
                break;
            default:
                throw new Exception("folderType " + folderType + " not valid");
        }

        List<ChildAssociationRef> childAssociationRefs = getServiceRegistry().getNodeService().getChildAssocs(parentRef, Utilities.getODFName(folderName), null);

        if (childAssociationRefs.size() == 0) {
            folderRef = getServiceRegistry().getNodeService().createNode(parentRef, getODFName(folderName), getCMName(folderName), TYPE_FOLDER).getChildRef();
        } else if (childAssociationRefs.size() == 1) {
            folderRef = childAssociationRefs.get(0).getChildRef();
        } else {
            throw new Exception("More than one folder of type " + folderName + " created on application " + parentRef);
        }

        return folderRef;

    }
}
