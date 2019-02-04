/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.behavior;

import static dk.opendesk.foundationapplication.Utilities.*;

import java.awt.*;
import java.util.List;
import java.util.Set;

import dk.opendesk.foundationapplication.Utilities;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptionPair;
import org.alfresco.service.namespace.QName;

import javax.rmi.CORBA.Util;

/**
 *
 * @author martin
 */
public class ApplicationStateChange implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.BeforeDeleteAssociationPolicy{
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
            //source = application
            //target = state

            List<Action> stateActions = serviceRegistry.getActionService().getActions(nodeAssocRef.getTargetRef());

            for (Action action : stateActions) {
                Set<QName> actionAspects = serviceRegistry.getNodeService().getAspects(action.getNodeRef());
                if (actionAspects.contains(Utilities.getODFName(ASPECT_ON_CREATE))) {
                    serviceRegistry.getActionService().executeAction(action, nodeAssocRef.getSourceRef());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*
            NodeService ns = serviceRegistry.getNodeService();
            System.out.println("SourceRef (application): " + nodeAssocRef.getSourceRef());
            System.out.println("TargetRef (state): " + nodeAssocRef.getTargetRef());
            System.out.println();

            System.out.println(ns.getType(nodeAssocRef.getTargetRef()).equals(Utilities.getODFName(STATE_TYPE_NAME)));
            System.out.println(ns.getType(nodeAssocRef.getSourceRef()).equals(Utilities.getODFName(APPLICATION_TYPE_NAME)));


            System.out.println("create1: " + ns.getType(nodeAssocRef.getSourceRef())+" "+ns.getType(nodeAssocRef.getTargetRef()));
            System.out.println("create2: " + ns.getProperty(nodeAssocRef.getSourceRef(), getODFName(APPLICATION_PARAM_TITLE))+" "+ns.getProperty(nodeAssocRef.getTargetRef(), getODFName(STATE_PARAM_TITLE)));
            //NodeRef stateRef = ns.getTargetAssocs(nodeAssocRef.getSourceRef(),getODFName(APPLICATION_ASSOC_STATE)).get(0).getTargetRef();
            //System.out.println("--> " + stateRef);
            AssociationRef test = ns.getTargetAssocs(nodeAssocRef.getSourceRef(),getODFName(APPLICATION_ASSOC_STATE)).get(0);
            //System.out.println("create3: " + ns.getProperty(stateRef, getODFName(STATE_PARAM_TITLE)));
        */
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
            e.printStackTrace();
        }

        /*

        NodeService ns = serviceRegistry.getNodeService();
        NodeRef stateRef = null;
        try {
		if(ns.exists(nodeAssocRef.getSourceRef())){
			System.out.println("delete 1: " + ns.getType(nodeAssocRef.getSourceRef())+ " " + ns.getProperty(nodeAssocRef.getSourceRef(), getODFName(APPLICATION_PARAM_TITLE)));
			List<AssociationRef> stateRefs = ns.getTargetAssocs(nodeAssocRef.getSourceRef(),getODFName(APPLICATION_ASSOC_STATE));
			if(!stateRefs.isEmpty()){
				stateRef = stateRefs.get(0).getTargetRef();
					if(ns.exists(stateRef)){
						System.out.println("delete 2: " + ns.getProperty(stateRef, getODFName(STATE_PARAM_TITLE)));
					}
			}
			
			
		}
		if(ns.exists(nodeAssocRef.getTargetRef())){
			System.out.println("delete 3: " + ns.getType(nodeAssocRef.getTargetRef())+ " " + ns.getProperty(nodeAssocRef.getTargetRef(), getODFName(STATE_PARAM_TITLE)));
		}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
