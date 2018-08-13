package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.AttributeRelation;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.ObjectAttributePair;
import edu.umbc.cs.maple.hiergen.CAT.SubCAT;

import java.util.*;

import static edu.umbc.cs.maple.hiergen.CAT.SubCAT.SUBCAT_ID;

public class HierBuilderUnify {

    public static List<SubCAT> run(Map<ObjectAttributePair, List<SubCAT>> variableToSubcats) {

        List<ObjectAttributePair> partition = new ArrayList<>();
        // regroup by CAT
        Map<String, List<SubCAT>> catNameToSubcats = new LinkedHashMap<>();
        for (ObjectAttributePair variable : variableToSubcats.keySet()) {
            List<SubCAT> subcats = variableToSubcats.get(variable);
            if (subcats == null) {
                System.out.println(variable.toString() + " had no subcats");
                continue;
            } else {
                partition.add(variable);
            }
            for (SubCAT subcat : subcats) {
                CATrajectory cat = subcat.getCat();
                String catName = cat.getName();
                List<SubCAT> catSubcats = catNameToSubcats.computeIfAbsent(catName, i -> new ArrayList<>());
                catSubcats.add(subcat);
            }
        }

        List<SubCAT> possibleSubcats = new ArrayList<>();
        for (String catName : catNameToSubcats.keySet()) {
            System.out.println("\n" + catName);
            List<SubCAT> subcats = catNameToSubcats.get(catName);
            CATrajectory cat = subcats.get(0).getCat();
            String subcatName = cat.getName() + "_subcat_" + SUBCAT_ID++ + "_unified";
            SubCAT unified = new SubCAT(subcatName, cat, new LinkedHashSet<>());
            for (SubCAT subcat : subcats) {
                System.out.println("\t"+subcat);
                unified.unify(subcat);
            }
            System.out.println(unified);
            possibleSubcats.add(unified);
        }

        List<SubCAT> unifiedSubcats = new ArrayList<>();
        for (SubCAT subcat : possibleSubcats) {
            if (!subcat.isValid()) {
                System.err.println("invalid unified subcat!");
                continue;
            }
            if (!subcat.includesVariables(partition)) {
                System.err.println("unified subcat does not encompass the partition");
                // partition does not work for some valid subcat -- return NULL (?) pseudocode unclear
                return null;
            }
            unifiedSubcats.add(subcat);
        }

        return unifiedSubcats;

    }
}
