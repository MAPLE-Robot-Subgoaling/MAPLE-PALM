package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.AttributeRelation;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.ObjectAttributePair;
import edu.umbc.cs.maple.hiergen.CAT.SubCAT;

import java.util.*;

public class HierBuilder {

    public static void start(List<CATrajectory> cats) {
        Set<AttributeRelation> goals = CATGoal.determineGoal(cats);
        Map<ObjectAttributePair, List<SubCAT>> setOfSubCATs = new HashMap<>();
        for (AttributeRelation relation : goals) {
            ObjectAttributePair variable = relation.getLeft();
            Set<ObjectAttributePair> wrappedVariable = new LinkedHashSet<>();
            wrappedVariable.add(variable);
            List<SubCAT> subCATs = CATScan.scan(cats, wrappedVariable);
            setOfSubCATs.put(relation.getLeft(), subCATs);
        }
        int counter = 0;
        for (ObjectAttributePair p : setOfSubCATs.keySet()) {
            List<SubCAT> list = setOfSubCATs.get(p);
            for (SubCAT subCAT : list) {
                System.out.println(counter + " " + p + " " + subCAT.getActionIndexes());
                counter++;
            }
        }
    }

}
