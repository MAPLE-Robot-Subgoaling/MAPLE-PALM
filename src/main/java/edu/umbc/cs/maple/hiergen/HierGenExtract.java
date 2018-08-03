package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.InvertedSubCAT;

import java.util.ArrayList;
import java.util.List;

public class HierGenExtract {

    public static List<CATrajectory> run(List<CATrajectory> cats, List<InvertedSubCAT> invertedSubcats) {

        if (cats.size() != invertedSubcats.size()) { throw new RuntimeException("Error: |cats| != |invSubcats"); }

        List<CATrajectory> extractedCats = new ArrayList<>();
        for (int i = 0; i < cats.size(); i++) {
            CATrajectory original = cats.get(i);
            InvertedSubCAT range = invertedSubcats.get(i);
            CATrajectory cat = new CATrajectory(original, range);
        }

        return extractedCats;
    }

}
