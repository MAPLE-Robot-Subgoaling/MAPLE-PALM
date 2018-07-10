package edu.umbc.cs.maple.hiergen;

import burlap.mdp.core.oo.state.OOVariableKey;
import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.ArrayList;
import java.util.List;


public class CATScan {

    // input: a CAT, the relevant variables
    // output: a seeded set of action indexes
    public static List<Integer> seedActionIndexes(CATrajectory cat, List<OOVariableKey> variables) {
        ArrayList<Integer> actionIndexes = new ArrayList<>();
        List<CausalEdge> edges = cat.getEdges();
        for (Object variable : variables) {

            String variableName = variable.toString();

            for (CausalEdge edge : edges) {

                // skip the START pseudoaction (shouold always be at index 0)
                if (edge.getStart() == cat.getStartIndex()) {
                    continue;
                }

                // skip the edge if its relevant variable is not this one
                if (!edge.getRelevantVariable().equals(variableName)) {
                    continue;
                }

                // skip if already in the actionIndexes
                if (actionIndexes.contains(edge.getStart())) {
                    continue;
                }

                // the edge is relevant and goes to END
                if (edge.getEnd() == cat.getEndIndex()) {
                    actionIndexes.add(edge.getStart());
                }

            }
        }
        return actionIndexes;
    }

    public static List<Integer> computeActionIndexes(CATrajectory cat, List<OOVariableKey> variables, List<Integer> actionIndexes) {
        String[] actions = cat.getActions();

        // check all actions with outgoing edges to the subCAT
        // only have outgoing edges maintained in the subCAT
        do {
            for (int i = 0; i < actions.length; i++) {
                boolean contained = false;
                if (!(actionIndexes.contains(i))) {
                    List<Integer> nextEdges = cat.findEdges(i);
                    if (nextEdges != null) {
                        contained = true;
                        for (Integer e : nextEdges) {
                            if (!actionIndexes.contains(e)) {
                                contained = false;
                                break;
                            }
                        }
                    }
                }
                if (contained) {
                    actionIndexes.add(i);
                    if (i < start) {
                        start = i;
                    }
                }
            }
        } while (actionIndexes.size() != prevSize);

    }

    public static List<SubCAT> scan(ArrayList<CATrajectory> cats, List<OOVariableKey> variables) {

        System.out.println("CATScan");

        List<SubCAT> subCATs = new ArrayList<SubCAT>();
        for (CATrajectory cat : cats) {

            List<Integer> actionIndexes = seedActionIndexes(cat, variables);

            actionIndexes = computeActionIndexes(cat, variables, actionIndexes);

            if (start != 0 || end != 0) {
                SubCAT subCAT = new SubCAT(start, end, actionIndexes, variables, cat);
                subCATs.add(subCAT);
            }
        }

        return subCATs;
    }

//    public static SubCAT scan(CATrajectory trajectory, List<OOVariableKey> variables) {
//        ArrayList<CATrajectory> cats = new ArrayList<>();
//        cats.add(trajectory);
//        List<SubCAT> temp = scan(cats, variables);
//        if (!temp.isEmpty()) {
//            return temp.get(0);
//        }
//
//        return null;
//
//    }


}
