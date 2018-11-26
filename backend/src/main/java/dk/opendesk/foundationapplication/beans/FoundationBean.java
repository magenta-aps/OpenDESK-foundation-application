/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.beans;

import dk.opendesk.repo.beans.PersonBean;
import org.alfresco.service.cmr.security.AuthorityService;

/**
 *
 * @author martin
 */
public class FoundationBean {
    
    private AuthorityService authorityService;

    public void setAuthorityService (AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
    
    
    
    
    
}
