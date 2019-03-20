/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.enums.PermissionGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

/**
 *
 * @author martin
 */
public class AuthorityBean extends FoundationBean{
    public static final String ERROR_GROUP_NOT_FOUND = "odf.group.not.found";
    
    public Set<String> getAllCreatedGroups(){
         AuthorityService as = getServiceRegistry().getAuthorityService();
         return getAllCreatedGroups(as);
    }
    
    public static Set<String> getAllCreatedGroups(AuthorityService as){
        
        Set<String> toReturn = new HashSet<>();
            try{
                for(String group : as.findAuthorities(AuthorityType.GROUP, getGroup(PermissionGroup.SUPER, (String)null, true, as), false, null, null)){
                toReturn.add(group);
            }
            }catch(InvalidNodeRefException ex){
                
            }catch(AlfrescoRuntimeException ex){
            //If the group doesn't exist, just skip. But if it is another exception, rethrow.
            if(!ex.getMsgId().equals(ERROR_GROUP_NOT_FOUND)){
                throw ex;
            }
        }
            try{
                for(String group : as.findAuthorities(AuthorityType.GROUP, getGroup(PermissionGroup.SUPER, (String)null, false, as), false, null, null)){
                toReturn.add(group);
                }
            }catch(InvalidNodeRefException ex){
                
            }catch(AlfrescoRuntimeException ex){
            //If the group doesn't exist, just skip. But if it is another exception, rethrow.
            if(!ex.getMsgId().equals(ERROR_GROUP_NOT_FOUND)){
                throw ex;
            }
        }
           
        
        return toReturn;
    }
    
    
    public void addFullPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        addFullPermission(target, group, subName.getTitle());
    }
    
    public void addFullPermission(NodeRef target, PermissionGroup group, String subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, false);
        String writePermissionGroup = getOrCreateGroup(group, subName, true);
        ps.setPermission(target, readPermissionGroup, PermissionService.READ, true);
        ps.setPermission(target, writePermissionGroup, PermissionService.READ, true);
        ps.setPermission(target, writePermissionGroup, PermissionService.WRITE, true);
    }
    
    public void addReadPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        addReadPermission(target, group, subName.getTitle());
    }
    
    public void addReadPermission(NodeRef target, PermissionGroup group, String subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, false);
        ps.setPermission(target, readPermissionGroup, PermissionService.READ, true);
    }
    
        
    public void addWritePermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        addWritePermission(target, group, subName.getTitle());
    }
    
    public void addWritePermission(NodeRef target, PermissionGroup group, String subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, true);
        ps.setPermission(target, readPermissionGroup, PermissionService.WRITE, true);
    }
    
    public void removeFullPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        removeFullPermission(target, group, subName.getTitle());
    }
    
    public void removeFullPermission(NodeRef target, PermissionGroup group, String subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, false);
        String writePermissionGroup = getOrCreateGroup(group, subName, true);
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ);
        ps.deletePermission(target, writePermissionGroup, PermissionService.READ);
        ps.deletePermission(target, writePermissionGroup, PermissionService.WRITE);
    }
    
    public void removeReadPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        removeReadPermission(target, group, subName.getTitle());
    }
    
    public void removeReadPermission(NodeRef target, PermissionGroup group, String subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, false);
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ);
    }
        
    public void removeWritePermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        removeWritePermission(target, group, subName.getTitle());
    }
    
    public void removeWritePermission(NodeRef target, PermissionGroup group, String subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, true);
        ps.deletePermission(target, readPermissionGroup, PermissionService.WRITE);
    }
    
    public String getGroup(PermissionGroup group, boolean write){
        return getGroup(group, (String)null, write);
    }
    
    public String getGroup(PermissionGroup group, Reference subName, boolean write){
        verifyType(group, subName);
        return getGroup(group, subName.getTitle(), write);
    }
    
    public String getGroup(PermissionGroup group, String subName, boolean write){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        return getGroup(group, subName, write, as);
    }
    
    public static String getGroup(PermissionGroup group, String subName, boolean write, AuthorityService as){
        String shortName = group.getShortName(subName)+(!write ? "_Read" : "");
        String groupName = as.getName(AuthorityType.GROUP, shortName);
        if(!as.authorityExists(groupName)){
            throw new AlfrescoRuntimeException(ERROR_GROUP_NOT_FOUND, new Object[]{groupName});
        }
        return groupName;
    }
    
    public String getOrCreateGroup(PermissionGroup group, String subName, boolean write){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        String shortName = group.getShortName(subName)+(!write ? "_Read" : "");
        String groupName = as.getName(AuthorityType.GROUP, shortName);
        if(!as.authorityExists(groupName)){
            String newGroupName = as.createAuthority(AuthorityType.GROUP, shortName);
            getLogger().debug("Created new group: "+newGroupName);
            if(!newGroupName.equals(groupName)){
                //If this happens, group names are not created as we expect.
                throw new RuntimeException("A group was created with an unexpected name");
            }
            if(subName != null){
                //This is a subgroup, and should be added to the supergroup.
                String superGroup = getOrCreateGroup(group, null, write);
                linkAuthorities(superGroup, groupName);
            }else if(!PermissionGroup.SUPER.equals(group)){
                //This is a supergroup, add it so superadmin group
                String superAdminGroup = getOrCreateGroup(PermissionGroup.SUPER, null, write);
                linkAuthorities(superAdminGroup, groupName);
            }
        }
        return groupName;
    }
    
    public void linkAuthorities(String inherits, String from){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.addAuthority(inherits, from);
    }
    
    public void unlinkAuthorities(String inherits, String from){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.removeAuthority(inherits, from);
    }
    
    public void addUserGroup(String userName, String group){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.addAuthority(group, userName);
    }
    
    public void removeUserGroup(String userName, String group){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.removeAuthority(group, userName);
    }
    
    public void enableInheritPermissions(NodeRef ref){
        getServiceRegistry().getPermissionService().setInheritParentPermissions(ref, true);
    }
    public void disableInheritPermissions(NodeRef ref){
        getServiceRegistry().getPermissionService().setInheritParentPermissions(ref, false);
    }
    
    public void verifyType(PermissionGroup group, Reference subName){
        if(subName != null && group.getRequiredType() == null){
            throw new RuntimeException("PermissionGroup "+group+" cannot be used with a subName");
        }
        if(!group.getRequiredType().isAssignableFrom(subName.getClass())){
            throw new RuntimeException("PermissionGroup "+group+" cannot be used with Reference of class "+subName.getClass()+" must be used with "+group.getRequiredType());
        }
        if(subName.getTitle() == null || subName.getTitle().isEmpty()){
            throw new RuntimeException("Title of Reference must not be null");
        }
    }
    
}
