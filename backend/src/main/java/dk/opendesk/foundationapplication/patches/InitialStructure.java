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
        
        QName dataTypeQname = getODFName(DATA_TYPE_NAME);
        QName dataQname = getODFName(DATA_NAME);
        
        NodeRef dictionaryRef = getDataDictionaryRef();
        NodeRef dataRef = serviceRegistry.getNodeService().createNode(dictionaryRef, ContentModel.ASSOC_CONTAINS, dataQname, dataTypeQname).getChildRef();    
        serviceRegistry.getPermissionService().setInheritParentPermissions(dataRef, false);
        
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