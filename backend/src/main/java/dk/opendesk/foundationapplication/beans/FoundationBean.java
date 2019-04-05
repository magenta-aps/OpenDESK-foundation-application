/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.Utilities;
import static dk.opendesk.foundationapplication.Utilities.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class FoundationBean {

    private final Logger logger = Logger.getLogger(getClass());

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

    public Logger getLogger() {
        return logger;
    }
    
    

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
    
    public NodeRef getDataHome() {
        return Utilities.getDataNode(serviceRegistry);
    }

    public String getCurrentUserName() {
        return serviceRegistry.getAuthenticationService().getCurrentUserName();
    }

    public <T> T getProperty(NodeRef ref, String name, Class<T> type) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        return type.cast(ns.getProperty(ref, getODFName(name)));
    }

    public <T> List<T> getPropertyList(NodeRef ref, String name, Class<T> type) throws Exception {
        NodeService ns = serviceRegistry.getNodeService();
        return (List<T>) ns.getProperty(ref, getODFName(name));
    }
    
    public <T> T getProperty(NodeRef ref, String name, Class<T> type, Map<QName, Serializable> properties) throws Exception {
        return type.cast(properties.get(getODFName(name)));
        
    }

    public <T> List<T> getPropertyList(NodeRef ref, String name, Class<T> type, Map<QName, Serializable> properties) throws Exception {
        return (List<T>) properties.get(getODFName(name));
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

    public void ensureType(String expectedType, NodeRef ref) throws Exception {
        ensureType(getODFName(expectedType), ref);
    }
    
    public void ensureType(QName expectedType, NodeRef ref) {
        QName actualType = serviceRegistry.getNodeService().getType(ref);
        if (!expectedType.equals(actualType)) {
            throw new AlfrescoRuntimeException(ID_BAD_NODE_TYPE, new Object[]{expectedType, actualType});
        }
    }
    
    public NodeRef getOrCreateFolder(NodeRef applicationRef, String folderName) throws Exception {
        NodeRef folder = serviceRegistry.getNodeService().getChildByName(applicationRef, getODFName(folderName), "cm:" + folderName);
        if (folder == null) {
            ChildAssociationRef childRef = serviceRegistry.getNodeService().createNode(applicationRef, getODFName(folderName), getCMName(folderName), getCMName("Folder"));
            folder = childRef.getChildRef();
        }

        return folder;
    }







   
}
