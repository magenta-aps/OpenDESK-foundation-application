/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.WorkflowSummary;
import dk.opendesk.foundationapplication.beans.FoundationBean;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import java.util.List;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class GetWorkflows extends JacksonBackedWebscript{

    private FoundationBean foundationBean;

    public void setFoundationBean(FoundationBean foundationBean) {
        this.foundationBean = foundationBean;
    }

    @Override
    protected List<WorkflowSummary> doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        return foundationBean.getWorkflowSummaries();
    }
    
}
