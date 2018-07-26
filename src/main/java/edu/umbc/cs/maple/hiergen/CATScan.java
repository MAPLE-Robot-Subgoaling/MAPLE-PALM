package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;


public class CATScan {

    public static int DEBUG_CODE_CATSCAN = 12013123;

    // input: a CAT, the relevant variables
    // output: a seeded set of action indexes
    protected static Set<Integer> seedActionIndexes(CATrajectory cat, Collection<ObjectAttributePair> variables) {
        int catStart = cat.getStartIndex();
        int catEnd = cat.getEndIndex();
        Set<Integer> actionIndexes = new TreeSet<>();
        List<CausalEdge> edges = cat.getEdges();
        for (ObjectAttributePair variable : variables) {

            String variableName = variable.toString();

            for (CausalEdge edge : edges) {

                int edgeStart = edge.getStart();
                int edgeEnd = edge.getEnd();
                String relevantVariable = edge.getRelevantVariable();

                // skip the START pseudoaction (should always be at index 0)
                if (catStart == edgeStart) {
                    continue;
                }

                // skip the edge if its relevant variable is not this one
                if (!relevantVariable.equals(variableName)) {
                    continue;
                }

                // skip if already in the actionIndexes
                if (actionIndexes.contains(edgeStart)) {
                    continue;
                }

                // the edge is relevant and goes to END
                if (catEnd == edgeEnd) {
                    actionIndexes.add(edge.getStart());
                }

            }
        }
        return actionIndexes;
    }

    protected static Set<Integer> computeActionIndexes(CATrajectory cat, Set<Integer> actionIndexes) {
        String[] actions = cat.getActions();
        int prevSize;
        do {
            prevSize = actionIndexes.size();
            for (int i = 0; i < actions.length; i++) {
                boolean connectedInto = isConnectedToKnownAction(cat, actionIndexes, i);
                boolean connectedOutOf = isConnectedToUnknownAction(cat, actionIndexes, i);
                if (connectedInto && !connectedOutOf) {
                    actionIndexes.add(i);
                }
            }
        } while (actionIndexes.size() != prevSize);
        return actionIndexes;
    }

    protected static boolean isConnectedToKnownAction(CATrajectory cat, Set<Integer> actionIndexes, int iIndex) {
        boolean connected = false;
        List<Integer> nextEdges = cat.findEdges(iIndex);
        if (nextEdges == null) {
            return connected;
        }
        for (Integer jIndex : nextEdges) {
            if (actionIndexes.contains(jIndex)) {
                // if there exists some action index, jIndex, in the known set to which
                // iIndex is connected, then it is true
                connected = true;
                break;
            }
        }
        return connected;

    }

    protected static Set<Integer> getUnknownActionIndexes(CATrajectory cat, Set<Integer> knownActionIndexes) {
        Set<Integer> unknownActionIndexes = new HashSet<>();
        for (int i = 0; i < cat.getActions().length; i++) {
            if (!knownActionIndexes.contains(i)) { unknownActionIndexes.add(i); }
        }
        return unknownActionIndexes;
    }

    protected static boolean isConnectedToUnknownAction(CATrajectory cat, Set<Integer> actionIndexes, int iIndex) {
        Set<Integer> unknownActionIndexes = getUnknownActionIndexes(cat, actionIndexes);
        boolean connected = false;
        List<Integer> nextEdges = cat.findEdges(iIndex);
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

    public static Set<Integer> enforceUniquePreconditions(CATrajectory cat, Set<Integer> actionIndexes) {
        return actionIndexes;
    }

    public static List<SubCAT> scan(List<CATrajectory> cats, Collection<ObjectAttributePair> variables) {

        System.out.println("CATScan");


        List<SubCAT> subCATs = new ArrayList<>();
        for (CATrajectory cat : cats) {

            Set<Integer> actionIndexes = seedActionIndexes(cat, variables);

            actionIndexes = computeActionIndexes(cat, actionIndexes);

//             enforce unique preconditions (line 8 in CAT-Scan)
            actionIndexes = enforceUniquePreconditions(cat, actionIndexes);

            SubCAT subCAT = new SubCAT(actionIndexes);
            subCATs.add(subCAT);

        }

        return subCATs;
    }


}
