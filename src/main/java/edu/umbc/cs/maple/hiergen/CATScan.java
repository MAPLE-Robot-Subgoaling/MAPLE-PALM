package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;

import static edu.umbc.cs.maple.hiergen.CAT.SubCAT.SUBCAT_ID;


public class CATScan {


    // input: a CAT, the relevant variables
    // output: initial subCATs
    protected static SubCAT seedSubCAT(CATrajectory cat, Collection<ObjectAttributePair> variables) {
        int catStart = cat.getStartIndex();
        int catEnd = cat.getEndIndex();
        String subcatName = cat.getName() + "_subcat_" + SUBCAT_ID++;
        SubCAT subcat = new SubCAT(subcatName, cat, variables);
        Set<CausalEdge> edges = cat.getEdges();
        for (ObjectAttributePair variable : variables) {
            String variableName = variable.toString();
            for (CausalEdge edge : edges) {

                // skip edge if it does not connect to END of CAT
                int edgeEnd = edge.getEnd();
                if (edgeEnd != catEnd) { continue; }

                // skip edge if trivial, meaning it starts at the START of the CAT
                int edgeStart = edge.getStart();
                if (edgeStart == catStart) { continue; }

                // if edge's relevant variable is this one, and not already recorded: add it
                String relevantVariable = edge.getRelevantVariable();
                if (relevantVariable.equals(variableName) && !subcat.contains(edgeStart)) {
                    subcat.add(edgeStart, "-> END");
                }

            }
        }
        return subcat;
    }

    protected static void assembleSubCAT(SubCAT subcat) {
        List<Integer> indexes = subcat.getSortedIndexes();
        int prevSize;
        do {
            prevSize = subcat.size();
            for (int index : indexes) {
                boolean connectedInto = isConnectedToKnownActionIndex(subcat, index);
                boolean connectedOutOf = isConnectedToUnknownActionIndex(subcat, index);
                if (connectedInto && !connectedOutOf) {
                    subcat.add(index, "-> " + getSomeConnectedKnownActionIndex(subcat, index));
                }
            }
        } while (subcat.size() != prevSize);
    }

    protected static boolean isConnectedToKnownActionIndex(SubCAT subcat, int iIndex) {
        return getSomeConnectedKnownActionIndex(subcat, iIndex) != null;
    }

    protected static Integer getSomeConnectedKnownActionIndex(SubCAT subcat, int iIndex) {
        CATrajectory cat = subcat.getCat();
        List<CausalEdge> nextEdges = cat.findOutgoingEdges(iIndex);
        if (nextEdges == null) {
            return null;
        }
        for (CausalEdge edge : nextEdges) {
            int jIndex = edge.getEnd();
            if (subcat.contains(jIndex)) {
                // if there exists some action index, jIndex, in the known set to which
                // iIndex is connected, then it is true
                return jIndex;
            }
        }
        return null;

    }

    protected static Set<Integer> getUnknownActionIndexes(SubCAT subcat) {
        Set<Integer> unknownActionIndexes = new LinkedHashSet<>();
        List<Integer> indexes = subcat.getSortedIndexes();
        for (int index : indexes) {
            if (!subcat.contains(index)) { unknownActionIndexes.add(index); }
        }
        return unknownActionIndexes;
    }

    protected static boolean isConnectedToUnknownActionIndex(SubCAT subcat, int iIndex) {
        Set<Integer> unknownActionIndexes = getUnknownActionIndexes(subcat);
        boolean connected = false;
        CATrajectory cat = subcat.getCat();
        List<CausalEdge> nextEdges = cat.findOutgoingEdges(iIndex);
        if (nextEdges == null) {
            return connected;
        }
        for (CausalEdge edge : nextEdges) {
            Integer jIndex = edge.getEnd();
            if (unknownActionIndexes.contains(jIndex)) {
                // there exists some action not in the known set of action indexes to which
                // iIndex is connected
                connected = true;
                break;
            }
        }
        return connected;
    }

    public static void enforceUniquePreconditions(SubCAT subcat) {
        // all incoming arcs labeled with a particular variable v come from the same causal action in the CAT

//        System.out.println(subcat);

//        System.out.println("incoming...");
        Map<String, List<Integer>> possiblePruning = new HashMap<>();
        List<CausalEdge> incomingEdges = subcat.getIncoming();
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
//                        int incomingEdgeEnd = incomingEdge.getEnd();
//                        int otherEdgeEnd = otherEdge.getEnd();
                        List<Integer> actionIndexes = possiblePruning.computeIfAbsent(incomingVariable, v -> new ArrayList<>());
                        actionIndexes.add(incomingEdgeStart);
                        actionIndexes.add(otherEdgeStart);
//                        int lowerIndex = Math.min(incomingEdgeStart, otherEdgeStart);
//                        actionIndexes.add(lowerIndex);
//                        actionIndexes.add(otherEdgeEnd);
//                        if (incomingVariable.contains("Passenger1:y") && subcat.getCat().getName().contains("19")) {
//                            System.out.println("For subcat ... " + subcat);
//                            System.out.println("considering to prune " + incomingEdge + " because other edge " + otherEdge + " had same variable " + incomingVariable);
//                        }
                    }
                }
            }
        }
        for (String variable : possiblePruning.keySet()) {
            List<Integer> actionIndexes = possiblePruning.get(variable);
            Collections.sort(actionIndexes);
            // prune to highest index
            while (actionIndexes.size() > 1) {
                int index = actionIndexes.remove(0);
                subcat.prune(index, "enforcing preconditions");
            }
        }
    }

    public static List<SubCAT> scan(List<CATrajectory> cats, Collection<ObjectAttributePair> variables) {

        System.out.println("CATScan  ");

        List<SubCAT> subCATs = new ArrayList<>();
        for (CATrajectory cat : cats) {

            SubCAT subcat = seedSubCAT(cat, variables);

            assembleSubCAT(subcat);

            enforceUniquePreconditions(subcat);

            subCATs.add(subcat);

        }

        return subCATs;
    }


}
