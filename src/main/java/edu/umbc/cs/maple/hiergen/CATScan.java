package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
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

    // the input CATs *must* be successful trajectories, meaning their final state is a goal state
    public static Map<OOVariableKey, Object> determineGoal(List<CATrajectory> goalCats) {

        Map<OOVariableKey, Object> globalPredicates = new HashMap<>();
        Set<AttributeRelation> globalRelations = new HashSet<>();
        for (CATrajectory cat : goalCats) {
            Map<OOVariableKey, Object> constantPredicates = new HashMap<>();
            Set<AttributeRelation> equalToRelations = new HashSet<>();
            Set<String> nontrivialChangedVariables = cat.getNontrivialChangedVariable();
            OOState ultimateState = (OOState) cat.getUltimateState();
            List<ObjectInstance> objectInstances = ultimateState.objects();
            for (int i = 0; i < objectInstances.size(); i++) {
                ObjectInstance objectInstance = objectInstances.get(i);
                String objectName = objectInstance.name();
                List<Object> variableKeys = (List) objectInstance.variableKeys();
                for (int v = 0; v < variableKeys.size(); v++) {
                    Object variableKey = variableKeys.get(v);
                    Object attributeValue = objectInstance.get(variableKey);
                    OOVariableKey ooVariableKey = new OOVariableKey(objectName, variableKey);

                    // now consider the variable as potentially a constant goal predicate
                    String variable = objectName + ":" + variableKey;
                    if (nontrivialChangedVariables.contains(variable)) {
                        System.out.println(variable + " : " + attributeValue);
                        constantPredicates.put(ooVariableKey, attributeValue);
                    }

                    // now consider if the variable is equal to any other variables on other objects
                    for (int j = i+1; j < objectInstances.size(); j++) {
                        ObjectInstance otherObjectInstance = objectInstances.get(j);
                        String otherObjectName = otherObjectInstance.name();
                        List<Object> otherVariableKeys = (List) otherObjectInstance.variableKeys();

                        for (int w = 0; w < otherVariableKeys.size(); w++) {
                            Object otherVariableKey = otherVariableKeys.get(w);
                            Object otherAttributeValue = otherObjectInstance.get(otherVariableKey);
                            if (attributeValue.equals(otherAttributeValue)) {
                                OOVariableKey otherOOVariableKey = new OOVariableKey(otherObjectName, otherVariableKey);
                                equalToRelations.add(new AttributeRelation(ooVariableKey, otherOOVariableKey, Relation.EQUAL_TO));
                            }
                        }
                    }
                }
            }
            if (globalPredicates.isEmpty()) {
                globalPredicates.putAll(constantPredicates);
            } else {
                // keep only the objectName:attributeName:attributeValue that are constant across all goal states
                globalPredicates.keySet().retainAll(constantPredicates.keySet());
            }
            globalRelations.addAll(equalToRelations);
        }

        return null;

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
