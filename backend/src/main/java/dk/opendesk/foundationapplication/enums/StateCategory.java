/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author martin
 */
public enum StateCategory {
    NOMINATED("nominated"), ACCEPTED("accepted"), CLOSED("closed"), REJECTED("rejected");
    
    private final String categoryName;
    
    private StateCategory(String stateName){
        this.categoryName = stateName;
    }

    @JsonValue
    public String getCategoryName() {
        return categoryName;
    }
    
    public static StateCategory getFromName(String name){
        for(StateCategory category : values()){
            if(category.getCategoryName().equals(name)){
                return category;
            }
        }
        return null;
    }
    
}
