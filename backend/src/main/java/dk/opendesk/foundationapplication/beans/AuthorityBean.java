/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.enums.PermissionGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.opendesk.repo.beans.PersonBean;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static dk.opendesk.foundationapplication.Utilities.BUDGETYEAR_ASSOC_BUDGETS;
import static dk.opendesk.foundationapplication.Utilities.DATA_ASSOC_BRANCHES;
import static dk.opendesk.foundationapplication.Utilities.DATA_ASSOC_BUDGETYEARS;
import static dk.opendesk.foundationapplication.Utilities.DATA_ASSOC_NEW_APPLICATIONS;
import static dk.opendesk.foundationapplication.Utilities.DATA_ASSOC_WORKFLOW;
import static dk.opendesk.foundationapplication.Utilities.getCMName;
import static dk.opendesk.foundationapplication.Utilities.getODFName;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_SUBJECT;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TEMPLATE_MODEL;
import static org.alfresco.repo.action.executer.MailActionExecuter.PARAM_TO;

/**
 *
 * @author martin
 */
public class AuthorityBean extends FoundationBean{
    public static final String ERROR_GROUP_NOT_FOUND = "odf.group.not.found";
    private static final String ERROR_SUBNAME_NOT_FOUND = "odf.subname.not.found";
    private static final String ERROR_SUBNAME_NOT_UNIQUE = "odf.subname.not.unique";
    public static final String ERROR_EMPTY_EMAIL = "odf.authoritybean.empty.email";
    private static final String ERROR_UNKNOWN_KEY = "odf.authoritybean.unknown.key";
    private static final String ERROR_EMPTY_PERMISSIONS = "odf.authoritybean.empty.permissions";
    private static final String ERROR_UNKNOWN_ROLE = "odf.authorityBean.unknown.role";
    private static final String ERROR_UNKNOWN_PERMISSION = "odf.authorityBean.unknown.permission";
    private PersonBean personBean;

    public void setPersonBean(PersonBean personBean) {
        this.personBean = personBean;
    }

    public Set<String> getAllCreatedGroups(){
         AuthorityService as = getServiceRegistry().getAuthorityService();
         return getAllCreatedGroups(as);
    }
    
    public static Set<String> getAllCreatedGroups(AuthorityService as){
        Set<String> toReturn = new HashSet<>();
        try{
        Set<String> writeGroups = as.getContainingAuthorities(AuthorityType.GROUP, getGroup(PermissionGroup.SUPER, null, true, as), false);
        toReturn.addAll(writeGroups);
        }catch(AlfrescoRuntimeException ex){
            //If the group doesn't exist, just skip. But if it is another exception, rethrow.
            if(!ex.getMsgId().equals(ERROR_GROUP_NOT_FOUND)){
                throw ex;
            }
        }
        
        try{
        Set<String> readGroups = as.getContainingAuthorities(AuthorityType.GROUP, getGroup(PermissionGroup.SUPER, null, false, as), false);
        toReturn.addAll(readGroups);
        }catch(AlfrescoRuntimeException ex){
            //If the group doesn't exist, just skip. But if it is another exception, rethrow.
            if(!ex.getMsgId().equals(ERROR_GROUP_NOT_FOUND)){
                throw ex;
            }
        }
        
//            try{
//                for(String group : as.findAuthorities(AuthorityType.GROUP, getGroup(PermissionGroup.SUPER, (NodeRef)null, true, as), false, null, null)){
//                toReturn.add(group);
//            }
//            }catch(InvalidNodeRefException ex){
//                
//            }catch(AlfrescoRuntimeException ex){
//            //If the group doesn't exist, just skip. But if it is another exception, rethrow.
//            if(!ex.getMsgId().equals(ERROR_GROUP_NOT_FOUND)){
//                throw ex;
//            }
//        }
//            try{
//                for(String group : as.findAuthorities(AuthorityType.GROUP, getGroup(PermissionGroup.SUPER, (NodeRef)null, false, as), false, null, null)){
//                toReturn.add(group);
//                }
//            }catch(InvalidNodeRefException ex){
//                
//            }catch(AlfrescoRuntimeException ex){
//            //If the group doesn't exist, just skip. But if it is another exception, rethrow.
//            if(!ex.getMsgId().equals(ERROR_GROUP_NOT_FOUND)){
//                throw ex;
//            }
//        }
           
        
        return toReturn;
    }
    
    
    
