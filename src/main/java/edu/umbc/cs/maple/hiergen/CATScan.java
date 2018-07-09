package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.*;

import java.util.ArrayList;
import java.util.List;


public class CATScan {

    public static List<SubCAT> scan(ArrayList<CATrajectory> trajectories, List<Object> variables) {
        //System.out.println("Scan");
        List<SubCAT> subCATs = new ArrayList<SubCAT>();
        int start, end = -1;
        for (CATrajectory ct : trajectories) {
            ArrayList<Integer> actionIndicies = new ArrayList<>();
            List<CausalEdge> edges = ct.getEdges();
            for (Object v : variables) {
                String var = v.toString();
                for (CausalEdge e : edges) {

                    //if(e.getEnd()== ct.getEnd())
                    //    System.out.println(e.getRelavantVariable());
                    if (e.getStart() != 0 && e.getRelavantVariable().equals(var) && !(actionIndicies.contains(e.getStart()))) {
                        //System.out.println("Entered");
                        if (ct.getSub() == null) {
                            if (e.getEnd() == ct.getEnd()) {
                                actionIndicies.add(e.getStart());
                                end = e.getStart();
                            }
                        } else {
                            if (e.getEnd() > ct.getEnd() && e.getStart() <= ct.getEnd() && !(actionIndicies.contains(e.getStart()))) {
                                actionIndicies.add(e.getStart());
                                end = e.getStart();
                            }
                        }
                    }
                }
            }

            if (end == -1)
                return null;

            start = end;
            int prevSize;
            List<String> actions = ct.getActions();
            //check all actions with outgoing edges to the subCAT, only have outgoing edges maintained in the subCAT
            do {
                prevSize = actionIndicies.size();
                for (int i = 0; i < actions.size(); i++) {
                    //System.out.println(i);
                    //skip duplicates
                    boolean contained = false;
                    if (!(actionIndicies.contains(i))) {
                        List<Integer> nextEdges = ct.findEdges(i);
                        if (nextEdges != null) {
                            contained = true;
                            for (Integer e : nextEdges) {
                                if (!actionIndicies.contains(e)) {
                                    contained = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (contained) {
                        actionIndicies.add(i);
                        if (i < start)
                            start = i;
                    }
                }
            } while (actionIndicies.size() != prevSize);
            /*System.out.println("--------------------Sub-CAT--------------------------");
            System.out.println("Start: " + start);
            System.out.println("End: " + end);
            for(Integer ind: actionIndicies)
                System.out.print(ind + " " );
            System.out.println();*/
            if (start != 0 || end != 0)
                subCATs.add(new SubCAT(start, end, actionIndicies, variables, ct));
        }

        return subCATs;
    }

    public static SubCAT scan(CATrajectory trajectory, List<Object> variables) {
        ArrayList<CATrajectory> cats = new ArrayList<>();
        cats.add(trajectory);
        List<SubCAT> temp = scan(cats, variables);
        if (!temp.isEmpty()) {
            return temp.get(0);
        }

        return null;

    }


}
