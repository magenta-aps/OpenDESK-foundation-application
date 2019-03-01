/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author martin
 */
public class Functional<E>{
    private static final Map<String, Functional> functionals;
    
    private final Class requiredType;
    private final String friendlyName;

    static{
        Map<String, Functional> newFunctionals = new HashMap<>();
        put(newFunctionals, "amount", Number.class);
        put(newFunctionals, "email_to", String.class);
        functionals = Collections.unmodifiableMap(newFunctionals);
        
        
    }
    
    private static void put(Map<String, Functional> map, String name, Class type){
        map.put(name, new Functional(name, type));
    }
    
    private Functional(String friendlyName, Class requiredType) {
        this.requiredType = requiredType;
        this.friendlyName = friendlyName;
    }

    public Class getRequiredType() {
        return requiredType;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
    
    public static Functional getFromName(String name){
        return functionals.get(name);
    }
    
    public static Functional<Long> amount(){
        return getFromName("amount");
    }
    public static Functional<String> email_to(){
        return getFromName("email_to");
    }
    
}
