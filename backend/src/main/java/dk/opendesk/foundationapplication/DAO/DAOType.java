/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author martin
 */
public class DAOType {
    
    
    public ToStringBuilder toStringBuilder(){
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE); 
    }
    
    @Override
    public String toString(){
        return toStringBuilder().build();
    }
    
    protected <T> Optional<T> optional(T value){
        if(value != null){
            return Optional.of(value);
        }else{
            return Optional.empty();
        }
    }
    
    protected <T> T get(Optional<T> value){
        if(value != null && value.isPresent()){
            return value.get();
        }else{
            return null;
        }
    }
    
    protected boolean wasSet(Optional value){
        return value != null;
    }
    
}
