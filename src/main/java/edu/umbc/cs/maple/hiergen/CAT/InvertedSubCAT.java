package edu.umbc.cs.maple.hiergen.CAT;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InvertedSubCAT {

    protected SubCAT subCAT;
    protected Set<Integer> precedingIndexes;

    public InvertedSubCAT(SubCAT subcat) {
        this.precedingIndexes = new TreeSet<>();
        CATrajectory cat = subcat.getCat();
        int lowestIndex = cat.getLastIndex();
        Set<Integer> processedIndexes = subcat.getActionIndexes();
        if (processedIndexes.size() > 0) {
            lowestIndex = Collections.min(subcat.getActionIndexes());
        }
        int firstIndex = cat.getFirstIndex();
        for (int i = firstIndex + 1; i < lowestIndex; i++) {
            this.precedingIndexes.add(i);
        }
    }

    public SubCAT getSubCAT() {
        return subCAT;
    }

    public void setSubCAT(SubCAT subCAT) {
        this.subCAT = subCAT;
    }

    public Set<Integer> getPrecedingIndexes() {
        return precedingIndexes;
    }

    public void setPrecedingIndexes(Set<Integer> precedingIndexes) {
        this.precedingIndexes = precedingIndexes;
    }

    public static List<InvertedSubCAT> create(List<SubCAT> subcats) {
        return subcats.stream().map(InvertedSubCAT::new).collect(Collectors.toList());
    }
}
