/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.validator.aggregate;

import java.util.Collection;
import java.util.List;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public class IntegerRating implements Aggregator<Integer, Integer>{

    @Override
    public Class<Integer> getInputType() {
        return Integer.class;
    }

    @Override
    public Class<Integer> getOutputType() {
        return Integer.class;
    }

    @Override
    public Integer convert(Collection<Collection<Integer>> inputs, ServiceRegistry serviceRegistry) {
        Integer aggregate = 0;
        Integer count = 0;
        for(Collection<Integer> input : inputs){
            for(Integer integer : input){
                aggregate = aggregate + integer;
                count ++;
            }
            
        }
        if(count == 0){
            return 0;
        }
        return aggregate/count;
    }
    
    
    
}
