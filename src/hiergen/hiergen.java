package hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import hiergen.CAT.CATrajectory;
import hiergen.CAT.VariableTree;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 */
public class hiergen {

    public static Task generate(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> caTrajectories)
    {
        ArrayList<String> goalVars = new ArrayList<>();
        //determine goal somehow
        for(CATrajectory c: caTrajectories)
        {
            if(c.getActions().size() > 1){

            }
        }
    }

    public static ArrayList<Task> builder(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> caTrajectories)
    {
        Map<Object, Object> goal = hiergen.determineGoal(caTrajectories);
        if(goal.size() == 0)
            return null;
        Set<Object> keys = goal.keySet();
        ArrayList<Object> releventVarsObj = new ArrayList<Object>(keys);
        ArrayList<String> goalVars = new ArrayList<>();
        for(Object rv : releventVarsObj)
        {
            goalVars.add(rv.toString());
        }
        ArrayList<Integer> indices = CATScan.scan(caTrajectories, goalVars);


    }

    public static Map<Object, Object> determineGoal(ArrayList<CATrajectory> caTrajectories)
    {
        Episode e = caTrajectories.get(0).getBaseTrajectory();
        State s = e.state(e.numActions());
        List<Object> vars = s.variableKeys();
        Map<Object, Object> goalCond = new HashMap<>();
        for(Object var: vars)
        {
            goalCond.put(var, s.get(var));
        }
        for(CATrajectory cat: caTrajectories)
        {
            e = caTrajectories.get(0).getBaseTrajectory();
            s = e.state(e.numActions());
            for(Object var: vars)
            {
                if(!(goalCond.get(var).equals(s.get(var))))
                {
                    goalCond.remove(var);
                }
            }
        }
        return goalCond;
    }

}
