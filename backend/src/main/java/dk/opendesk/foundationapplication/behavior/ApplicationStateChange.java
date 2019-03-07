/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.behavior;

import static dk.opendesk.foundationapplication.Utilities.*;

import java.util.List;
import java.util.Set;

import dk.opendesk.foundationapplication.Utilities;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author martin
 */
public class ApplicationStateChange implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.BeforeDeleteAssociationPolicy{

    private static final String EXCEPTION_MESSAGE_ON_CREATION = "OnAssociationCreation-actions not properly executed";
    private static final String EXCEPTION_MESSAGE_BEFORE_DELETE = "BeforeAssociationDeletion-actions not properly executed";

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
                        Behaviour.NotificationFrequency.EVERY_EVENT));
        eventManager.bindAssociationBehaviour(
                NodeServicePolicies.BeforeDeleteAssociationPolicy.QNAME,
                getODFName(APPLICATION_TYPE_NAME),
                getODFName(APPLICATION_ASSOC_STATE),
                new JavaBehaviour(this, "beforeDeleteAssociation",
                        Behaviour.NotificationFrequency.EVERY_EVENT));


    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        try {

            List<Action> stateActions = serviceRegistry.getActionService().getActions(nodeAssocRef.getTargetRef());

            for (Action action : stateActions) {
                Set<QName> actionAspects = serviceRegistry.getNodeService().getAspects(action.getNodeRef());
                if (actionAspects.contains(Utilities.getODFName(ASPECT_ON_CREATE))) {
                    serviceRegistry.getActionService().executeAction(action, nodeAssocRef.getSourceRef());
                }
            }

        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_MESSAGE_ON_CREATION, e);
        }

    }


    @Override
    public void beforeDeleteAssociation(AssociationRef nodeAssocRef) {
        try {
            List<Action> stateActions = serviceRegistry.getActionService().getActions(nodeAssocRef.getTargetRef());

            for (Action action : stateActions) {
                Set<QName> actionAspects = serviceRegistry.getNodeService().getAspects(action.getNodeRef());
                if (actionAspects.contains(Utilities.getODFName(ASPECT_BEFORE_DELETE))) {
                    serviceRegistry.getActionService().executeAction(action, nodeAssocRef.getSourceRef());
                }
            }
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(EXCEPTION_MESSAGE_BEFORE_DELETE, e);
        }

    }
}
