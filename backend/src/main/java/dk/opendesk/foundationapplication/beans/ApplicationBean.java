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
import dk.opendesk.foundationapplication.DAO.ApplicationBlockSpecification;
import dk.opendesk.foundationapplication.DAO.ApplicationField;
import dk.opendesk.foundationapplication.DAO.ApplicationFieldValue;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSchema;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.MultiFieldData;
import dk.opendesk.foundationapplication.DAO.MultiFieldDataValue;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import dk.opendesk.foundationapplication.Utilities;

import java.io.Serializable;
import java.text.ParsePosition;
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
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import dk.opendesk.foundationapplication.enums.StateCategory;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;

/**
 *
 * @author martin
 */
public class ApplicationBean extends FoundationBean {
    private static final String ERROR_CREATING_FIELD = "odf.create.userfield";
    

    private ActionBean actionBean;
    private AuthorityBean authBean;
    private BranchBean branchBean;
    private BudgetBean budgetBean;
    private WorkflowBean workflowBean;

    public void setActionBean(ActionBean actionBean) {
        this.actionBean = actionBean;
    }

    public void setAuthBean(AuthorityBean authBean) {
        this.authBean = authBean;
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

        QName applicationTypeQname = getODFName(APPLICATION_TYPE_NAME);
        QName applicationQname = getODFName(application.getTitle());
        QName dataAssocApplication = getODFName(DATA_ASSOC_APPLICATIONS);

        NodeRef applicationRef
                = getServiceRegistry().getRetryingTransactionHelper().doInTransaction(() -> {
                    NodeRef newApplicationRef = getServiceRegistry().getNodeService().createNode(getDataHome(), dataAssocApplication, applicationQname, applicationTypeQname, properties).getChildRef();

                    application.parseRef(newApplicationRef);
                    List<ApplicationBlock> blocks = application.getBlocks();
                    if (blocks != null) {
                        for (ApplicationBlock block : blocks) {
                            addBlockToApplication(application, block);
                        }
                    }

                    return newApplicationRef;
                });

        if (application.getBudget() != null) {
            getServiceRegistry().getNodeService().createAssociation(applicationRef, application.getBudget().asNodeRef(), getODFName(APPLICATION_ASSOC_BUDGET));
            authBean.addFullPermission(applicationRef, PermissionGroup.BUDGET, application.getBudget());
        }
        if (application.getBranchSummary() != null) {
            NodeRef workFlowRef = getServiceRegistry().getNodeService().getTargetAssocs(application.getBranchSummary().asNodeRef(), getODFName(BRANCH_ASSOC_WORKFLOW)).get(0).getTargetRef();
            List<AssociationRef> workflowEntryRefs = getServiceRegistry().getNodeService().getTargetAssocs(workFlowRef, getODFName(WORKFLOW_ASSOC_ENTRY));
            if (workflowEntryRefs.size() != 1) {
                throw new AlfrescoRuntimeException("Cannot create new application. The workflow on this branch does not have an entry point set");
            }
            getServiceRegistry().getNodeService().createAssociation(applicationRef, application.getBranchSummary().asNodeRef(), getODFName(APPLICATION_ASSOC_BRANCH));
            getServiceRegistry().getNodeService().createAssociation(applicationRef, workflowEntryRefs.get(0).getTargetRef(), getODFName(APPLICATION_ASSOC_STATE));
            authBean.addFullPermission(applicationRef, PermissionGroup.WORKFLOW, application.getBranchSummary().getWorkflowRef());
        } else {
            getServiceRegistry().getNodeService().createAssociation(getDataHome(), applicationRef, getODFName(DATA_ASSOC_NEW_APPLICATIONS));
            authBean.addFullPermission(applicationRef, PermissionGroup.NEW_APPLICATION);
        }
        getServiceRegistry().getPermissionService().setInheritParentPermissions(applicationRef, false);

        getServiceRegistry().getVersionService().createVersion(applicationRef, Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_CREATED));
        
