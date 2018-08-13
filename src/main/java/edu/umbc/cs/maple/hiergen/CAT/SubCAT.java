package edu.umbc.cs.maple.hiergen.CAT;

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
            String message = messages.get(i);
            if (message == null || prunedIndexes.contains(i)) { continue; }
            if (!first) { sb.append(", "); } else { first = false; }
            sb.append(i);
            sb.append("(");
            sb.append(cat.getActions().get(i));
            sb.append(") ");
            sb.append(message);
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

    public boolean includesVariables(Collection<ObjectAttributePair> variables) {
        return this.variables.containsAll(variables);
    }

    public boolean isValid() {

        // warning: this method is inefficient (for now)
        System.err.println("isValid() is not efficient -- consider disabling");

        // 1. there is at most one outgoing arc for every variable
        if (!atMostOneOutgoingPerVariable()) {
            return false;
        }

        // 2. all incoming arcs for a variable emanate from the same action (index) in the CAT
        if (getInvalidIndexes().size() > 0) {
            return false;
        }

        // 3. every member action, besides ones from which the outgoing arcs emanate,
        //    must have an arc to another member action, and *only* to member actions
        List<CausalEdge> outgoingEdges = getOutgoing();
        List<Integer> invalidIndexes = actionIndexes
        .stream()
        .filter(i -> {
            // must not be the start of an outgoing edge for this subcat
            for (CausalEdge edge : outgoingEdges) {
                if (edge.start == i) {
                    // we can ignore this since
                    return false;
                }
            }
            // and must
            List<CausalEdge> edges = cat.findOutgoingEdges(i);
            for (CausalEdge edge : edges) {
                int end = edge.end;
                if (!actionIndexes.contains(end)) {
                    // has its own outgoing edge that does not end inside the subcat
                    return true;
                }
            }
            return false;
        })
        .collect(Collectors.toList());
        if (invalidIndexes.size() > 0) {
            return false;
        }


        System.err.println("WARNING: need to finish isValid()");

        return true;
    }

    public boolean atMostOneOutgoingPerVariable() {
        for (ObjectAttributePair variable : variables) {
            String variableString = variable.toString();
            int outgoingCount = 0;
            for (CausalEdge outgoing : getOutgoing()) {
                String outgoingVariableString = outgoing.relevantVariable;
                if (variableString.equals(outgoingVariableString)) {
                    outgoingCount += 1;
                    if (outgoingCount > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Map<String,List<Integer>> getInvalidIndexes() {
        Map<String, List<Integer>> possiblePruning = new HashMap<>();
        List<CausalEdge> incomingEdges = getIncoming();
        for (int i = 0; i < incomingEdges.size(); i++) {
            CausalEdge incomingEdge = incomingEdges.get(i);
            String incomingVariable = incomingEdge.getRelevantVariable();
            int incomingEdgeStart = incomingEdge.getStart();
            for (int j = i + 1; j < incomingEdges.size(); j++) {
                CausalEdge otherEdge = incomingEdges.get(j);
                String otherVariable = otherEdge.getRelevantVariable();
                if (incomingVariable.equals(otherVariable)) {
                    int otherEdgeStart = otherEdge.getStart();
                    if (incomingEdgeStart != otherEdgeStart) {
                        // this means two incoming arcs for the same variable
                        // come from different causal actions (at different indexes)
                        // this possibly violates the "unique precondition" requirement
                        List<Integer> actionIndexes = possiblePruning.computeIfAbsent(incomingVariable, v -> new ArrayList<>());
                        actionIndexes.add(incomingEdgeStart);
                        actionIndexes.add(otherEdgeStart);
//                        if (incomingVariable.contains("Passenger1:y") && subcat.getCat().getName().contains("19")) {
//                            System.out.println("For subcat ... " + subcat);
//                            System.out.println("considering to prune " + incomingEdge + " because other edge " + otherEdge + " had same variable " + incomingVariable);
//                        }
                    }
                }
            }
        }
        return possiblePruning;
    }
}
