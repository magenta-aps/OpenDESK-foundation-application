package dk.opendesk.foundationapplication.behavior;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.concurrent.TimeUnit;

import static dk.opendesk.foundationapplication.Utilities.APPLICATION_FOLDER_DOCUMENT_TEMP;
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static dk.opendesk.foundationapplication.Utilities.getODFName;

public class ValidateUploadedDocument implements NodeServicePolicies.OnCreateChildAssociationPolicy {

    private static final String EXCEPTION_MESSAGE_CACHE_FOLDER = "validateUploadedDocument.cache.exception";

    private PolicyComponent eventManager;
    private ServiceRegistry serviceRegistry;
    private static Cache<NodeRef, Boolean> folderCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(10, TimeUnit.MINUTES).build();

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.eventManager = policyComponent;
    }

    public void registerEventHandlers() throws Exception {
        eventManager.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                getCMName("folder"),
                getCMName("contains"),
                new JavaBehaviour(this, "onCreateChildAssociation",
                        Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNewNode) {
        System.out.println("onCreateAssociation called");

        NodeRef parentRef = childAssociationRef.getParentRef();
        NodeRef childRef = childAssociationRef.getChildRef();

        Boolean isTempFolder = folderCache.getIfPresent(parentRef);

        if (isTempFolder == null) {
            try {
                QName tempDocFolderName = getODFName(APPLICATION_FOLDER_DOCUMENT_TEMP);
                QName parentName = QName.createQName(serviceRegistry.getNodeService().getPath(parentRef).last().getElementString());
                isTempFolder = tempDocFolderName.equals(parentName);
                folderCache.put(parentRef, isTempFolder);
            } catch (Exception e) {
                throw new AlfrescoRuntimeException(EXCEPTION_MESSAGE_CACHE_FOLDER, e);
            }
        }

        if (!isTempFolder) {
            return;
        }

        //new document added to tempDocumentFolder

    }

}
