package dk.opendesk.foundationapplication.behavior;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import javax.xml.soap.Node;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static dk.opendesk.foundationapplication.Utilities.APPLICATION_FOLDER_DOCUMENT_TEMP;
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static dk.opendesk.foundationapplication.Utilities.getODFName;

public class ValidateUploadedDocument implements NodeServicePolicies.OnCreateChildAssociationPolicy {

    private static final String EXCEPTION_CACHE_FOLDER = "validateUploadedDocument.cache.exception";
    private static final String EXCEPTION_READ_FILE = "validateUploadedDocument.read.file.exception";
    private static final String EXCEPTION_DOCUMENT_FOLDER = "validateUploadedDocument.parentfolder.not.documentfolder.exception";

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
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNewNode) {
        System.out.print("\nonCreateAssociation called");

        NodeService nodeService = serviceRegistry.getNodeService();
        ContentService contentService = serviceRegistry.getContentService();

        NodeRef parentRef = childAssociationRef.getParentRef();
        NodeRef childRef = childAssociationRef.getChildRef();

        Boolean isTempFolder = folderCache.getIfPresent(parentRef);

        if (isTempFolder == null) {
            try {
                QName tempDocFolderName = getODFName(APPLICATION_FOLDER_DOCUMENT_TEMP);
                QName parentName = QName.createQName(nodeService.getPath(parentRef).last().getElementString());
                isTempFolder = tempDocFolderName.equals(parentName);
                folderCache.put(parentRef, isTempFolder);
            } catch (Exception e) {
                throw new AlfrescoRuntimeException(EXCEPTION_CACHE_FOLDER, e);
            }
        }

        if (!isTempFolder) {
            System.out.println(" - not tempDocumentFolder, returning.");
            return;
        }

        System.out.println(" - is tempDocumentFolder, processing file");

        ContentReader reader = contentService.getReader(childRef, ContentModel.PROP_CONTENT);
        String mimeType = reader.getMimetype();
        String fileName = reader.getContentData().getContentUrl();
        System.out.println("fileName : " + fileName);
        System.out.println(reader.getContentString());
        nodeService.getPath(childRef).last();

        /*
        InputStream originalInputStream = reader.getContentInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final int BUF_SIZE = 1 << 8; //1KiB buffer
        byte[] buffer = new byte[BUF_SIZE];
        int bytesRead = -1;
        try {
        while((bytesRead = originalInputStream.read(buffer)) > -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        originalInputStream.close();
        } catch (IOException e) {
            // todo slet filen
            throw new AlfrescoRuntimeException(EXCEPTION_READ_FILE, e);
        }
        byte[] binaryData = outputStream.toByteArray();


        //moving file to application document folder
        NodeRef docFolder = nodeService.getPrimaryParent(parentRef).getParentRef();

        if (!QName.createQName(nodeService.getPath(docFolder).last().getElementString()).getLocalName().equals("documentFolder")) {
            throw new AlfrescoRuntimeException(EXCEPTION_DOCUMENT_FOLDER);
        }

        //nodeService.
*/




        //System.out.println(nodeService.getProperty(childRef, ContentModel.PROP_CONTENT).toString());

    }

}
