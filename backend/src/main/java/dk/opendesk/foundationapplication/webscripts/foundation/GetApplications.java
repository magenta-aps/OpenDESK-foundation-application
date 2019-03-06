/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.ApplicationSummary;
import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.DAO.BranchReference;
import dk.opendesk.foundationapplication.DAO.Budget;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetApplications extends JacksonBackedWebscript{
    
    @Override
    protected List<ApplicationSummary> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        String branchID = getUrlQueryParams().get("branchID");
        String budgetID = getUrlQueryParams().get("budgetID");
        
        Branch branch = null;
        Budget budget = null;
        if(branchID != null){
            branch = getBranchBean().getBranch(Reference.refFromID(branchID));
        }
        if(budgetID != null){
            budget = getBudgetBean().getBudget(Reference.refFromID(budgetID));
        }
        
        if(branch == null && budget == null){
            return getApplicationBean().getApplicationSummaries();
        }
        if(branch != null && budget != null){
            List<ApplicationSummary> branchRefs = new LinkedList<>(branch.getSummaries());
            Set<NodeRef> budgetRefs = new HashSet<>();
            for(ApplicationReference ref : budget.getApplications()){
                budgetRefs.add(ref.asNodeRef());
            }
            ListIterator<ApplicationSummary> branchRefIterator = branchRefs.listIterator(branchRefs.size());
            while(branchRefIterator.hasPrevious()){
                ApplicationSummary branchApp = branchRefIterator.previous();
                if(!budgetRefs.contains(branchApp.asNodeRef())){
                    branchRefIterator.remove();
                }
            }
            return branchRefs;
            
        }
        if(branch != null){
            return branch.getSummaries();
        }
        if(budget != null){
            List<ApplicationSummary> budgetSummaries = new ArrayList<>();
            for(ApplicationReference ref : budget.getApplications()){
                budgetSummaries.add(getApplicationBean().getApplicationSummary(ref.asNodeRef()));
            }
            return budgetSummaries;
        }
        
        return null;
    }
}
