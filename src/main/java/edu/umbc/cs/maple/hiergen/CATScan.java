package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.hiergen.CAT.*;
import edu.umbc.cs.maple.utilities.MutableObjectInstance;

import java.util.*;

import static edu.umbc.cs.maple.utilities.BurlapConstants.POINTER_REFERENCE;


public class CATScan {


    // input: a CAT, the relevant variables
    // output: a seeded set of action indexes
    protected static Set<Integer> seedActionIndexes(CATrajectory cat, List<ObjectAttributePair> variables) {
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

    public static List<SubCAT> scan(List<CATrajectory> cats, List<AttributeRelation> relations) {

        System.out.println("CATScan");

        List<ObjectAttributePair> variables = new ArrayList<>();
        for (AttributeRelation relation : relations) {
            variables.add((ObjectAttributePair) relation.getLeft());
        }

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
    public static Set<AttributeRelation> determineGoal(List<CATrajectory> goalCats) {

        Set<String> allChangedVariables = new HashSet<>();
        for (CATrajectory cat : goalCats) {
            Set<String> nontrivialChangedVariables = cat.getNontrivialChangedVariable();
            allChangedVariables.addAll(nontrivialChangedVariables);
        }
        Set<String> objectsWithChangingVariables = new HashSet<>();
        for (String changedVariable : allChangedVariables) {
            String objectName = changedVariable.split(":")[0];
            objectsWithChangingVariables.add(objectName);
        }

        Set<AttributeRelation> globalConstantRelations = new HashSet<>();
        Set<AttributeRelation> globalEqualToRelations = new HashSet<>();
        for (CATrajectory cat : goalCats) {
            Set<AttributeRelation> constantRelations = new HashSet<>();
            Set<AttributeRelation> equalToRelations = new HashSet<>();
            Set<String> nontrivialChangedVariables = cat.getNontrivialChangedVariable();
            OOState ultimateState = (OOState) cat.getUltimateState();
            List<ObjectInstance> objectInstances = ultimateState.objects();
            for (int i = 0; i < objectInstances.size(); i++) {
                ObjectInstance objectInstance = objectInstances.get(i);
                String objectName = objectInstance.name();
                List variableKeys = objectInstance.variableKeys();
                for (Object variableKey : variableKeys) {
                    Object attributeValue = objectInstance.get(variableKey);
                    ObjectAttributePair objectAttribute = new ObjectAttributePair(objectName, variableKey.toString());

                    // now consider the variable as potentially a constant goal predicate
                    String variable = objectName + ":" + variableKey;
                    if (nontrivialChangedVariables.contains(variable)) {
                        RelationConstant constant = new RelationConstant(attributeValue);
                        AttributeRelation equalToConstant = new AttributeRelation(objectAttribute, constant, Relation.EQUAL_TO);
                        constantRelations.add(equalToConstant);
                    }

                    // now consider if the variable is equal to any other variables on other objects
                    // but only if the given object has SOME variable that changed
                    if (objectsWithChangingVariables.contains(objectName)) {
                        for (int j = 0; j < objectInstances.size(); j++) {
                            if (i == j) { continue; }
                            ObjectInstance otherObjectInstance = objectInstances.get(j);
                            String otherObjectName = otherObjectInstance.name();
                            List otherVariableKeys = otherObjectInstance.variableKeys();
                            for (Object otherVariableKey : otherVariableKeys) {
                                Object otherAttributeValue = otherObjectInstance.get(otherVariableKey);
                                if (attributeValue.equals(otherAttributeValue)) {
                                    ObjectAttributePair otherObjectAttribute = new ObjectAttributePair(otherObjectName, otherVariableKey.toString());
                                    equalToRelations.add(new AttributeRelation(objectAttribute, otherObjectAttribute, Relation.EQUAL_TO));
                                }
                            }
                            // special case: also consider the other object's name as a variable
                            if (attributeValue.equals(otherObjectName)) {
                                ObjectAttributePair otherObjectAttribute = new ObjectAttributePair(otherObjectName, "name");
                                equalToRelations.add(new AttributeRelation(objectAttribute, otherObjectAttribute, Relation.EQUAL_TO));

                                // then dereference the object, compare both att_vals
                                for (Object aVariableKey : variableKeys) {
                                    Object aAttributeValue = objectInstance.get(aVariableKey);
                                    ObjectAttributePair aObjectAttribute = new ObjectAttributePair(objectName, aVariableKey.toString());
                                    for (Object bVariableKey : variableKeys) {
                                        Object bAttributeValue = otherObjectInstance.get(bVariableKey);
                                        if (aAttributeValue.equals(bAttributeValue)) {
                                            String referencedName = variableKey.toString();
                                            referencedName = POINTER_REFERENCE + referencedName;
                                            ObjectAttributePair bObjectAttribute = new ObjectAttributePair(referencedName, bVariableKey.toString());
                                            equalToRelations.add(new AttributeRelation(aObjectAttribute, bObjectAttribute, Relation.EQUAL_TO));
                                        }
                                    }
                                }

                            }
                        }
                    }

                }
            }
            if (globalConstantRelations.isEmpty()) {
                globalConstantRelations.addAll(constantRelations);
            } else {
                globalConstantRelations.retainAll(constantRelations);
            }
            if (globalEqualToRelations.isEmpty()) {
                globalEqualToRelations.addAll(equalToRelations);
            } else {
                globalEqualToRelations.retainAll(equalToRelations);
            }
        }

        System.out.println("****");
        for (AttributeRelation relation : globalConstantRelations) {
            System.out.println(relation);
        }
        System.out.println("****");
        for (AttributeRelation relation : globalEqualToRelations) {
            System.out.println(relation);
        }

        Set<AttributeRelation> globalRelations = new HashSet<>();
        globalRelations.addAll(globalConstantRelations);
        globalRelations.addAll(globalEqualToRelations);
        return globalRelations;
    }

    public static void prescan(List<CATrajectory> cats, Set<AttributeRelation> goals) {
        List<List<SubCAT>> listOfSubCATs = new ArrayList<>();
        for (AttributeRelation relation : goals) {
            List<AttributeRelation> wrappedRelation = new ArrayList<>();
            wrappedRelation.add(relation);
            List<SubCAT> subCATs = CATScan.scan(cats, wrappedRelation);
            listOfSubCATs.add(subCATs);
        }

    }
}
