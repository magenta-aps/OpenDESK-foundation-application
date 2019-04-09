/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.enums;

import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.BudgetYear;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 *
 * @author martin
 */
public enum PermissionGroup {
    BRANCH("Branch", BranchReference.class), WORKFLOW("Workflow", WorkflowReference.class), BUDGET_YEAR("BudgetYear", BudgetYear.class),
    BUDGET("Budget", BudgetReference.class), APPLICATION("Application", ApplicationReference.class), NEW_APPLICATION("NewApplication", ApplicationReference.class),
    SUPER("Super", null);    
    private final String shortName;
    private final Class<? extends Reference> requiredType;

    private PermissionGroup(String shortName, Class<? extends Reference> requiredType) {
        this.shortName = shortName;
        this.requiredType = requiredType;
    }

    public Class<? extends Reference> getRequiredType() {
        return requiredType;
    }

    public String getShortName(NodeRef subName){
        if(subName == null){
            return shortName;
        }else{
            return shortName+"_"+subName.getId();
        }
    }

    public static PermissionGroup getPermissionGroup(String name) {
        for (PermissionGroup group : PermissionGroup.values()) {
            if (group.shortName.equals(name)) {
                return group;
            }
        }
        return null;
    }

}
