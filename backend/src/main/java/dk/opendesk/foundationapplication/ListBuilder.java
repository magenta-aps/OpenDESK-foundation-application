/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author martin
 * @param <E>
 * @param <T>
 */
public class ListBuilder<E, T extends List<E>> {
    private final T list;
    
    public ListBuilder(T list){
        this.list = list;
    }
    
    public ListBuilder<E, T> add(E value){
        list.add(value);
        return this;
    }
    
    public ListBuilder<E, T> add(int index, E value){
        list.add(index, value);
        return this;
    }
    
    public ListBuilder<E, T> addAll(List<E> values){
        list.addAll(values);
        return this;
    }
    
    public ListBuilder<E, T> addAll(int index, Collection<? extends E> values){
        list.addAll(index, values);
        return this;
    }
    public T build(){
        return list;
    }
    
    public static <E> ArrayList<E> listFrom(E value){
        return new ListBuilder<>(new ArrayList<E>()).add(value).build();
    }
}