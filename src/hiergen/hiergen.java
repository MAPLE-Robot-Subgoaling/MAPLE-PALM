package hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import hiergen.CAT.CATrajectory;
import hiergen.CAT.SubCAT;
import hiergen.CAT.VariableTree;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 */
public class hiergen {

    public static Task generate(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories)
    {
        Map<Object, Object> goalVars = determineGoal(CATrajectories);
        if(goalVars.isEmpty())
            return null;

        if(CATrajectories.get(0).actionCount() > 1)
        {
            builder(trees, CATrajectories);
        }
        //List<String> actions = CATrajectories.get(0).getActions();
        return null;
    }

    public static ArrayList<Task> builder(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories)
    {
        Map<Object, Object> goal = hiergen.determineGoal(CATrajectories);
        if(goal.isEmpty())
            return null;
        Set<Object> keys = goal.keySet();
        ArrayList<Object> relevantVars = new ArrayList<Object>(keys);
        List<SubCAT> subCATs = new ArrayList<>();

        for(Object rv : relevantVars)
        {
            ArrayList<Object> curr = new ArrayList<>();
            curr.add(rv);
            List<SubCAT> temp = CATScan.scan(CATrajectories, curr);
        }



        //ArrayList<Integer> indices = CATScan.scan(caTrajectories, goalVars);

        return null;
    }

    public static Map<Object, Object> determineGoal(ArrayList<CATrajectory> CATrajectories)
    {
        List<Object> vars = CATrajectories.get(0).getBaseTrajectory().state(0).variableKeys();
        Map<Object, Object> goal = new HashMap<>();
        for(Object var: vars)
        {
            Object obj = CATrajectories.get(0).getBaseTrajectory().state(CATrajectories.get(0).getBaseTrajectory().stateSequence.size()-1).get(var);
            goal.put(var, obj);
        }
        for(CATrajectory c: CATrajectories)
        {
            List<Object> remove = new ArrayList<>();
            for(Object key: goal.keySet())
            {
                Object obj = c.getBaseTrajectory().state(c.getBaseTrajectory().stateSequence.size()-1).get(key);
                if(!obj.equals(goal.get(key)))
                {
                    remove.add(key);
                }
            }

            for(Object r: remove)
            {
                goal.remove(r);
            }
        }
        return goal;
    }

}
