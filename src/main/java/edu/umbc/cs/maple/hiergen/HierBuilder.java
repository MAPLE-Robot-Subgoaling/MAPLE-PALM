package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.*;
import java.util.stream.Collectors;

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
        List<SubCAT> unifiedSubcats = HierBuilderUnify.run(goalToSubcats);

        // Line 10
        if (unifiedSubcats.size() > 0) {

            //Line 11
//            goalToSubcats = goalToUnifiedSubcats;

            // Line 12 (inner step, invert all subcats)
//            List<SubCAT> subcats = goalToUnifiedSubcats
//                    .entrySet()
//                    .stream()
//                    .map(Map.Entry::getValue)
//                    .collect(Collectors.toList())
//                    .stream()
//                    .flatMap(List::stream)
//                    .collect(Collectors.toList());
            List<InvertedSubCAT> invertedSubcats = InvertedSubCAT.create(unifiedSubcats);

            // Line 12 (inner step, extract preceding CATs)
            List<CATrajectory> extractedCats = HierGenExtract.run(cats, invertedSubcats);

            // Line 12
            Set<HierGenTask> taskSetQ = HierBuilder.run(actionModels, extractedCats);

        } else {
            System.err.println("Debug: no unified subcats!");
        }


        return null;
    }

}
