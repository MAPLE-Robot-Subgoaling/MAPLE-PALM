package hiergen;

import hiergen.CAT.CATrajectory;
import hiergen.CAT.CausalEdge;
import hiergen.CAT.SubCAT;

import java.util.ArrayList;
import java.util.List;


public class CATScan {

    public static List<SubCAT> scan(ArrayList<CATrajectory > trajectories, List<Object> variables)
    {
        System.out.println("Scan");
        List<SubCAT> subCATs = new ArrayList<SubCAT>();
        ArrayList<Integer> actionIndicies = new ArrayList<>();
        int start, end = -1;
        for(CATrajectory ct : trajectories)
        {
            List<CausalEdge> edges = ct.getEdges();
            for(Object v: variables)
            {
                String var = v.toString();
                //System.out.println("Variable: " + var);
                for (CausalEdge e : edges)
                {
                    /*if(e.getRelavantVariable().equals("Passenger0:inTaxi")) {
                        System.out.println(e.getRelavantVariable());
                        System.out.println(e.getStart());
                        System.out.println(ct.actionCount());
                    }*/
                    if (e.getEnd() == ct.actionCount()+1 && e.getRelavantVariable().equals(var) && !(actionIndicies.contains(e.getStart())))
                    {
                        System.out.println("Entered");
                        actionIndicies.add(e.getStart());
                        end = e.getStart();
                        break;
                    }
                }
            }

            //System.out.println("End: " + end);
            if(end == -1)
                return null;

            start = end;
            int prevSize;
            List<String> actions = ct.getActions();
            //for(String a:actions)
             //   System.out.println(a);
            //System.out.println(actions.size());
            //check all actions with outgoing edges to the subCAT, only have outgoing edges maintained in the subCAT
            do
            {
                prevSize = actionIndicies.size();
                for(int i = 0; i < actions.size(); i++)
                {
                    //System.out.println(i);
                    //skip duplicates
                    boolean contained = false;
                    if(!(actionIndicies.contains(i)))
                    {
                        List<Integer> prevEdges = ct.findEdges(i);
                        if(prevEdges != null) {
                            for (Integer e : prevEdges) {
                                for (Integer k : actionIndicies)
                                    if (k.equals(e))
                                        contained = true;
                            }
                        }
                    }
                    if (contained) {
                        actionIndicies.add(i);
                        if(i < start)
                            start = i;
                    }
                }
            }while(actionIndicies.size() != prevSize);
            System.out.println("--------------------Sub-CAT--------------------------");
            System.out.println("Start: " + start);
            System.out.println("End: " + end);
            for(Integer ind: actionIndicies)
                System.out.print(ind + " ");
            System.out.println();
            subCATs.add(new SubCAT(start, end, actionIndicies, ct));
        }

        return subCATs;
    }


}
