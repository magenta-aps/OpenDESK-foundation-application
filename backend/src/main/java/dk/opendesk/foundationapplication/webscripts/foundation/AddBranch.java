/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Branch;
import dk.opendesk.foundationapplication.DAO.Reference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class AddBranch extends JacksonBackedWebscript {
    
    @Override
    protected Reference doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        Branch branch = getRequestAs(Branch.class);    
        return Reference.from(getBranchBean().addNewBranch(branch.getTitle(), branch.getTitle()));
    }
    
}
