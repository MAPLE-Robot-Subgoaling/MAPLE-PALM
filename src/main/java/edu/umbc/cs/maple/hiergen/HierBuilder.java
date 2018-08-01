package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;

public class HierBuilder {

    public static Set<HierGenTask> run(Map<String, Map<String, VariableTree>> actionModels, List<CATrajectory> cats) {

        // Line 1: G <- GoalCondition(Omega)
        Set<AttributeRelation> goals = CATGoal.determineGoal(cats);

        // Lines 2-3
        if (goals.isEmpty()) { return null; }

        // Line 4
        Map<AttributeRelation, List<SubCAT>> goalToSubcats = new LinkedHashMap<>();

        // Line 5
        for (AttributeRelation relation : goals) {

            // Line 6
            ObjectAttributePair variable = relation.getLeft();
            Set<ObjectAttributePair> wrappedVariable = new LinkedHashSet<>();
            wrappedVariable.add(variable);
            List<SubCAT> subcats = CATScan.scan(cats, wrappedVariable);

            // Line 7
            goalToSubcats.put(relation, subcats);
        }

        // Line 8
        List<HierGenTask> tasks = new ArrayList<>();

        // Line 9
        Map<AttributeRelation, List<SubCAT>> goalToUnifiedSubcats = HierUnify.run(goalToSubcats);



        return null;
    }

}
