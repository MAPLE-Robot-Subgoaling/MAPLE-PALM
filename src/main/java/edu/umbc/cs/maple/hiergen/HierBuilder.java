package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;
import java.util.stream.Collectors;

public class HierBuilder {

    public static List<HierGenTask> run(Map<String, Map<String, VariableTree>> actionModels, List<CATrajectory> cats) {

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
        List<SubCAT> unifiedSubcats = HierBuilderUnify.run(goalToSubcats);

        List<List<SubCAT>> wrappedUnifiedSubcats = unifiedSubcats.stream().map(i -> {
            List<SubCAT> wrapper = new ArrayList<>();
            wrapper.add(i);
            return wrapper;
        }).collect(Collectors.toList());

        // the new Psi
        List<List<SubCAT>> leftoverSubcatSets = null;

        // Line 10
        if (wrappedUnifiedSubcats.size() > 0) {

            // Line 11
            leftoverSubcatSets = wrappedUnifiedSubcats;

            // Line 12
            for (List<SubCAT> wrappedUnifiedSubcat : wrappedUnifiedSubcats) {

                // Line 13 (inner step, invert all subcats)
                List<InvertedSubCAT> invertedSubcats = InvertedSubCAT.create(wrappedUnifiedSubcat);

                // Line 13 (inner step, extract preceding CATs)
                List<CATrajectory> extractedInvertedCats = HierGenExtract.run(cats, invertedSubcats);

                // Line 13
                List<HierGenTask> taskSetQ = HierBuilder.run(actionModels, extractedInvertedCats);

                // Line 14
//                if (taskSetQ != null && taskSetQ.size() > 0) {
//                    Object xgc = HierGen.run(actionModels, )
//                }
            }



        } else {
            System.err.println("Debug: no unified subcats!");
        }


        return tasks;
    }

}
