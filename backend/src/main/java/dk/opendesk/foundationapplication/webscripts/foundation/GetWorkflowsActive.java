/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.WorkflowReference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetWorkflowsActive extends JacksonBackedWebscript{

    @Override
    protected List<WorkflowReference> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        //A workflow can be used by several branches. We do not want duplicates, so we use a set which will ignore repeated equal noderefs.
        Set<WorkflowReference> workflows = new TreeSet<>((o1, o2) -> o1.getNodeRef().compareTo(o2.getNodeRef()));
        for(BranchSummary branch : getBranchBean().getBranchSummaries()){
            workflows.add(branch.getWorkflowRef());
        }
        List<WorkflowReference> workflowList = new ArrayList<>(workflows);
        Collections.sort(workflowList, new Comparator<WorkflowReference>() {
            @Override
            public int compare(WorkflowReference o1, WorkflowReference o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return workflowList;
    }
    
}
