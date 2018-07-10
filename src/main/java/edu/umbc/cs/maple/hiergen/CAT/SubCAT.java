package edu.umbc.cs.maple.hiergen.CAT;

import burlap.mdp.core.oo.state.OOVariableKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class SubCAT extends CATrajectory {

    private List<OOVariableKey> relevantVariables;

    public SubCAT( ) {

    }

    public SubCAT unify(SubCAT a) {

        throw new RuntimeException("Not implenmented");
//        SubCAT unification = new SubCAT(a.start, a.end, a.actionInds, relevantVariables, c);
//
//        for (Integer i : this.actionInds) {
//            if (!unification.actionInds.contains(i))
//                unification.actionInds.add(i);
//        }
//
//        if (this.start < a.start)
//            unification.start = a.start;
//        if (this.end > a.end)
//            unification.end = a.end;
//
//        for (OOVariableKey var : a.relevantVariables) {
//            if (!unification.relevantVariables.contains(var)) {
//                unification.relevantVariables.add(var);
//            }
//        }
//
//        return unification;
    }

    public static SubCAT unify(SubCAT a, SubCAT b) {
        throw new RuntimeException("Not implemented");
        //System.out.println("Unity");
        //System.out.println(a.CAT);
//        SubCAT unification = new SubCAT(a.start, a.end, a.actionInds, a.relevantVariables, a.c);
//
//        for (Integer i : b.actionInds) {
//            if (!unification.actionInds.contains(i))
//                unification.actionInds.add(i);
//        }
//
//        if (b.start < a.start)
//            unification.start = a.start;
//        if (b.end > a.end)
//            unification.end = a.end;
//
//        unification.relevantVariables.addAll(a.relevantVariables);
//
//        return unification;
    }

    /*public CATrajectory getUltimateActions()
    {
        SubCAT ultimate = new SubCAT(this);
        ArrayList<Integer> inds = new ArrayList<>();
        inds.add(end);
        ultimate.actionInds = inds;
        ultimate.start = end;
        return ultimate;
    }*/

    public List<OOVariableKey> getRelevantVariables() {
        return relevantVariables;
    }

    public void setRelevantVariables(List<OOVariableKey> relevantVariables) {
        this.relevantVariables = relevantVariables;
    }
}
