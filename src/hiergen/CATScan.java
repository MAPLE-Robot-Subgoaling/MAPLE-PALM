package hiergen;

import hiergen.CAT.CATrajectory;
import hiergen.CAT.CausalEdge;

import java.util.ArrayList;
import java.util.List;


public class CATScan {

    public static ArrayList<Integer> scan (ArrayList<CATrajectory > trajectories, ArrayList<String > variables)
    {
        ArrayList<Integer> actionIndicies = new ArrayList<>();
        int start, end;
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
                        break;
                    }
                }
            }
            int prevSize;
            //check all actions with outgoing edges to the subCAT, only have outgoing edges maintained in the subCAT
            do
            {
                prevSize = actionIndicies.size();
                List<String> actions = ct.getActions();
                for(int i = 0; i < actions.size(); i++)
                {
                    //skip duplicates
                            if(!(actionIndicies.contains(i)))
                            {
                                boolean contained = false;
                                for (String var : variables) {
                            Integer e = ct.findEdge(i, var);
                            if (actionIndicies.contains(e))
                                contained = true;
                            else {
                                contained = false;
                                break;
                            }
                        }
                        if (contained)
                            actionIndicies.add(i);
                    }
                }
            }while(actionIndicies.size() != prevSize);
        }
        return actionIndicies;
    }
}
