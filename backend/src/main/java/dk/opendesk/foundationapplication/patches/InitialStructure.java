/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.patches;

import java.util.List;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import static dk.opendesk.foundationapplication.Utilities.*;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class InitialStructure extends AbstractPatch {
    private static Logger LOGGER = Logger.getLogger(InitialStructure.class);
    
    public static final String DICTIONARY_PATH = "/app:company_home/app:dictionary";
    public static final String DATA_NAME = "foundationData";
    public static final String WORKFLOW_NAME = "defaultWorkFlow";
    
    public static final String FOUNDATION_TAG = "odf";
    public static final String DATA_PATH = DICTIONARY_PATH+"/"+FOUNDATION_TAG+":"+DATA_NAME;
    
    public static final String STATE_RECIEVED_NAME = "recieved";
    public static final String STATE_ASSESS_NAME = "assesment";
    public static final String STATE_DENIED_NAME = "denied";
    public static final String STATE_ACCEPTED_NAME = "accepted";
    

    //QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
    
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    

    @Override
    protected void checkProperties() {
        checkPropertyNotNull(serviceRegistry, "serviceRegistry");
        super.checkProperties();
    }

    @Override
    protected String applyInternal() throws Exception {
        String foundationNamespace = getFoundationModelNameSpace();
        
        QName dataTypeQname = QName.createQName(foundationNamespace, DATA_TYPE_NAME);
        QName dataQname = QName.createQName(foundationNamespace, DATA_NAME);
//        QName dataWorkflowsQname = QName.createQName(foundationNamespace, DATA_ASSOC_WORKFLOW);
//        QName dataBudgetsQname = QName.createQName(foundationNamespace, DATA_ASSOC_BUDGETS);
//        QName dataBranchesQname = QName.createQName(foundationNamespace, DATA_ASSOC_BRANCHES);
//        
//        QName workFlowTypeQname = QName.createQName(foundationNamespace, BRANCH_TYPE_NAME);
//        QName workFlowQname = QName.createQName(foundationNamespace, WORKFLOW_NAME);
//        QName workFlowStatesQname = QName.createQName(foundationNamespace, WORKFLOW_ASSOC_STATES);
//        
//        QName workFlowTypeQname = QName.createQName(foundationNamespace, WORKFLOW_TYPE_NAME);
//        QName workFlowQname = QName.createQName(foundationNamespace, WORKFLOW_NAME);
//        QName workFlowStatesQname = QName.createQName(foundationNamespace, WORKFLOW_ASSOC_STATES);
//        
//        QName stateTypeQname = QName.createQName(foundationNamespace, STATE_TYPE_NAME);
//        QName stateRecievedQname = QName.createQName(foundationNamespace, STATE_RECIEVED_NAME);
//        QName stateAssessQname = QName.createQName(foundationNamespace, STATE_ASSESS_NAME);
//        QName stateDeniedQname = QName.createQName(foundationNamespace, STATE_DENIED_NAME);
//        QName stateAcceptedQname = QName.createQName(foundationNamespace, STATE_ACCEPTED_NAME);
//        QName stateTransitionsQname = QName.createQName(foundationNamespace, STATE_ASSOC_TRANSITIONS);
        
        
        
        
        
        NodeRef dictionaryRef = getDataDictionaryRef();
        NodeRef dataRef = serviceRegistry.getNodeService().createNode(dictionaryRef, ContentModel.ASSOC_CONTAINS, dataQname, dataTypeQname).getChildRef();    
        serviceRegistry.getPermissionService().setInheritParentPermissions(dataRef, false);
        
        
        
//        NodeRef workFlowRef = serviceRegistry.getNodeService().createNode(dataRef, dataWorkflowsQname, workFlowQname, workFlowTypeQname).getChildRef();
//        
//        NodeRef stateRecievedRef = serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateRecievedQname, stateTypeQname).getChildRef();
//        NodeRef stateAccessRef = serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateAssessQname, stateTypeQname).getChildRef();
//        NodeRef stateDeniedRef = serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateDeniedQname, stateTypeQname).getChildRef();
//        NodeRef stateAcceptedRef = serviceRegistry.getNodeService().createNode(workFlowRef, workFlowStatesQname, stateAcceptedQname, stateTypeQname).getChildRef();
//        
//        serviceRegistry.getNodeService().createAssociation(stateRecievedRef, stateAccessRef, stateTransitionsQname);
//        serviceRegistry.getNodeService().createAssociation(stateRecievedRef, stateDeniedRef, stateTransitionsQname);
//        
//        serviceRegistry.getNodeService().createAssociation(stateAccessRef, stateAcceptedRef, stateTransitionsQname);
//        serviceRegistry.getNodeService().createAssociation(stateAccessRef, stateDeniedRef, stateTransitionsQname);
        

        return "Patch applied";
    }
    
    protected NodeRef getDataDictionaryRef(){
        StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        NodeRef rootRef = serviceRegistry.getNodeService().getRootNode(store);

        List<NodeRef> refs = serviceRegistry.getSearchService().selectNodes(rootRef, DICTIONARY_PATH, null, serviceRegistry.getNamespaceService(), false);
        if (refs.size() != 1) {
            throw new AlfrescoRuntimeException("Failed to apply patch: Returned multiple refs for " + DICTIONARY_PATH);
        }

        return refs.get(0);
    }
}
