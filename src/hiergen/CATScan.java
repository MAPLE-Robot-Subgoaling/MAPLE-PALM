package hiergen;

import hiergen.CAT.CATrajectory;
import hiergen.CAT.CausalEdge;
import hiergen.CAT.SubCAT;

import java.util.ArrayList;
import java.util.List;


public class CATScan {

    public static SubCAT scan (ArrayList<CATrajectory > trajectories, List<String > variables)
    {
        ArrayList<Integer> actionIndicies = new ArrayList<>();
        int start = -1, end = -1;
        for(CATrajectory ct : trajectories)
        {
            List<CausalEdge> edges = ct.getEdges();
            for(String var: variables)
            {
                for (CausalEdge e : edges)
                {
                    if (e.getEnd() == ct.actionCount() && e.getRelavantVariable().equals(var) && !(actionIndicies.contains(e.getStart())))
                    {
                        actionIndicies.add(e.getStart());
                        end = e.getStart();
                        break;
                    }
                }
            }

            if(end == -1)
                return null;

            start = end;
            int prevSize;
            List<String> actions = ct.getActions();
            //check all actions with outgoing edges to the subCAT, only have outgoing edges maintained in the subCAT
            do
            {
                prevSize = actionIndicies.size();
                for(int i = 0; i < actions.size(); i++)
                {
                    //skip duplicates
                    boolean contained = false;
                    if(!(actionIndicies.contains(i)))
                    {
                        List<Integer> prevEdges = ct.findEdges(i);
                        for(Integer e: prevEdges) {
                            if (actionIndicies.contains(e))
                                contained = true;
                            else {
                                contained = false;
                                break;
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
        }


        return new SubCAT(start, end, actionIndicies);
    }
}
