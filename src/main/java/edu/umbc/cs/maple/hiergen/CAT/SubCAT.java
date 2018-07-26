package edu.umbc.cs.maple.hiergen.CAT;

import burlap.mdp.core.oo.state.OOVariableKey;

import java.util.*;

public class SubCAT {

    protected Set<Integer> actionIndexes;
    protected Set<CausalEdge> // put causal edges (incoming outgoing arcs) in here

    public SubCAT( ) {
        actionIndexes = new TreeSet<>();
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
