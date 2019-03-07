/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetApplicationsInBranch extends JacksonBackedWebscript{

    @Override
    protected List<ApplicationSummary> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String branchID = getUrlParams().get("branchID");
        String budgetID = getUrlQueryParams().get("budgetID");
        Reference ref = new Reference();
        ref.setNodeID(branchID);   
        
        List<ApplicationSummary> branchSummaries = getBranchBean().getBranchApplications(ref.asNodeRef());
        if(budgetID == null){
            return branchSummaries;
        }else{
            Reference budgetReference = new Reference();
            budgetReference.setNodeID(budgetID);
            Budget budget = getBudgetBean().getBudget(budgetReference.asNodeRef());
            Set<NodeRef> budgetApplicationNodeRefs = new HashSet<>();
            for(ApplicationReference budgetAppRef : budget.getApplications()){
                budgetApplicationNodeRefs.add(budgetAppRef.asNodeRef());
            }
            
            branchSummaries.removeIf((ApplicationSummary t) -> {
                return !budgetApplicationNodeRefs.contains(t.asNodeRef());
            });
            
            return branchSummaries;
        }
    }

    
    
}