        return getApplicationReference(applicationRef);
    }
    
    public void updateApplicationStaticData(List<ApplicationField> fields) throws Exception {
        for (ApplicationField field : fields) {
            updateStaticField(field);
        }
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
                if (currentBranchRef != null) {
                    NodeRef currentBranchWorkflow = getSingleTargetAssoc(currentBranchRef, BRANCH_ASSOC_WORKFLOW);
                    changedWorkflow = !newBranchWorkflow.equals(currentBranchWorkflow);
                    authBean.removeFullPermission(currentBranchRef, PermissionGroup.BRANCH, branchBean.getBranchReference(currentBranchRef));
                } else {
                    changedWorkflow = true;
                }

//                if (changedWorkflow && app.getState() == null) {
//                    throw new AlfrescoRuntimeException(MUST_SPECIFY_STATE);
//                }
                authBean.addFullPermission(newBranchRef, PermissionGroup.BRANCH, branchBean.getBranchReference(newBranchRef));
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
        } else if (changedWorkflow) {
            setStateDifferentWorkflow(app);
        }

        if (app.wasBudgetSet()) {
            NodeRef currentBudget = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BUDGET);
            NodeRef currentBranch = getSingleTargetAssoc(app.asNodeRef(), APPLICATION_ASSOC_BRANCH);
            List<AssociationRef> branchBudgets = ns.getTargetAssocs(currentBranch, getODFName(BRANCH_ASSOC_BUDGETS));
            authBean.removeFullPermission(app.asNodeRef(), PermissionGroup.BUDGET, budgetBean.getBudgetReference(currentBudget));
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
                    authBean.addFullPermission(app.asNodeRef(), PermissionGroup.BUDGET, budgetBean.getBudgetReference(newBudget));
                }

            }
        }

        List<ApplicationBlock> currentBlocks = getApplication(app.asNodeRef()).getBlocks();

        if (app.wasBlocksSet() && app.getBlocks() != null) {
            for (ApplicationBlock block : app.getBlocks()) {
                if (block.getId() != null) {
                    ApplicationBlock currentBlock = getBlockByID(block.getId(), currentBlocks);
                    if (currentBlock == null) {
                        addBlockToApplication(app, block);
                    } else {
                        block.parseRef(currentBlock.asNodeRef());
                        updateBlock(block);

                        if (block.wasFieldsSet()) {
                            if (block.getFields() == null) {
                                currentBlock.setFields(null);
                            } else {
                                for (ApplicationFieldValue field : block.getFields()) {
                                    if (field.getId() != null) {
                                        ApplicationFieldValue currentField = getFieldByID(field.getId(), currentBlock.getFields());
                                        if (currentField == null) {
                                            addFieldToBlock(app, currentBlock, field);
                                        } else {
                                            field.parseRef(currentField.asNodeRef());
                                            updateField(field);

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

        }

        ns.addProperties(app.asNodeRef(), properties);

        getServiceRegistry().getVersionService().createVersion(app.asNodeRef(), Collections.singletonMap(APPLICATION_CHANGE, APPLICATION_CHANGE_UPDATE));
    }


    protected void addBlockToApplication(Application application, ApplicationBlock newBlock) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(BLOCK_PARAM_ID), newBlock.getId());
        properties.put(getODFName(BLOCK_PARAM_LABEL), newBlock.getLabel());
        properties.put(getODFName(BLOCK_PARAM_LAYOUT), newBlock.getLayout());
        properties.put(getODFName(BLOCK_PARAM_ICON), newBlock.getIcon());
        properties.put(getODFName(BLOCK_PARAM_COLLAPSIBLE), newBlock.getCollapsible());
        properties.put(getODFName(BLOCK_PARAM_REPEATABLE), newBlock.getRepeatable());

        NodeRef newBlockRef = getServiceRegistry().getNodeService().createNode(application.asNodeRef(), getODFName(APPLICATION_ASSOC_BLOCKS), getODFName(newBlock.getId()), getODFName(BLOCKIMPL_TYPE_NAME), properties).getChildRef();

        
        
        //Add all fields on block as well
        if(newBlock.getFields() != null){
            for (ApplicationFieldValue field : newBlock.getFields()) {
                addFieldToBlock(application, newBlockRef, field);
            }
        }
    }

    protected void addFieldToBlock(Application containingApplication, ApplicationBlock block, MultiFieldDataValue field) throws Exception {
        addFieldToBlock(containingApplication, block.asNodeRef(), field);

    }

    protected void addFieldToBlock(Application containingApplication, NodeRef blockRef, MultiFieldDataValue field) throws Exception {
        NodeRef staticFieldRef = getServiceRegistry().getNodeService().getChildByName(getDataHome(), getODFName(DATA_ASSOC_STATIC_FIELDS), field.getId());
        if (staticFieldRef == null) {
            staticFieldRef = addStaticField(field);
            authBean.addFullPermission(staticFieldRef, PermissionGroup.BASIC, (NodeRef)null);
        }
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(FIELD_PARAM_OPTIONS), field.getOptions());
        if(!getServiceRegistry().getNodeService().hasAspect(staticFieldRef, getODFName(STATICMULTIFIELD_ASPECT_NAME))){
            properties.put(getODFName(FIELD_PARAM_VALUE), field.getValue());
        }
        
        NodeRef fieldRef = getServiceRegistry().getNodeService().createNode(blockRef, getODFName(BLOCKIMPL_ASSOC_FIELDS), getODFName(field.getId()), getODFName(FIELD_TYPE_NAME), properties).getChildRef();

        getServiceRegistry().getNodeService().setAssociations(fieldRef, getODFName(FIELD_ASSOC_STATICDATA), Collections.singletonList(staticFieldRef));
        
        if(getServiceRegistry().getNodeService().hasAspect(staticFieldRef, getODFName(STATICMULTIFIELD_ASPECT_NAME))){
            getServiceRegistry().getNodeService().addAspect(fieldRef, getODFName(MULTIFIELD_ASPECT_NAME), null);
        }
                
    }

    protected NodeRef addStaticField(MultiFieldData field) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(getODFName(STATICFIELD_PARAM_ID), field.getId());
        properties.put(getODFName(STATICFIELD_PARAM_LABEL), field.getLabel());
        properties.put(getODFName(STATICFIELD_PARAM_LAYOUT), field.getLayout());
        properties.put(getODFName(STATICFIELD_PARAM_HINT), field.getHint());
        properties.put(getODFName(STATICFIELD_PARAM_DESCRIBES), field.getDescribes());
        properties.put(getODFName(STATICFIELD_PARAM_COMPONENT), field.getComponent());
        properties.put(getODFName(STATICFIELD_PARAM_WRAPPER), field.getWrapper());
        properties.put(getODFName(STATICFIELD_PARAM_CONTROLLED_BY), field.getControlledBy());
        properties.put(getODFName(STATICFIELD_PARAM_TYPE), field.getType());
        properties.put(getODFName(STATICFIELD_PARAM_VALIDATION), field.getValidation());
        
        NodeRef ref = getServiceRegistry().getNodeService().createNode(getDataHome(), getODFName(DATA_ASSOC_STATIC_FIELDS), getODFName(field.getId()), getODFName(STATICFIELD_TYPE_NAME), properties).getChildRef();
        authBean.addReadPermission(ref, PermissionGroup.BASIC, (NodeRef)null);
        
        if(field.isAggregate()){
            properties = new HashMap<>();
            properties.put(getODFName(STATICMULTIFIELD_PARAM_COMPONENT), field.getAggregateComponent());
            properties.put(getODFName(STATICMULTIFIELD_PARAM_DESCRIBES), field.getAggregateDescribes());
            properties.put(getODFName(STATICMULTIFIELD_PARAM_HINT), field.getAggregateHint());
            properties.put(getODFName(STATICMULTIFIELD_PARAM_LAYOUT), field.getAggregateLayout());
            properties.put(getODFName(STATICMULTIFIELD_PARAM_TYPE), field.getAggregateType());
            properties.put(getODFName(STATICMULTIFIELD_PARAM_WRAPPER), field.getAggregateWrapper());
            getServiceRegistry().getNodeService().addAspect(ref, getODFName(STATICMULTIFIELD_ASPECT_NAME), properties);
        }
        
        return ref;
    }

    protected void updateField(ApplicationFieldValue field) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        if (field.wasOptionsSet()) {
            properties.put(getODFName(FIELD_PARAM_OPTIONS), field.getOptions());
        }
        if (field.wasValueSet()) {
            properties.put(getODFName(FIELD_PARAM_VALUE), field.getValue());
        }

        if (!properties.isEmpty()) {
            getServiceRegistry().getNodeService().addProperties(field.asNodeRef(), properties);
        }
    }

    protected void updateStaticField(ApplicationField field) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        if (field.wasLabelSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_LABEL), field.getLabel());
        }
        if (field.wasHintSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_HINT), field.getHint());
        }
        if (field.wasComponentSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_COMPONENT), field.getComponent());
        }
        if (field.wasLayoutSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_LAYOUT), field.getLayout());
        }
        if (field.wasWrapperSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_WRAPPER), field.getWrapper());
        }
        if (field.wasValidationSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_VALIDATION), field.getValidation());
        }
        if (field.wasControlledBy()) {
            properties.put(getODFName(STATICFIELD_PARAM_CONTROLLED_BY), field.getControlledBy());
        }
        if (field.wasTypeSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_TYPE), field.getType());
        }
        if (field.wasDescribesSet()) {
            properties.put(getODFName(STATICFIELD_PARAM_DESCRIBES), field.getDescribes());
        }

        if (!properties.isEmpty()) {
            getServiceRegistry().getNodeService().addProperties(field.asNodeRef(), properties);
        }
    }

    protected void updateBlock(ApplicationBlock block) throws Exception {
        Map<QName, Serializable> properties = new HashMap<>();
        if (block.wasLabelSet() || block.wasTitleSet()) {
            properties.put(getODFName(BLOCK_PARAM_LABEL), block.getLabel()); //Label and title should be equal
        }
        if (block.wasLayoutSet()) {
            properties.put(getODFName(BLOCK_PARAM_LAYOUT), block.getLayout());
        }
        if (block.wasCollapsibleSet()) {
            properties.put(getODFName(BLOCK_PARAM_COLLAPSIBLE), block.getCollapsible());
        }
        if (block.wasRepeatableSet()) {
            properties.put(getODFName(BLOCK_PARAM_REPEATABLE), block.getRepeatable());
        }
        if (block.wasIconSet()) {
            properties.put(getODFName(BLOCK_PARAM_ICON), block.getIcon());
        }

        if (!properties.isEmpty()) {
            getServiceRegistry().getNodeService().addProperties(block.asNodeRef(), properties);
        }
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
        NodeRef newState = null;
        NodeRef newBranchRef = app.getBranchSummary().asNodeRef();
        NodeRef newBranchWorkflow = getSingleTargetAssoc(newBranchRef, BRANCH_ASSOC_WORKFLOW);
        if (app.getState() != null) {
            newState = app.getState().asNodeRef();
            List<ChildAssociationRef> newWorkflowStates = ns.getChildAssocs(newBranchWorkflow, getODFName(WORKFLOW_ASSOC_STATES), null);
            boolean found = false;
            for (ChildAssociationRef workflowState : newWorkflowStates) {
                if (newState.equals(workflowState.getChildRef())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new AlfrescoRuntimeException(INVALID_STATE);
            }
        } else {
            newState = workflowBean.getWorkflowEntryPoint(newBranchWorkflow);
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

    public List<ApplicationSchema> getSchemas() throws Exception {
        List<ChildAssociationRef> schemasRefs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_SCHEMAS), null);
        List<ApplicationSchema> schemas = new ArrayList<>();

        for (ChildAssociationRef schemaRef : schemasRefs) {
            schemas.add(getSchema(schemaRef.getChildRef()));
        }

        return schemas;
    }

    public ApplicationSchema getSchema(NodeRef nodeRef) throws Exception {
        ensureType(APPLICATIONSCHEMA_TYPE_NAME, nodeRef);

        ApplicationSchema schema = new ApplicationSchema();
        schema.setId(getProperty(nodeRef, APPLICATIONSCHEMA_PARAM_ID, String.class));
        schema.setTitle(getProperty(nodeRef, APPLICATIONSCHEMA_PARAM_TITLE, String.class));

        List<AssociationRef> blockRefs = getServiceRegistry().getNodeService().getTargetAssocs(nodeRef, getODFName(APPLICATIONSCHEMA_ASSOCS_BLOCKS));
        List<ApplicationBlockSpecification> blocks = new ArrayList<>();
        for (AssociationRef block : blockRefs) {
            blocks.add(getBlockSpecification(block.getTargetRef()));
        }

        return schema;
    }

    public ApplicationBlockSpecification getBlockSpecification(NodeRef nodeRef) throws Exception {
        ensureType(BLOCKSPEC_TYPE_NAME, nodeRef);

        ApplicationBlockSpecification blockSpecfication = new ApplicationBlockSpecification();
        blockSpecfication.parseRef(nodeRef);

        blockSpecfication.setId(getProperty(nodeRef, BLOCK_PARAM_ID, String.class));
        blockSpecfication.setLabel(getProperty(nodeRef, BLOCK_PARAM_LABEL, String.class));
        blockSpecfication.setLayout(getProperty(nodeRef, BLOCK_PARAM_LAYOUT, String.class));
        blockSpecfication.setIcon(getProperty(nodeRef, BLOCK_PARAM_ICON, String.class));
        blockSpecfication.setCollapsible(getProperty(nodeRef, BLOCK_PARAM_COLLAPSIBLE, Boolean.class));
        blockSpecfication.setRepeatable(getProperty(nodeRef, BLOCK_PARAM_REPEATABLE, Boolean.class));

        List<AssociationRef> fieldRefs = getServiceRegistry().getNodeService().getTargetAssocs(nodeRef, getODFName(BLOCKSPEC_ASSOC_FIELDS));

        List<ApplicationField> fields = new ArrayList<>();
        for (AssociationRef field : fieldRefs) {
            fields.add(getFieldSpec(field.getTargetRef()));
        }

        blockSpecfication.setFields(fields);

        return blockSpecfication;
    }

    public ApplicationBlock getBlock(NodeRef nodeRef, StateCategory currentStateCategory) throws Exception {
        ensureType(BLOCKIMPL_TYPE_NAME, nodeRef);

        ApplicationBlock block = new ApplicationBlock();
        block.parseRef(nodeRef);

        block.setId(getProperty(nodeRef, BLOCK_PARAM_ID, String.class));
        block.setLabel(getProperty(nodeRef, BLOCK_PARAM_LABEL, String.class));
        block.setLayout(getProperty(nodeRef, BLOCK_PARAM_LAYOUT, String.class));
        block.setIcon(getProperty(nodeRef, BLOCK_PARAM_ICON, String.class));
        block.setCollapsible(getProperty(nodeRef, BLOCK_PARAM_COLLAPSIBLE, Boolean.class));
        block.setRepeatable(getProperty(nodeRef, BLOCK_PARAM_REPEATABLE, Boolean.class));

        List<ChildAssociationRef> fieldRefs = getServiceRegistry().getNodeService().getChildAssocs(nodeRef, getODFName(BLOCKIMPL_ASSOC_FIELDS), null);

        List<ApplicationFieldValue> fields = new ArrayList<>();
        for (ChildAssociationRef field : fieldRefs) {
            MultiFieldDataValue fieldData = getField(field.getChildRef());
            ApplicationFieldValue applicationField = getField(fieldData, currentStateCategory, getCurrentUserName());
            fields.add(applicationField);
        }

        block.setFields(fields);

        return block;
    }
    
    public List<MultiFieldData> getApplicationFieldSpecs() throws Exception{
        List<ChildAssociationRef> fieldRefs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_STATIC_FIELDS), null);
        List<MultiFieldData>  fields = new ArrayList<>();
        for(ChildAssociationRef fieldRef : fieldRefs){
            try{
                fields.add(getFieldSpec(fieldRef.getChildRef()));
            }catch(AccessDeniedException ex){
                //No read rights, ignore and continue
            }
        }
        return fields;
    }

    public MultiFieldData getFieldSpec(NodeRef applicationField) throws Exception {
        ensureType(STATICFIELD_TYPE_NAME, applicationField);

        MultiFieldData field = new MultiFieldData();
        field.parseRef(applicationField);
        Map<QName, Serializable> properties = getServiceRegistry().getNodeService().getProperties(applicationField);
        
        field.setId(getProperty(properties, STATICFIELD_PARAM_ID, String.class));
        field.setLabel(getProperty(properties, STATICFIELD_PARAM_LABEL, String.class));
        field.setLayout(getProperty(properties, STATICFIELD_PARAM_LAYOUT, String.class));
        field.setComponent(getProperty(properties, STATICFIELD_PARAM_COMPONENT, String.class));
        field.setHint(getProperty(properties, STATICFIELD_PARAM_HINT, String.class));
        field.setWrapper(getProperty(properties, STATICFIELD_PARAM_WRAPPER, String.class));
        field.setDescribes(getProperty(properties, STATICFIELD_PARAM_DESCRIBES, String.class));
        field.setType(getProperty(properties, STATICFIELD_PARAM_TYPE, String.class));
        field.setValidation(getProperty(properties, STATICFIELD_PARAM_VALIDATION, String.class));
        field.setControlledBy(getPropertyList(properties, STATICFIELD_PARAM_CONTROLLED_BY, String.class));

//        Set<AccessPermission> userPermissions = getServiceRegistry().getPermissionService().getPermissions(applicationField);
//        boolean readOnly = true;
//        for (AccessPermission permission : userPermissions) {
//            if (permission.getPermission().equals(PermissionService.WRITE)) {
//                readOnly = false;
//                break;
//            }
//        }
//        field.setReadOnly(readOnly);
        
        if(getServiceRegistry().getNodeService().hasAspect(applicationField, getODFName(STATICMULTIFIELD_ASPECT_NAME))){
            field.setAggregateComponent(getProperty(properties, STATICMULTIFIELD_PARAM_COMPONENT, String.class));
            field.setAggregateHint(getProperty(properties, STATICMULTIFIELD_PARAM_HINT, String.class));
            field.setAggregateLayout(getProperty(properties, STATICMULTIFIELD_PARAM_LAYOUT, String.class));
            field.setAggregateType(getProperty(properties, STATICMULTIFIELD_PARAM_TYPE, String.class));
            field.setAggregateWrapper(getProperty(properties, STATICMULTIFIELD_PARAM_WRAPPER, String.class));
            field.setAggregateDescribes(getProperty(properties, STATICMULTIFIELD_PARAM_DESCRIBES, String.class));
            field.setAggregateStateCategories(getPropertyList(properties, STATICMULTIFIELD_PARAM_STATE_CATEGORIES, String.class));
        }

        return field;
    }

    public MultiFieldDataValue getField(NodeRef applicationField) throws Exception {
        ensureType(FIELD_TYPE_NAME, applicationField);

        MultiFieldDataValue field = new MultiFieldDataValue();
        field.parseRef(applicationField);

        NodeRef staticData = getSingleTargetAssoc(applicationField, FIELD_ASSOC_STATICDATA);

        MultiFieldData staticField = getFieldSpec(staticData);

        field.setId(staticField.getId());
        field.setLabel(staticField.getLabel());
        field.setLayout(staticField.getLayout());
        field.setComponent(staticField.getComponent());
        field.setHint(staticField.getHint());
        field.setWrapper(staticField.getWrapper());
        field.setDescribes(staticField.getDescribes());
        field.setType(staticField.getType());
        field.setValidation(staticField.getValidation());
        field.setControlledBy(staticField.getControlledBy());

//        Set<AccessPermission> userPermissions = getServiceRegistry().getPermissionService().getPermissions(applicationField);
//        boolean readOnly = true;
//        for (AccessPermission permission : userPermissions) {
//            if (permission.getPermission().equals(PermissionService.WRITE)) {
//                readOnly = false;
//                break;
//            }
//        }
//        field.setReadOnly(readOnly);
        
        

        
        
        
        field.setOptions(getPropertyList(applicationField, FIELD_PARAM_OPTIONS, String.class));
        
        
        List<String> textValues;
        if(staticField.isAggregate()){       
            //This is a multi-value field
            if(!getServiceRegistry().getNodeService().hasAspect(applicationField, getODFName(MULTIFIELD_ASPECT_NAME))){
                getServiceRegistry().getNodeService().addAspect(applicationField, getODFName(MULTIFIELD_ASPECT_NAME), null);
            }
            field.setAggregateComponent(staticField.getAggregateComponent());
            field.setAggregateHint(staticField.getAggregateHint());
            field.setAggregateLayout(staticField.getAggregateLayout());
            field.setAggregateType(staticField.getAggregateType());
            field.setAggregateWrapper(staticField.getAggregateWrapper());
            field.setAggregateDescribes(staticField.getAggregateDescribes());
            
            getOrCreateUserField(applicationField); //Ensure that a field exists for the calling user
            
            HashMap<String, List<Object>> userValues = new HashMap<>();
            List<NodeRef> userFields = getAllUserFields(applicationField);
            for(NodeRef userField : userFields){
                Map<QName, Serializable> userFieldProperties = getServiceRegistry().getNodeService().getProperties(userField);
                String userName = getProperty(userFieldProperties, MULTIFIELD_VALUE_PARAM_USERNAME, String.class);
                List<String> values = getPropertyList(userFieldProperties, MULTIFIELD_VALUE_PARAM_VALUE, String.class);
                
                userValues.put(userName, getObjectValues(values, field.getTypeAsClass()));
            }
            
            field.setUserValue(userValues);
            
        }else{
            textValues = getPropertyList(applicationField, FIELD_PARAM_VALUE, String.class);
            ArrayList<Object> objectValues = getObjectValues(textValues, field.getTypeAsClass());
            field.setSingularValue(objectValues);
        }
        
        return field;
    }
    
    protected ArrayList<Object> getObjectValues(List<String> jsonObjects, Class listContainsType) throws IOException, ParseException{
        ArrayList<Object> objectValues = new ArrayList<>();
        ObjectMapper mapper = Utilities.getMapper();
        if (jsonObjects != null) {
            for (String textValue : jsonObjects) {
                if (textValue != null && !textValue.isEmpty()) {
                    if (String.class.isAssignableFrom(listContainsType)) {
                        objectValues.add(textValue);
                    } else if(Date.class.isAssignableFrom(listContainsType)){
                        objectValues.add(Utilities.UNIVERAL_DATE_FORMAT.parse(textValue));
                    } else {
                        objectValues.add(mapper.readValue(textValue, listContainsType));
                    }
                }

            }
        }
        return objectValues;
    }
    
    public NodeRef getOrCreateUserField(NodeRef applicationField) throws Exception {
        String username = getCurrentUserName();
        NodeRef userField = getMultiField(applicationField, username);
        if (userField == null) {
            return createMultiField(applicationField, username);
        } else {
            return userField;
        }
    }
    
    public NodeRef getMultiField(NodeRef applicationField, String username) throws Exception {
        return getServiceRegistry().getRetryingTransactionHelper().doInTransaction(() -> {
            List<ChildAssociationRef> fields = getServiceRegistry().getNodeService().getChildAssocs(applicationField, getODFName(MULTIFIELD_ASSOC_VALUES), getODFName(username));
            if (fields.size() > 1) {
                throw new RuntimeException("Field inconsistency detected. Multiple fields for user " + username);
            } else if (fields.size() < 1) {
                return null;
            } else {
                return fields.get(0).getChildRef();
            }
        });
    }
    
    public NodeRef createMultiField(NodeRef applicationField, String userName) throws Exception {
        return getServiceRegistry().getRetryingTransactionHelper().doInTransaction(() -> AuthenticationUtil.runAsSystem(() -> {    
            List<ChildAssociationRef> fields = getServiceRegistry().getNodeService().getChildAssocs(applicationField, getODFName(MULTIFIELD_ASSOC_VALUES), getODFName(userName));
            if (fields.size() > 0) {
                throw new RuntimeException("Cannot create field. The field already exists for user: " + userName);
            }else {
                try {
                    Map<QName, Serializable> properties = new HashMap<>();
                    properties.put(getODFName(MULTIFIELD_VALUE_PARAM_USERNAME), userName);
                    NodeRef newField = getServiceRegistry().getNodeService().createNode(applicationField, getODFName(MULTIFIELD_ASSOC_VALUES), getODFName(userName), getODFName(MULTIFIELD_VALUE_TYPE_NAME), properties).getChildRef();
                    authBean.addUserPermission(userName, newField);
                    //authBean.disableInheritPermissions(newField);
                    return newField;
                } catch (Exception ex) {
                    throw new AlfrescoRuntimeException(ERROR_CREATING_FIELD, ex);
                }
            }
        }));
    }
    
    public List<NodeRef> getAllUserFields(NodeRef applicationField) throws Exception{
        if(!getServiceRegistry().getNodeService().hasAspect(applicationField, getODFName(MULTIFIELD_ASPECT_NAME))){
            throw new RuntimeException("Method is only usable for userFields");
        }
        List<NodeRef> userFields = new ArrayList<>();
        List<ChildAssociationRef> fields = getServiceRegistry().getNodeService().getChildAssocs(applicationField, getODFName(MULTIFIELD_ASSOC_VALUES), null);
        for(ChildAssociationRef field : fields){
            userFields.add(field.getChildRef());
        }
        return userFields;
        
    }
    
    public ApplicationFieldValue getField(MultiFieldDataValue fieldData, StateCategory currentApplicationStateCategory, String username){
        if(fieldData.getAggregatorAsClass() == null){
            return getBasicField(fieldData);
        }else{
            if(fieldData.getAggregateStateCategories() == null || fieldData.getAggregateStateCategories().isEmpty()){
                if(currentApplicationStateCategory == null){
                    //No aggregate category was set, and the current state has no category, so aggregate
                    return getAggregateField(fieldData);
                }else{
                    return getUserField(fieldData, username);
                }
                
            }else{
                if(fieldData.getAggregateStateCategories().contains(currentApplicationStateCategory.getCategoryName())){
                    return getAggregateField(fieldData);
                }else{
                    return getUserField(fieldData, username);
                }
            }
        }
    }
    
    public ApplicationField getField(MultiFieldData fieldData, StateCategory currentApplicationStateCategory, String username){
        if(fieldData.getAggregatorAsClass() == null){
            return getBasicField(fieldData);
        }else{
            if(fieldData.getAggregateStateCategories() == null || fieldData.getAggregateStateCategories().isEmpty()){
                if(currentApplicationStateCategory == null){
                    //No aggregate category was set, and the current state has no category, so aggregate
                    return getAggregateField(fieldData);
                }else{
                    return getUserField(fieldData, username);
                }
                
            }else{
                if(fieldData.getAggregateStateCategories().contains(currentApplicationStateCategory.getCategoryName())){
                    return getAggregateField(fieldData);
                }else{
                    return getUserField(fieldData, username);
                }
            }
        }
    }
    
    protected ApplicationFieldValue getBasicField(MultiFieldDataValue fieldData) {

        ApplicationFieldValue fieldValue = new ApplicationFieldValue();
        MultiFieldDataValue fieldDataValue = (MultiFieldDataValue) fieldData;

        fieldValue.setValue(fieldDataValue.getSingularValue());
        fieldValue.setOptions(fieldDataValue.getOptions());

        populateUserField(fieldValue, fieldData);

        return fieldValue;
    }
    
    protected ApplicationField getBasicField(MultiFieldData fieldData){
        ApplicationField field = new ApplicationField();
        populateUserField(field, fieldData);
        return field;
    }
    
    protected ApplicationFieldValue getUserField(MultiFieldDataValue fieldData, String userName) {

        ApplicationFieldValue fieldValue = new ApplicationFieldValue();
        MultiFieldDataValue<?, ?> fieldDataValue = (MultiFieldDataValue<?, ?>) fieldData;

        ArrayList<?> userValue = fieldDataValue.getUserValue().get(userName);

        fieldValue.setValue(userValue);
        fieldValue.setOptions(fieldDataValue.getOptions());

        populateUserField(fieldValue, fieldData);

        return fieldValue;
    }
    
    protected ApplicationField getUserField(MultiFieldData fieldData, String userName){
        ApplicationField field = new ApplicationField();

        populateUserField(field, fieldData);        
        
        return field;
    }
    
    protected void populateUserField(ApplicationField field, MultiFieldData fieldData){
        field.setId(fieldData.getId());
        field.setLabel(fieldData.getLabel());
        field.setType(fieldData.getType());
        field.setComponent(fieldData.getComponent());
        field.setDescribes(fieldData.getDescribes());
        field.setLayout(fieldData.getLayout());
        field.setHint(fieldData.getHint());
        field.setWrapper(fieldData.getWrapper());
        field.setValidation(fieldData.getValidation());
        field.setControlledBy(fieldData.getControlledBy());
    }
    
    protected ApplicationFieldValue getAggregateField(MultiFieldDataValue fieldData) {
        ApplicationFieldValue fieldValue = new ApplicationFieldValue();
        MultiFieldDataValue fieldDataValue = (MultiFieldDataValue) fieldData;

        fieldValue.setValue(fieldDataValue.getSingularValue());
        fieldValue.setOptions(fieldDataValue.getOptions());

        populateAggregateField(fieldValue, fieldData);

        return fieldValue;
    }

    protected ApplicationField getAggregateField(MultiFieldData fieldData){
        ApplicationField field = new ApplicationField();
        populateAggregateField(field, fieldData);
        
        return field;
    }
    
    protected void populateAggregateField(ApplicationField field, MultiFieldData fieldData){
        field.setId(fieldData.getId());
        field.setLabel(fieldData.getLabel());
        field.setType(fieldData.getAggregateType());
        field.setComponent(fieldData.getAggregateComponent());
        field.setDescribes(fieldData.getAggregateDescribes());
        field.setLayout(fieldData.getAggregateLayout());
        field.setHint(fieldData.getAggregateHint());
        field.setWrapper(fieldData.getAggregateWrapper());
        field.setValidation(fieldData.getValidation());
        field.setControlledBy(fieldData.getControlledBy());
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
    
//    public ApplicationField getField(MultiFieldDataValue fieldData, StateCategory currentApplicationStateCategory, String username){
//        if(fieldData.getAggregatorAsClass() == null){
//            return getBasicField(fieldData);
//        }else{
//            if(fieldData.getAggregateStateCategories() == null || fieldData.getAggregateStateCategories().isEmpty()){
//                if(currentApplicationStateCategory == null){
//                    //No aggregate category was set, and the current state has no category, so aggregate
//                    return getAggregateField(fieldData);
//                }else{
//                    return getUserField(fieldData, username);
//                }
//                
//            }else{
//                if(fieldData.getAggregateStateCategories().contains(currentApplicationStateCategory.getCategoryName())){
//                    return getAggregateField(fieldData);
//                }else{
//                    return getUserField(fieldData, username);
//                }
//            }
//        }
//    }

    public List<ApplicationSummary> getApplicationSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (ChildAssociationRef applicationRef : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_APPLICATIONS), null)) {
            try {
                toReturn.add(getApplicationSummary(applicationRef.getChildRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return toReturn;
    }

    public List<ApplicationSummary> getDeletedApplicationSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (ChildAssociationRef applicationRef : ns.getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_DELETED_APPLICATION), null)) {
            try {
                toReturn.add(getApplicationSummary(applicationRef.getChildRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return toReturn;
    }

    public List<ApplicationSummary> getNewApplicationSummaries() throws Exception {
        NodeService ns = getServiceRegistry().getNodeService();
        List<ApplicationSummary> toReturn = new ArrayList<>();
        for (AssociationRef applicationRef : ns.getTargetAssocs(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS))) {
            try {
                toReturn.add(getApplicationSummary(applicationRef.getTargetRef()));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        return toReturn;
    }

    public ApplicationSummary getApplicationSummary(NodeRef applicationSummary) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationSummary);

        ApplicationSummary app = new ApplicationSummary();
        app.parseRef(applicationSummary);

        try {
            NodeRef branchRef = getSingleTargetAssoc(applicationSummary, APPLICATION_ASSOC_BRANCH);
            if (branchRef != null) {
                BranchSummary branchSummary = branchBean.getBranchSummary(branchRef);
                app.setBranchSummary(branchSummary);
            }
        } catch (AccessDeniedException ex) {
            //Skip the node and continue
        }

        app.setId(getProperty(applicationSummary, APPLICATION_PARAM_ID, String.class));
        app.setTitle(getProperty(applicationSummary, APPLICATION_PARAM_TITLE, String.class));
        app.setIsSeen(isApplicationSeen(applicationSummary, getCurrentUserName()));

        List<ChildAssociationRef> blockRefs = getServiceRegistry().getNodeService().getChildAssocs(applicationSummary, getODFName(APPLICATION_ASSOC_BLOCKS), null);
        List<ApplicationBlock> blocks = new ArrayList<>();

        for (ChildAssociationRef blockRef : blockRefs) {
            blocks.add(getBlock(blockRef.getChildRef()));
        }

        app.setBlocks(blocks);

        return app;
    }

    public Application getApplication(NodeRef applicationRef) throws Exception {
        ensureType(getODFName(APPLICATION_TYPE_NAME), applicationRef);

        Application application = new Application();
        application.parseRef(applicationRef);
        application.setTitle(getProperty(applicationRef, APPLICATION_PARAM_TITLE, String.class));
        application.setIsSeen(isApplicationSeen(applicationRef, getCurrentUserName()));

        List<ChildAssociationRef> blockRefs = getServiceRegistry().getNodeService().getChildAssocs(applicationRef, getODFName(APPLICATION_ASSOC_BLOCKS), null);
        List<ApplicationBlock> blocks = new ArrayList<>();

        for (ChildAssociationRef blockRef : blockRefs) {
            blocks.add(getBlock(blockRef.getChildRef()));
        }

        application.setBlocks(blocks);

        NodeRef branchRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BRANCH);
        NodeRef budgetRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_BUDGET);
        NodeRef stateRef = getSingleTargetAssoc(applicationRef, APPLICATION_ASSOC_STATE);

        if (branchRef != null) {
            try {
                application.setBranchSummary(branchBean.getBranchSummary(branchRef));
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        if (budgetRef != null) {
            try {
                BudgetReference budget = budgetBean.getBudgetReference(budgetRef);
                application.setBudget(budget);
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
            }
        }
        if (stateRef != null) {
            try {
                StateReference state = workflowBean.getStateReference(stateRef);
                application.setState(state);
                NodeRef workflowRef = getSingleParentAssoc(stateRef, WORKFLOW_ASSOC_STATES);
                if (workflowRef != null) {
                    WorkflowReference workflow = workflowBean.getWorkflowReference(workflowRef);
                    application.setWorkflow(workflow);
                }
            } catch (AccessDeniedException ex) {
                //Skip the node and continue
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

    public Map<String, ApplicationFieldValue> getApplicationFields(Application application) {
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

    public <E extends Reference> void resolveChangedAssociation(E oldType, E newType, String associationName, List<ApplicationChangeUnit> changes) {
        if (!Objects.equals(oldType, newType)) {
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
            Date date1 = UNIVERAL_DATE_FORMAT.parse((String) o1.getTimesStamp(), new ParsePosition(0));
            Date date2 = UNIVERAL_DATE_FORMAT.parse((String) o2.getTimesStamp(), new ParsePosition(0));
            return date1.compareTo(date2);
        });

        return changes;
    }

    public ApplicationChange getVersionDifference(Version oldVersion, Version newVersion) throws Exception {
        if (newVersion == null) {
            throw new Exception("newVersion must not be null");
        }

        String changeType = (String) newVersion.getVersionProperty(APPLICATION_CHANGE);

        Date timeStamp = (Date)newVersion.getVersionProperty("modified");
//newVersion.getFrozenModifiedDate();
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
