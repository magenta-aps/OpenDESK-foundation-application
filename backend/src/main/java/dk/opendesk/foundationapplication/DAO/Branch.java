/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author martin
 */
public class Branch extends BranchSummary{
    private final List<ApplicationSummary> summaries = new ArrayList<>();

    public Branch() {
    }

    public Branch(String title, String uuid, String workflowTitle, String workflowUUID) {
        super(title, uuid, workflowTitle, workflowUUID);
    }

    public List<ApplicationSummary> getSummaries() {
        return summaries;
    }

    
}
