package edu.umbc.cs.maple.hiergen.CAT;

import burlap.mdp.core.oo.state.OOVariableKey;

import java.util.*;

public class SubCAT {

    protected CATrajectory cat;
    protected Set<Integer> actionIndexes;

    public SubCAT(CATrajectory cat) {
        this.cat = cat;
        this.actionIndexes = new TreeSet<Integer>();
    }

    public boolean contains(int actionIndex) {
        return actionIndexes.contains(actionIndex);
    }

    public void add(int actionIndex) {
        actionIndexes.add(actionIndex);
    }

    public CATrajectory getCat() {
        return cat;
    }

    public int size() {
        return actionIndexes.size();
    }

    public Set<Integer> getActionIndexes() {
        return actionIndexes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : actionIndexes) {
            sb.append(cat.getActions()[i]);
            sb.append(", ");
        }
        return sb.toString();
    }

}
