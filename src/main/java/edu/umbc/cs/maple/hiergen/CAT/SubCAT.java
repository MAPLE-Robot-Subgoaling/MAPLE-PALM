package edu.umbc.cs.maple.hiergen.CAT;

import burlap.mdp.core.oo.state.OOVariableKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubCAT {

    protected Set<Integer> actionIndexes;

    public SubCAT( ) {
        actionIndexes = new HashSet<>();
    }

    public SubCAT(Set<Integer> actionIndexes) {
        this.actionIndexes = actionIndexes;
    }

    public Set<Integer> getActionIndexes() {
        return actionIndexes;
    }

    public void setActionIndexes(Set<Integer> actionIndexes) {
        this.actionIndexes = actionIndexes;
    }
}
