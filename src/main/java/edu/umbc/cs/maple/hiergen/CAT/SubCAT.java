package edu.umbc.cs.maple.hiergen.CAT;

import burlap.mdp.core.oo.state.OOVariableKey;

import java.util.*;
import java.util.stream.Collectors;

public class SubCAT {

    public static int SUBCAT_ID = 0;

    protected String name;
    protected CATrajectory cat;
    protected Collection<ObjectAttributePair> variables;
    protected Set<Integer> actionIndexes;
    protected Set<Integer> prunedIndexes;
    protected Map<Integer, String> messages;

    public SubCAT(String name, CATrajectory cat, Collection<ObjectAttributePair> variables) {
        this.name = name;
        this.cat = cat;
        this.variables = variables;
        this.actionIndexes = new TreeSet<>();
        this.prunedIndexes = new TreeSet<>();
        this.messages = new LinkedHashMap<>();
    }

    public boolean contains(int actionIndex) {
        return actionIndexes.contains(actionIndex);
    }

    public void add(int actionIndex, String message) {
        boolean added = actionIndexes.add(actionIndex);
        if (added) {
            messages.put(actionIndex, message);
        }
    }

    public void prune(int actionIndex, String message) {
        boolean removed = actionIndexes.remove(actionIndex);
        if (removed) {
            prunedIndexes.add(actionIndex);
            messages.put(actionIndex, "!" +messages.get(actionIndex) + " " + message);
        }
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
        sb.append(name);
        sb.append(": Variables: (");
        boolean first = true;
        for (ObjectAttributePair variable : variables) {
            if (!first) { sb.append(", "); } else { first = false; }
            sb.append(variable);
        }
        sb.append("), Actions: ");
        first = true;
        List<Integer> sortedIndexes = getSortedIndexes();
        for (Integer i : sortedIndexes) {
            if (!first) { sb.append(", "); } else { first = false; }
            sb.append(i);
            sb.append("(");
            sb.append(cat.getActions().get(i));
            sb.append(") ");
            sb.append(messages.get(i));
        }
        sb.append("; Pruned: ");
        first = true;
        for (Integer i : prunedIndexes) {
            if (!first) { sb.append(", "); } else { first = false; }
            sb.append(i);
            sb.append("(");
            sb.append(cat.getActions().get(i));
            sb.append(") ");
            sb.append(messages.get(i));
        }
        return sb.toString();
    }

    public List<CausalEdge> getIncoming() {
        return actionIndexes.stream()
                .map(endIndex -> cat.findIncomingEdges(endIndex))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<CausalEdge> getOutgoing() {
        return actionIndexes.stream()
                .map(startIndex -> cat.findOutgoingEdges(startIndex))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void unify(SubCAT subcat) {
        if (!this.cat.name.equals(subcat.cat.name)) {
            throw new RuntimeException("Trying to unify subcats from different cats");
        }
        for(Integer actionIndex : subcat.actionIndexes) {
            this.variables.addAll(subcat.variables);
            String message = subcat.messages.get(actionIndex);
            this.add(actionIndex, message);
//            String message = this.messages.computeIfAbsent(actionIndex, s -> "");
//            message += " (unified)";
//            this.messages.put(actionIndex, message);
        }
    }

    public List<Integer> getSortedIndexes() {
        List<Integer> indexes = new ArrayList<>(cat.getActions().keySet());
        Collections.sort(indexes);
        return indexes;
    }
}
