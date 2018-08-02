package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.AttributeRelation;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.SubCAT;

import java.util.*;

import static edu.umbc.cs.maple.hiergen.CAT.SubCAT.SUBCAT_ID;

public class HierUnify {
    public static Map<AttributeRelation,List<SubCAT>> run(Map<AttributeRelation, List<SubCAT>> goalToSubcats) {

        // regroup by CAT
        Map<String, List<SubCAT>> catNameToSubcats = new LinkedHashMap<>();
        for (AttributeRelation goal : goalToSubcats.keySet()) {
            List<SubCAT> subcats = goalToSubcats.get(goal);
            for (SubCAT subcat : subcats) {
                CATrajectory cat = subcat.getCat();
                String catName = cat.getName();
                List<SubCAT> catSubcats = catNameToSubcats.computeIfAbsent(catName, i -> new ArrayList<>());
                catSubcats.add(subcat);
            }
        }

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
        }

            return goalToSubcats;

    }
}
