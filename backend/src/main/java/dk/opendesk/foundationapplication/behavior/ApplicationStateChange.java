/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.behavior;

import static dk.opendesk.foundationapplication.Utilities.*;
import java.util.List;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptionPair;

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
            NodeService ns = serviceRegistry.getNodeService();
            System.out.println("create: " + ns.getType(nodeAssocRef.getSourceRef())+" "+ns.getType(nodeAssocRef.getTargetRef()));
            System.out.println("create: " + ns.getProperty(nodeAssocRef.getSourceRef(), getODFName(APPLICATION_PARAM_TITLE))+" "+ns.getProperty(nodeAssocRef.getTargetRef(), getODFName(STATE_PARAM_TITLE)));
            NodeRef stateRef = ns.getTargetAssocs(nodeAssocRef.getSourceRef(),getODFName(APPLICATION_ASSOC_STATE)).get(0).getTargetRef();
            System.out.println("create: " + ns.getProperty(stateRef, getODFName(STATE_PARAM_TITLE)));
        } catch (Exception ex) {

            ex.printStackTrace();
        }
        
    }


    @Override
    public void beforeDeleteAssociation(AssociationRef nodeAssocRef) {
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

    }
}
