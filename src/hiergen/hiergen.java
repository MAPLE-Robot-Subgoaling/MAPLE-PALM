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
        List<Task> finalTasks = new ArrayList<>();
        List<Task> subTasks = new ArrayList<Task>();
        List<String> actions = new ArrayList<>();
        if(goal.isEmpty())
            return null;
        Set<Object> keys = goal.keySet();
        ArrayList<Object> relevantVars = new ArrayList<Object>(keys);
        List<List<SubCAT> > subCATs = new ArrayList<>();

        for(Object rv : relevantVars)
        {
            ArrayList<Object> curr = new ArrayList<>();
            curr.add(rv);
            List<SubCAT> temp = CATScan.scan(CATrajectories, curr);
            subCATs.add(temp);
        }

        List<SubCAT> unifiedSubCATs = new ArrayList<>();

        for(int i = 0; i < subCATs.size(); i ++)
        {
            SubCAT unity = new SubCAT();
            for(SubCAT sC : subCATs.get(i))
            {
                SubCAT.Unify(unity,sC);
            }
            unifiedSubCATs.add(unity);
        }

        if(!unifiedSubCATs.isEmpty())
        {
            for(SubCAT sub: unifiedSubCATs)
            {
                ArrayList<CATrajectory> extractedCATrajectories = extract(CATrajectories, sub);
                ArrayList<Task> Q = builder(trees, extractedCATrajectories);
                if(Q != null && Q.size() > 0)
                {
                    Task q = generate(trees, extractedCATrajectories);
                    List<String> variablesQ = q.variables;
                    Map<Object, Object> goalQ = q.goal;
                    for(Task t: Q)
                    {
                        subTasks.add(t);
                    }

                    for(String act:trees.keySet())
                    {
                        actions.add(act);
                    }

                    relevantVars.addAll(goal.keySet());

                    for(Task s: Q) {
                        relevantVars.addAll(s.variables);
                    }

                    finalTasks.add(new Task(goal, actions, relevantVars));
                }
            }
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

    public static ArrayList<CATrajectory> extract(List<CATrajectory> CATs, SubCAT extractee)
    {
        ArrayList<CATrajectory> CATsExtracted = new ArrayList<>();

        for(CATrajectory c: CATs)
        {
                CATrajectory temp = c;
                c.setEdges(c.getEdges().subList(0,extractee.getStart()));
                c.setActions(c.getActions().subList(0,extractee.getStart()));
                CATsExtracted.add(c);
        }

        return CATsExtracted;

    }

}
