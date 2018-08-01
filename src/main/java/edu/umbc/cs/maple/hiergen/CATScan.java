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
        String[] actions = subcat.getCat().getActions();
        int prevSize;
        do {
            prevSize = subcat.size();
            for (int i = 0; i < actions.length; i++) {
                boolean connectedInto = isConnectedToKnownActionIndex(subcat, i);
                boolean connectedOutOf = isConnectedToUnknownActionIndex(subcat, i);
                if (connectedInto && !connectedOutOf) {
                    subcat.add(i, "-> " + getSomeConnectedKnownActionIndex(subcat, i));
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
        CATrajectory cat = subcat.getCat();
        for (int i = 0; i < cat.getActions().length; i++) {
            if (!subcat.contains(i)) { unknownActionIndexes.add(i); }
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

        Map<String, List<CausalEdge>> variableToEdges = new LinkedHashMap<>();

//        System.out.println("incoming...");
        List<CausalEdge> incoming = subcat.getIncoming();
        for (CausalEdge edge : incoming) {
//            System.out.println(edge + " " + subcat.getCat().getActions()[edge.getStart()] + " " + subcat.getCat().getActions()[edge.getEnd()]);
            String variable = edge.getRelevantVariable();
            List<CausalEdge> existing = variableToEdges.computeIfAbsent(variable, i -> new ArrayList<>());
            for (CausalEdge existingEdge : existing) {
                int existingEdgeStart = existingEdge.getStart();
                int edgeStart = edge.getStart();
                if (existingEdgeStart != edgeStart) {
                    int edgeEnd = edge.getEnd();
                    subcat.prune(edgeEnd, "enforcing preconditions");
                    break;
                }
            }
            existing.add(edge);

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
