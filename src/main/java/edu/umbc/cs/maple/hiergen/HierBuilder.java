package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.AttributeRelation;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.ObjectAttributePair;
import edu.umbc.cs.maple.hiergen.CAT.SubCAT;

import java.util.*;

public class HierBuilder {

    public static void start(List<CATrajectory> cats) {
        Set<AttributeRelation> goals = CATGoal.determineGoal(cats);
        Map<AttributeRelation, List<SubCAT>> container = new HashMap<>();
        for (AttributeRelation relation : goals) {
            ObjectAttributePair variable = relation.getLeft();
            Set<ObjectAttributePair> wrappedVariable = new LinkedHashSet<>();
            wrappedVariable.add(variable);
            List<SubCAT> subCATs = CATScan.scan(cats, wrappedVariable);
            container.put(relation, subCATs);
        }
        int counter = 0;
        for (AttributeRelation relation : container.keySet()) {
            List<SubCAT> list = container.get(relation);
            for (SubCAT subCAT : list) {
                System.out.println(counter + " " + relation + " " + subCAT.getActionIndexes());
                counter++;
            }
        }
    }

}
