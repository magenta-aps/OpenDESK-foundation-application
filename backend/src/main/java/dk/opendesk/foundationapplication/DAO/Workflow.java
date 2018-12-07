/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author martin
 */
public class Workflow extends WorkflowReference{
    private StateReference entry;
    private List<State> states;

    public Workflow() {
        states = new ArrayList<>();
    }

    public StateReference getEntry() {
        return entry;
    }

    public void setEntry(StateReference entry) {
        this.entry = entry;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.getNodeRef());
        hash = 83 * hash + Objects.hashCode(this.getTitle());
        hash = 83 * hash + Objects.hashCode(this.entry);
        hash = 83 * hash + Objects.hashCode(this.states);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Workflow other = (Workflow) obj;
        if (!Objects.equals(this.getNodeRef(), other.getNodeRef())) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.entry, other.entry)) {
            return false;
        }
        if (!Objects.equals(this.states, other.states)) {
            return false;
        }
        return true;
    }
}
