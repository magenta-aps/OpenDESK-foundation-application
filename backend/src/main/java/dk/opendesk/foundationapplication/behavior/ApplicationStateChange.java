/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.behavior;

import static dk.opendesk.foundationapplication.Utilities.*;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 *
 * @author martin
 */
public class ApplicationStateChange implements NodeServicePolicies.OnCreateAssociationPolicy{
    private PolicyComponent eventManager;
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.eventManager = policyComponent;
    }
    
    public void registerEventHandlers() throws Exception {
        eventManager.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
                getODFName(APPLICATION_TYPE_NAME),
                getODFName(APPLICATION_ASSOC_STATE),
                new JavaBehaviour(this, "onCreateAssociation",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        try {
            NodeService ns = serviceRegistry.getNodeService();
            System.out.println(ns.getType(nodeAssocRef.getSourceRef())+" "+ns.getType(nodeAssocRef.getTargetRef()));
            System.out.println(ns.getProperty(nodeAssocRef.getSourceRef(), getODFName(APPLICATION_PARAM_TITLE))+" "+ns.getProperty(nodeAssocRef.getTargetRef(), getODFName(STATE_PARAM_TITLE)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    
    
}
