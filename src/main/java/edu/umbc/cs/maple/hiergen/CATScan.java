package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;


public class CATScan {

    public static int DEBUG_CODE_CATSCAN = 12013123;

    // input: a CAT, the relevant variables
    // output: initial subCATs
    protected static SubCAT seedSubCAT(CATrajectory cat, Collection<ObjectAttributePair> variables) {
        int catStart = cat.getStartIndex();
        int catEnd = cat.getEndIndex();
//        Set<Integer> actionIndexes = new TreeSet<>();
        SubCAT subcat = new SubCAT(cat);
        Set<CausalEdge> edges = cat.getEdges();
        for (ObjectAttributePair variable : variables) {

            String variableName = variable.toString();

            for (CausalEdge edge : edges) {

                int edgeEnd = edge.getEnd();
                // skip if it does not connect to END of CAT
                if (edgeEnd != catEnd) {
                    continue;
                }

                int edgeStart = edge.getStart();
                // skip if trivial, meaning edge is at START of CAT
                if (edgeStart == catStart) {
                    continue;
                }

                String relevantVariable = edge.getRelevantVariable();
                // if its relevant variable is this one, and not already recorded: add it
                if (relevantVariable.equals(variableName) && !subcat.contains(edgeStart)) {
                    subcat.add(edgeStart);
                }

            }
        }
        return subcat;
    }

    protected static void assembleSubCAT(SubCAT subcat) {
        String[] actions = subcat.getCat().getActions();
        int prevSize;
        do {
            prevSize = subcat.size();
            for (int i = 0; i < actions.length; i++) {
                boolean connectedInto = isConnectedToKnownAction(subcat, i);
                boolean connectedOutOf = isConnectedToUnknownAction(subcat, i);
                if (connectedInto && !connectedOutOf) {
                    subcat.add(i);
                    if (subcat.getCat().getActions()[i].contains("pick")) {
                        connectedInto = isConnectedToKnownAction(subcat, i);
                        connectedOutOf = isConnectedToUnknownAction(subcat, i);
                        System.out.println(connectedInto + " " + connectedOutOf);
                    }
                }
            }
        } while (subcat.size() != prevSize);
    }

    protected static boolean isConnectedToKnownAction(SubCAT subcat, int iIndex) {
        CATrajectory cat = subcat.getCat();
        boolean connected = false;
        List<CausalEdge> nextEdges = cat.findOutgoingEdges(iIndex);
        if (nextEdges == null) {
            return connected;
        }
        for (CausalEdge edge : nextEdges) {
            int jIndex = edge.getEnd();
            if (subcat.contains(jIndex)) {
                // if there exists some action index, jIndex, in the known set to which
                // iIndex is connected, then it is true
                connected = true;
                break;
            }
        }
        return connected;

    }

    protected static Set<Integer> getUnknownActionIndexes(SubCAT subcat) {
        Set<Integer> unknownActionIndexes = new HashSet<>();
        CATrajectory cat = subcat.getCat();
        for (int i = 0; i < cat.getActions().length; i++) {
            if (!subcat.contains(i)) { unknownActionIndexes.add(i); }
        }
        return unknownActionIndexes;
    }

    protected static boolean isConnectedToUnknownAction(SubCAT subcat, int iIndex) {
        Set<Integer> unknownActionIndexes = getUnknownActionIndexes(subcat);
        boolean connected = false;
        CATrajectory cat = subcat.getCat();
        List<CausalEdge> nextEdges = cat.findEdges(iIndex);
        if (nextEdges == null) {
            return connected;
        }
        for (Integer jIndex : nextEdges) {
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
        CATrajectory cat = subcat.getCat();
        for (Integer index : subcat.getActionIndexes()) {
            System.out.println(index + " " + cat.getActions()[index]);
            List<CausalEdge> edges = cat.findIncomingEdges(index);
            if (edges != null) {
                for (CausalEdge edge : edges) {
                    System.out.println("\t"+edge.getRelevantVariable());
                }
            } else {
                System.out.println("no edge");
            }
        }
    }

    public static List<SubCAT> scan(List<CATrajectory> cats, Collection<ObjectAttributePair> variables) {

        System.out.println("CATScan");

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
