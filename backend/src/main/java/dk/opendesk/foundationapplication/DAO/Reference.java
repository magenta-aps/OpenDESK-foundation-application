/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class Reference {
    public static final String DEFAULT_STORE = "workspace://SpacesStore";
    
    private String nodeID;
    private String storeID = null;

    public Reference() {
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getNodeRef() {
        String actualStoreID = (storeID != null ? storeID : DEFAULT_STORE);
        return actualStoreID+"/"+nodeID;
    }

    public void setNodeRef(String nodeRef) {
        int lastDash = nodeRef.lastIndexOf("/");
        storeID = nodeRef.substring(0, lastDash);
        nodeID = nodeRef.substring(lastDash+1);
    }
    
    public void parseRef(NodeRef ref){
        storeID = ref.getStoreRef().toString();
        nodeID = ref.getId();
    }
    
    public void fromNodeID(String nodeID){
        storeID = DEFAULT_STORE;
        this.nodeID = nodeID;
    }
    
    public NodeRef asNodeRef(){
        return new NodeRef(getNodeRef());
    }
    
    public static final Reference from(NodeRef ref){
        Reference reference = new Reference();
        reference.parseRef(ref);
        return reference;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.nodeID);
        hash = 11 * hash + Objects.hashCode(this.storeID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Reference other = (Reference) obj;
        if (!Objects.equals(this.nodeID, other.nodeID)) {
            return false;
        }
        if (!Objects.equals(this.storeID, other.storeID)) {
            return false;
        }
        return true;
    }
    
    public ToStringBuilder toStringBuilder(){
        return new ToStringBuilder(this).append("StoreID", this.getStoreID()).append("NodeID", this.getNodeID()); 
    }
    
    @Override
    public String toString(){
        return toStringBuilder().build();
    }
    
    
}
