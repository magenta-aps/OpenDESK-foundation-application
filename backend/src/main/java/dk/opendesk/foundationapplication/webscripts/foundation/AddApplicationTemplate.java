/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.webscripts.foundation;

import dk.opendesk.foundationapplication.DAO.Application;
import dk.opendesk.foundationapplication.DAO.ApplicationReference;
import dk.opendesk.foundationapplication.DAO.BranchSummary;
import dk.opendesk.foundationapplication.DAO.BudgetReference;
import dk.opendesk.foundationapplication.DAO.StateReference;
import dk.opendesk.foundationapplication.webscripts.JacksonBackedWebscript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * @author martin
 */
public class AddApplicationTemplate extends JacksonBackedWebscript{


    @Override
    protected ApplicationReference doAction(WebScriptRequest req, WebScriptResponse res) throws Exception {
        Application input = getRequestAs(Application.class);
        String name = input.getTitle();
        BranchSummary branch = input.getBranchSummary();
        BudgetReference budget = input.getBudget();
        StateReference state = input.getState();
        Application newApplication = new Application();
        if(branch != null){
            newApplication.setBranchSummary(branch);
        }
        if(budget != null){
            newApplication.setBudget(budget);
        }
        if(state != null){
            newApplication.setState(state);
        }
        newApplication.setTitle(name);
//        newApplication.setRecipient(name+"_recipient");
//        newApplication.setShortDescription(name+"_description");
//        newApplication.setContactFirstName(name+"_first_name");
//        newApplication.setContactLastName(name+"_last_anem");
//        newApplication.setContactEmail(name+"@mail.test");
//        newApplication.setContactPhone("+45"+randomNumbers(8));
//        newApplication.setCategory("Category2");
//        newApplication.setAddressRoad(name+"_street");
//        newApplication.setAddressNumber(14);
//        newApplication.setAddressFloor(randomNumbers(1));
//        newApplication.setAddressPostalCode(randomNumbers(4));
//        newApplication.setAmountApplied(100000l);
//        newApplication.setAccountRegistration(randomNumbers(4));
//        newApplication.setAccountNumber(randomNumbers(8));
//        newApplication.setStartDate(Date.from(Instant.now()));
//        newApplication.setEndDate(Date.from(Instant.now().plus(Duration.ofDays(30))));
        
        
        return getFoundationBean().addNewApplication(newApplication);
    }
    
    String randomNumbers(int length){
        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i<length; i++){
            int randomInt = (int)(Math.random()*10);
            builder.append(randomInt);
        }
        return builder.toString();
    }
    
    
}