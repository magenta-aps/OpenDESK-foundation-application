/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.enums;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author martin
 */
public class TypeMapping<E>{
    private static final Map<String, TypeMapping> mappings;
    
    private final String type;
    private final Class<E> mappedType;
    
    static{
        Map<String, TypeMapping> newMappings = new HashMap<>();
        put(newMappings, "datepicker", Date.class);
        put(newMappings, "text", String.class);
        put(newMappings, "integer", Integer.class);
        put(newMappings, "double", Double.class);
        mappings = Collections.unmodifiableMap(newMappings);
    }
    
    private static void put(Map<String, TypeMapping> mappings, String name, Class type){
        mappings.put(name, new TypeMapping(name, type));
    }

    private TypeMapping(String type, Class<E> mappedType) {
        this.type = type;
        this.mappedType = mappedType;
    }

    public String getType() {
        return type;
    }

    public Class getMappedType() {
        return mappedType;
    }
    
    public static TypeMapping getFromName(String name){
        return mappings.get(name);
    }
    
    
    
    
}
