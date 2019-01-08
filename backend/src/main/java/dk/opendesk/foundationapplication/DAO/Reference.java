/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Objects;
import java.util.Optional;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author martin
 */
public class Reference {
    public static final String DEFAULT_STORE = "workspace://SpacesStore";
    
    private Optional<String> nodeID = null;
    private Optional<String> storeID = null;

    public Reference() {
    }

    public String getNodeID() {
        return get(nodeID);
    }
    
    public boolean wasNodeIDSet() {
        return wasSet(nodeID);
    }

    public void setNodeID(String nodeID) {
        this.nodeID = optional(nodeID);
    }

    public String getStoreID() {
        return get(storeID);
    }
    
    public boolean wasStoreIDSet(){
        return wasSet(storeID);
    }

    public void setStoreID(String storeID) {
        this.storeID = optional(storeID);
    }

    public String getNodeRef() {
        String actualStoreID = (getStoreID() != null ? getStoreID() : DEFAULT_STORE);
        return actualStoreID+"/"+getNodeID();
    }

    public void setNodeRef(String nodeRef) {
        int lastDash = nodeRef.lastIndexOf("/");
        setStoreID(nodeRef.substring(0, lastDash));
        setNodeID(nodeRef.substring(lastDash+1));
    }
    
    public void parseRef(NodeRef ref){
        setStoreID(ref.getStoreRef().toString());
        setNodeID(ref.getId());
    }
    
    public void fromNodeID(String nodeID){
        setStoreID(DEFAULT_STORE);
        setNodeID(nodeID);
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
        hash = 11 * hash + Objects.hashCode(this.getNodeID());
        hash = 11 * hash + Objects.hashCode(this.getStoreID());
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
        if (!Objects.equals(this.getNodeID(), other.getNodeID())) {
            return false;
        }
        if (!Objects.equals(this.getStoreID(), other.getStoreID())) {
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
    
    protected <T> Optional<T> optional(T value){
        if(value != null){
            return Optional.of(value);
        }else{
            return Optional.empty();
        }
    }
    
    protected <T> T get(Optional<T> value){
        if(value != null && value.isPresent()){
            return value.get();
        }else{
            return null;
        }
    }
    
    protected boolean wasSet(Optional value){
        return value != null;
    }
    
    
}
