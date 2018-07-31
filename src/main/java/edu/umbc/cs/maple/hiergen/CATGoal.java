package edu.umbc.cs.maple.hiergen;

import burlap.debugtools.DPrint;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.umbc.cs.maple.utilities.BurlapConstants.POINTER_REFERENCE;

public class CATGoal {

    // the input CATs *must* be successful trajectories, meaning their final state is a goal state
    public static Set<AttributeRelation> determineGoal(List<CATrajectory> goalCats) {
        System.out.println("CATScan: Determine Goal");

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
                        AttributeRelation equalToConstant = new AttributeRelation(objectAttribute, constant, RelationType.EQUAL_TO);
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
                                    equalToRelations.add(new AttributeRelation(objectAttribute, otherObjectAttribute, RelationType.EQUAL_TO));
                                }
                            }
                            // special case: also consider the other object's name as a variable
                            if (attributeValue.equals(otherObjectName)) {
                                ObjectAttributePair otherObjectAttribute = new ObjectAttributePair(otherObjectName, "name");
                                equalToRelations.add(new AttributeRelation(objectAttribute, otherObjectAttribute, RelationType.EQUAL_TO));

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
                                            equalToRelations.add(new AttributeRelation(aObjectAttribute, bObjectAttribute, RelationType.EQUAL_TO));
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
}
