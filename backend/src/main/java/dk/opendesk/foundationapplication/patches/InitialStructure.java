/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.patches;

import java.util.List;

import dk.opendesk.foundationapplication.Utilities;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import static dk.opendesk.foundationapplication.Utilities.*;
import dk.opendesk.foundationapplication.beans.AuthorityBean;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import java.util.Collections;
import org.alfresco.service.cmr.security.PermissionService;
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
    public static final String MAIL_TEMPLATE_PATH = DICTIONARY_PATH+"/cm:extensionwebscripts/cm:OpenDesk/cm:Templates/cm:Emails";
    public static final String MAIL_TEMPLATE_FOLDER_NAME = "foundationEmailTemplateFolder";
    
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
        
        AuthorityBean authBean = new AuthorityBean();
        authBean.setServiceRegistry(serviceRegistry);
        
        //serviceRegistry.getPermissionService().setPermission(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.READ, true);
        NodeRef dataRef = serviceRegistry.getNodeService().createNode(dictionaryRef, ContentModel.ASSOC_CONTAINS, dataQname, dataTypeQname, Collections.singletonMap(getODFName(DATA_PARAM_LASTID), 0)).getChildRef();
        authBean.addFullPermission(dataRef, PermissionGroup.BASIC);
//        serviceRegistry.getPermissionService().setPermission(dataRef, AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.WRITE, true);    
//        serviceRegistry.getPermissionService().setPermission(dataRef, AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.READ_ASSOCIATIONS, true); 
//        serviceRegistry.getPermissionService().setPermission(dataRef, AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.READ_CHILDREN, true); 
//        serviceRegistry.getPermissionService().setPermission(dataRef, AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.READ_PERMISSIONS, true); 
//        serviceRegistry.getPermissionService().setPermission(dataRef, AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.READ_PROPERTIES, true); 
//        serviceRegistry.getPermissionService().setPermission(dataRef, AuthorityBean.getOrCreateGroup(PermissionGroup.BASIC, null, true, serviceRegistry.getAuthorityService()), PermissionService.READ, true);
        serviceRegistry.getPermissionService().setInheritParentPermissions(dataRef, false);

        NodeRef emailTemplateFolder = serviceRegistry.getNodeService().createNode(Utilities.getEmailTemplateDir(serviceRegistry), ContentModel.ASSOC_CONTAINS, getCMName(MAIL_TEMPLATE_FOLDER_NAME), ContentModel.TYPE_FOLDER).getChildRef();

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