    public void addFullPermission(NodeRef target, PermissionGroup group){
        addFullPermission(target, group, (NodeRef)null);
    }
    
    public void addFullPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        addFullPermission(target, group, subName.asNodeRef());
    }
    
    public void addFullPermission(NodeRef target, PermissionGroup group, NodeRef subName){
        addReadPermission(target, group, subName);
        addWritePermission(target, group, subName);
    }
      
    public void addReadPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        addReadPermission(target, group, subName.asNodeRef());
    }
    
    public void addReadPermission(NodeRef target, PermissionGroup group, NodeRef subName){
        String readPermissionGroup = getOrCreateGroup(group, subName, false);
        addReadPermission(target, readPermissionGroup);
        
    }
    
    public void addReadPermission(NodeRef target, String authority){
        PermissionService ps = getServiceRegistry().getPermissionService();
        ps.setPermission(target, authority, PermissionService.READ, true);
        ps.setPermission(target, authority, PermissionService.READ_PERMISSIONS, true);
        ps.setPermission(target, authority, PermissionService.READ_CONTENT, true);
        ps.setPermission(target, authority, PermissionService.READ_ASSOCIATIONS, true);
        ps.setPermission(target, authority, PermissionService.READ_CHILDREN, true);
    }
    
        
    public void addWritePermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        addWritePermission(target, group, subName.asNodeRef());
    }
    
    public void addWritePermission(NodeRef target, PermissionGroup group, NodeRef subName){
        String readPermissionGroup = getOrCreateGroup(group, subName, true);
        addWritePermission(target, readPermissionGroup);
    }
    
    public void addWritePermission(NodeRef target, String authority){
        PermissionService ps = getServiceRegistry().getPermissionService();
        ps.setPermission(target, authority, PermissionService.WRITE, true);
        ps.setPermission(target, authority, PermissionService.WRITE_CONTENT, true);
        ps.setPermission(target, authority, PermissionService.WRITE_PROPERTIES, true);
        ps.setPermission(target, authority, PermissionService.DELETE, true);
        ps.setPermission(target, authority, PermissionService.DELETE_ASSOCIATIONS, true);
        ps.setPermission(target, authority, PermissionService.DELETE_CHILDREN, true);
        ps.setPermission(target, authority, PermissionService.DELETE_NODE, true);
        ps.setPermission(target, authority, PermissionService.CREATE_ASSOCIATIONS, true);
        ps.setPermission(target, authority, PermissionService.CREATE_CHILDREN, true);
        ps.setPermission(target, authority, PermissionService.CHANGE_PERMISSIONS, true);
        ps.setPermission(target, authority, PermissionService.ADD_CHILDREN, true);
    }
    
    public void removeFullPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        removeFullPermission(target, group, subName.asNodeRef());
    }
    
    public void removeFullPermission(NodeRef target, PermissionGroup group, NodeRef subName){
        removeReadPermission(target, group, subName);
        removeWritePermission(target, group, subName);
    }
    
    public void removeReadPermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        removeReadPermission(target, group, subName.asNodeRef());
    }
    
    public void removeReadPermission(NodeRef target, PermissionGroup group, NodeRef subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, false);        
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ);
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ_PERMISSIONS);
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ_CONTENT);
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ_ASSOCIATIONS);
        ps.deletePermission(target, readPermissionGroup, PermissionService.READ_CHILDREN);
    }
        
    public void removeWritePermission(NodeRef target, PermissionGroup group, Reference subName){
        verifyType(group, subName);
        removeWritePermission(target, group, subName.asNodeRef());
    }
    
    public void removeWritePermission(NodeRef target, PermissionGroup group, NodeRef subName){
        PermissionService ps = getServiceRegistry().getPermissionService();
        String readPermissionGroup = getOrCreateGroup(group, subName, true);        
        ps.deletePermission(target, readPermissionGroup, PermissionService.WRITE);
        ps.deletePermission(target, readPermissionGroup, PermissionService.WRITE_CONTENT);
        ps.deletePermission(target, readPermissionGroup, PermissionService.WRITE_PROPERTIES);
        ps.deletePermission(target, readPermissionGroup, PermissionService.DELETE);
        ps.deletePermission(target, readPermissionGroup, PermissionService.DELETE_ASSOCIATIONS);
        ps.deletePermission(target, readPermissionGroup, PermissionService.DELETE_CHILDREN);
        ps.deletePermission(target, readPermissionGroup, PermissionService.DELETE_NODE);
        ps.deletePermission(target, readPermissionGroup, PermissionService.CREATE_ASSOCIATIONS);
        ps.deletePermission(target, readPermissionGroup, PermissionService.CREATE_CHILDREN);
        ps.deletePermission(target, readPermissionGroup, PermissionService.CHANGE_PERMISSIONS);
        ps.deletePermission(target, readPermissionGroup, PermissionService.ADD_CHILDREN);
    }
    
    public String getGroup(PermissionGroup group, boolean write){
        return getGroup(group, (NodeRef)null, write);
    }
    
    public String getGroup(PermissionGroup group, Reference subName, boolean write){
        verifyType(group, subName);
        return getGroup(group, subName.asNodeRef(), write);
    }
    
    public String getGroup(PermissionGroup group, NodeRef subName, boolean write){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        return getGroup(group, subName, write, as);
    }
    
    public static String getGroup(PermissionGroup group, NodeRef subName, boolean write, AuthorityService as){
        String shortName = group.getShortName(subName)+(!write ? "_Read" : "");
        String groupName = as.getName(AuthorityType.GROUP, shortName);
        if(!as.authorityExists(groupName)){
            throw new AlfrescoRuntimeException(ERROR_GROUP_NOT_FOUND, new Object[]{groupName});
        }
        return groupName;
    }
    
    public String getOrCreateGroup(PermissionGroup group, NodeRef subName, boolean write){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        String shortName = group.getShortName(subName)+(!write ? "_Read" : "");
        String groupName = as.getName(AuthorityType.GROUP, shortName);
        if(!as.authorityExists(groupName)){
            String newGroupName = as.createAuthority(AuthorityType.GROUP, shortName);
            getLogger().debug("Created new group: "+newGroupName);
            if(write){
                linkAuthorities(newGroupName, getOrCreateGroup(group, subName, false));
            }
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
        }else{
            getLogger().debug("Found group: "+groupName);
        }
        return groupName;
    }
    
    public void linkAuthorities(String inherits, String from){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.addAuthority(from, inherits);
    }
    
    public void unlinkAuthorities(String inherits, String from){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.removeAuthority(from, inherits);
    }
    
    public void addUserToGroup(String userName, String group){
        AuthorityService as = getServiceRegistry().getAuthorityService();
        as.addAuthority(group, userName);
    }
    
    public void removeUserFromGroup(String userName, String group){
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
        if(subName != null && !group.getRequiredType().isAssignableFrom(subName.getClass())){
            throw new RuntimeException("PermissionGroup "+group+" cannot be used with Reference of class "+subName.getClass()+" must be used with "+group.getRequiredType());
        }
        if(subName != null && subName.asNodeRef() == null){
            throw new RuntimeException("Title of Reference must not be null");
        }
    }


    public void loadUsers(JSONObject roleObject, JSONArray userArray, String emailSubject, NodeRef emailTemplate) throws Exception {
        loadUsers(roleObject,userArray,emailSubject,emailTemplate,null);
    }

    public void loadUsers(JSONObject roleObject, JSONArray userArray, String emailSubject, NodeRef emailTemplate, HashMap<String, Serializable> emailModel) throws Exception {
        AuthorityService as = getServiceRegistry().getAuthorityService();

        Map<String,String> roleMap = readRolePermissionsFromJson(roleObject); //maps roleNames to roleAuthorities
        Map<String,Map<QName,Serializable>> personPropMap = new HashMap<>();  //maps userNames to personProperties
        Map<String, String> personRoleMap = new HashMap<>();                  //maps userNames to roleNames

        readUsersFromJson(userArray,roleMap,personPropMap,personRoleMap);

        RetryingTransactionHelper trans = getServiceRegistry().getRetryingTransactionHelper();
        trans.doInTransaction(() -> {
            for (String userName : personPropMap.keySet()) {
                Map<QName,Serializable> personProps = personPropMap.get(userName);
                String email = personProps.get(getCMName("email")).toString();

                AuthenticationUtil.runAs(() -> {
                    //creating user
                    String password = personBean.createPerson(personPropMap.get(userName));

                    //setting role
                    String roleName = personRoleMap.get(userName);
                    String authority = roleMap.get(roleName);
                    addUserToGroup(userName, authority);


                    //sending email
                    HashMap<String, Serializable> model = emailModel;
                    if (model == null) {
                        model = new HashMap<>();
                    }
                    model.put("userName", userName);
                    model.put("password", password);
                    model.put("email", email);
                    model.put("subject", emailSubject);
                    model.put("role", personRoleMap.get(userName));
                    model.put("firstName", personProps.getOrDefault(getCMName("firstName"),"*** fornavn mangler ***")); //todo hvad er kotyme?
                    model.put("lastName", personProps.getOrDefault(getCMName("lastName"),"*** efternavn mangler ***"));
                    model.put("phone", personProps.getOrDefault(getCMName("phone"),"*** telefonnummer mangler ***"));

                    Map<String, Serializable> emailActionParams = new HashMap<>();
                    emailActionParams.put(PARAM_TO, email);
                    emailActionParams.put(PARAM_SUBJECT, emailSubject);
                    emailActionParams.put(PARAM_TEMPLATE, emailTemplate);
                    emailActionParams.put(PARAM_TEMPLATE_MODEL, model);
                    Action emailAction = getServiceRegistry().getActionService().createAction("mail", emailActionParams); //uses standard MailActionExecuter
                    getServiceRegistry().getActionService().executeAction(emailAction, null);
                    return true;
                }, AuthenticationUtil.getSystemUserName());
            }
            return null;
        });
    }

    public Map<String, String> readRolePermissionsFromJson(JSONObject roleObject) throws Exception {
        AuthorityService as = getServiceRegistry().getAuthorityService();

        Map<String, String> roleMap = new HashMap<>();

        // reading groups from file
        Iterator<String> roleIterator = roleObject.keys();   //role: direktør, sekretær etc

        while (roleIterator.hasNext()) {
            String roleName = roleIterator.next();
            String roleAuth = as.createAuthority(AuthorityType.GROUP, roleName);
            roleMap.put(roleName,roleAuth);
            JSONObject permissionsAsJson = roleObject.getJSONObject(roleName);

            Iterator<String> groupIterator = permissionsAsJson.keys(); //group: State, Branch etc

            while (groupIterator.hasNext()) {
                String group = groupIterator.next();
                String groupPermissionsAsString = permissionsAsJson.getString(group);
                PermissionGroup permissionGroup = PermissionGroup.getPermissionGroup(group);

                if (groupPermissionsAsString.equals("read")) {
                    String authority = getGroup(permissionGroup, (NodeRef)null,false);
                    linkAuthorities(roleAuth,authority);
                } else if (groupPermissionsAsString.equals("write") || groupPermissionsAsString.equals("*")) {
                    String authority = getGroup(permissionGroup, (NodeRef) null, true);
                    linkAuthorities(roleAuth,authority);
                } else {
                    JSONObject groupPermissionsAsJson = new JSONObject(groupPermissionsAsString);
                    Iterator<String> subNameIterator = groupPermissionsAsJson.keys();  //subName: special State (Received, Denied, ...) , special Branch ect

                    while (subNameIterator.hasNext()) {
                        String subName = subNameIterator.next();
                        NodeRef subNameRef;
                        boolean canWrite = false;

                        String subNamePerm = groupPermissionsAsJson.getString(subName);
                        if (subNamePerm.equals("write")) {
                            subNameRef = findSubNameRef(permissionGroup, subName);
                            canWrite = true;
                        } else if (subNamePerm.equals("read")) {
                            subNameRef = findSubNameRef(permissionGroup, subName);
                        } else {
                            throw new AlfrescoRuntimeException(ERROR_UNKNOWN_PERMISSION);
                        }
                        String authority = getGroup(permissionGroup,subNameRef,canWrite);
                        linkAuthorities(roleAuth,authority);
                    }
                }
            }
        }
        return roleMap;
    }


    public void readUsersFromJson(
            JSONArray userArray,
            Map<String,String> roleMap,
            Map<String,Map<QName,Serializable>> personPropMap,
            Map<String, String> personRoleMap) throws JSONException {

        for (int i = 0; i < userArray.length(); i++) {
            JSONObject user = userArray.getJSONObject(i);
            Iterator<String> userIterator = user.keys();

            String firstName = null;
            String lastName = null;
            String email = null;
            String phone = null;
            String role = null;


            while (userIterator.hasNext()) {
                String key = userIterator.next();

                switch (key) {
                    case "fornavn":
                        firstName = user.getString(key);
                        break;
                    case "efternavn":
                        lastName = user.getString(key);
                        break;
                    case "email":
                        email = user.getString(key);
                        break;
                    case "telefon":
                        phone = user.getString(key);
                        break;
                    case "rolle":
                        role = user.getString(key);
                        break;
                    default:
                        throw new AlfrescoRuntimeException(ERROR_UNKNOWN_KEY);
                }
            }
            if (email == null || email.isEmpty()) {
                throw new AlfrescoRuntimeException(ERROR_EMPTY_EMAIL);
            }
            if (!roleMap.containsKey(role)) {
                throw new AlfrescoRuntimeException(ERROR_UNKNOWN_ROLE);
            }

            Map<QName, Serializable> personProps = personBean.createPersonProperties("", firstName, lastName, email, phone);
            String userName = personProps.get(getCMName("userName")).toString();
            personPropMap.put(userName, personProps);
            personRoleMap.put(userName, role);
        }
    }


    private NodeRef findSubNameRef(PermissionGroup group, String subName) throws Exception {
        List<ChildAssociationRef> childAssocs;
        switch (group) {
            case BRANCH:
                childAssocs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_BRANCHES), getODFName(subName));
                break;
            case WORKFLOW:
                childAssocs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_WORKFLOW), getODFName(subName));
                break;
            case NEW_APPLICATION:
                childAssocs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_NEW_APPLICATIONS), getODFName(subName));
                break;
            case BUDGET_YEAR:
                childAssocs = getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_BUDGETYEARS), getODFName(subName));
                break;
            case BUDGET:
                childAssocs = new ArrayList<>();
                for (ChildAssociationRef c : getServiceRegistry().getNodeService().getChildAssocs(getDataHome(), getODFName(DATA_ASSOC_BUDGETYEARS), null)) {
                    childAssocs.addAll(getServiceRegistry().getNodeService().getChildAssocs(c.getChildRef(), getODFName(BUDGETYEAR_ASSOC_BUDGETS), getODFName(subName)));
                }
                break;
            default:
                throw new NotImplementedException("findSubNameRef for " + group);
        }
        if (childAssocs.size() == 0) {
            throw new AlfrescoRuntimeException(ERROR_SUBNAME_NOT_FOUND);
        }
        if (childAssocs.size() > 1) {
            throw new AlfrescoRuntimeException(ERROR_SUBNAME_NOT_UNIQUE);
        }
        return childAssocs.get(0).getChildRef();
    }
}
