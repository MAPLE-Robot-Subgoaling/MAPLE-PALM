package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;


public class CATScan {

    // input: a CAT, the relevant variables
    // output: a seeded set of action indexes
    protected static Set<Integer> seedActionIndexes(CATrajectory cat, List<OOVariableKey> variables) {
        Set<Integer> actionIndexes = new HashSet<>();
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

    public static List<SubCAT> scan(ArrayList<CATrajectory> cats, List<OOVariableKey> variables) {

        System.out.println("CATScan");

        List<SubCAT> subCATs = new ArrayList<SubCAT>();
        for (CATrajectory cat : cats) {

            Set<Integer> actionIndexes = seedActionIndexes(cat, variables);

            actionIndexes = computeActionIndexes(cat, actionIndexes);

            // enforce unique preconditions (line 8 in CAT-Scan)
            actionIndexes = enforceUniquePreconditions(cat, actionIndexes);

        }

        return subCATs;
    }

    public static Map<OOVariableKey, Object> determineGoal(List<CATrajectory> CATrajectories) {
        CATrajectory firstCAT = CATrajectories.get(0);
        State firstState = firstCAT.getBaseTrajectory().state(0);
        List<OOVariableKey> variables = (List<OOVariableKey>) (List<?>) firstState.variableKeys();
        Map<OOVariableKey, Object> goal = new HashMap<>();
        for (OOVariableKey variable : variables) {
            Object attributeValue;
            Episode baseTrajectory = firstCAT.getBaseTrajectory();
            int lastStateIndex = -99;//subOfFirstCAT != null ? subOfFirstCAT.getEnd() - 1 : baseTrajectory.stateSequence.size() - 1;
            State lastState = baseTrajectory.state(lastStateIndex);
            attributeValue = lastState.get(variable);
            goal.put(variable, attributeValue);
        }
        for (CATrajectory cat : CATrajectories) {
            List<OOVariableKey> keysToRemove = new ArrayList<>();
            for (OOVariableKey key : goal.keySet()) {
                Object attributeValue;
                Episode baseTrajectory = cat.getBaseTrajectory();
                int lastStateIndex = -99;//sub != null ? cat.getSub().getEnd() - 1 : cat.getBaseTrajectory().stateSequence.size() - 1;
                attributeValue = baseTrajectory.state(lastStateIndex).get(key);
                if (!attributeValue.equals(goal.get(key))) {
                    keysToRemove.add(key);
                }
            }

            for (OOVariableKey keyToRemove : keysToRemove) {
                goal.remove(keyToRemove);
            }
        }
        return goal;
    }

    public static void test(List<CATrajectory> cats) {
        Map<OOVariableKey, Object> goal = determineGoal(cats);

        ArrayList<HierGenTask> finalTasks = new ArrayList<>();
        if (goal.isEmpty()) {
            return;
        }

//        List<List<SubCAT>> listOfSubCATs = new ArrayList<>();
//        for (OOVariableKey relevantVariable : goal.keySet()) {
//            ArrayList<OOVariableKey> currentVariables = new ArrayList<>();
//            currentVariables.add(relevantVariable);
//            List<SubCAT> subCATs = CATScan.scan(cat, currentVariables);
//            listOfSubCATs.add(subCATs);
//        }

    }
}
