/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.validator.aggregate;

import java.util.List;
import org.alfresco.service.ServiceRegistry;

/**
 *
 * @author martin
 */
public interface Aggregator<E, T> {
    
    public Class<E> getInputType();
    public Class<T> getOutputType();
    public T convert(List<E> inputs, ServiceRegistry serviceRegistry);
    
}
