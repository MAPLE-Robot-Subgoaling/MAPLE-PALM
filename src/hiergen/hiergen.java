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
        System.out.println("Gen");
        Map<Object, Object> goalVars = determineGoal(CATrajectories);
        if(goalVars.isEmpty())
            return null;
        ArrayList<Task> t = new ArrayList<>();
        if(CATrajectories.get(0).actionCount() > 1)
        {
            t = builder(trees, CATrajectories);
        }
        if(t == null || t.isEmpty())
        {
            return new Task();
        }

        //List<String> actions = CATrajectories.get(0).getActions();
        return null;
    }

    public static ArrayList<Task> builder(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories)
    {
        System.out.println("Builder");
        Map<Object, Object> goal = hiergen.determineGoal(CATrajectories);
        ArrayList<Task> finalTasks = new ArrayList<>();
        List<Task> subTasks = new ArrayList<Task>();
        ArrayList<String> actions = new ArrayList<>();
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
            if(temp != null)
                subCATs.add(temp);
        }

        List<SubCAT> unifiedSubCATs = new ArrayList<>();

        for(int i = 0;i < subCATs.size(); i ++)
        {
            SubCAT unity = subCATs.get(i).get(0);
            for(int j = 1; j < subCATs.get(i).size(); j++)
            {
                unity.Unify(subCATs.get(i).get(j));
            }
            unifiedSubCATs.add(unity);
        }

        ArrayList<Integer> remove = new ArrayList<>();
        int i = -1;
        if(!unifiedSubCATs.isEmpty())
        {
            for(SubCAT sub: unifiedSubCATs)
            {
                i++;
                ArrayList<CATrajectory> extractedCATrajectories = extractPreceeding(CATrajectories, sub);
                ArrayList<Task> Q = builder(trees, extractedCATrajectories);
                if(Q != null && Q.size() > 0)
                {
                    CATrajectory ct = CATrajectory.subCATToCAT(sub);
                    ArrayList<CATrajectory> fin = new ArrayList<>();
                    fin.add(ct);
                    Task q = generate(trees, fin);
                    List<Object> variablesQ = q.variables;
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
                    remove.add(i);
                }
            }
        }

        for(Integer s: remove)
        {
            subCATs.remove(s);
            unifiedSubCATs.remove(s);
        }

        if(!subCATs.isEmpty())
        {


        }
        //ArrayList<Integer> indices = CATScan.scan(caTrajectories, goalVars);

        return finalTasks;
    }

    public static Map<Object, Object> determineGoal(ArrayList<CATrajectory> CATrajectories)
    {
        List<Object> vars = CATrajectories.get(0).getBaseTrajectory().state(0).variableKeys();
        Map<Object, Object> goal = new HashMap<>();
        for(Object var: vars)
        {
            Object obj;
            if(CATrajectories.get(0).getSub() != null)
            {
                obj = CATrajectories.get(0).getBaseTrajectory().state(CATrajectories.get(0).getSub().getEnd()).get(var);
            }
            else {
                obj = CATrajectories.get(0).getBaseTrajectory().state(CATrajectories.get(0).getBaseTrajectory().stateSequence.size() - 1).get(var);
            }
            goal.put(var, obj);
        }
        for(CATrajectory c: CATrajectories)
        {
            List<Object> remove = new ArrayList<>();
            for(Object key: goal.keySet())
            {
                Object obj;
                if(c.getSub() != null)
                {
                    obj = c.getBaseTrajectory().state(c.getSub().getEnd()).get(key);
                }
                else{
                    obj = c.getBaseTrajectory().state(c.getBaseTrajectory().stateSequence.size() - 1).get(key);
                }
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

    public static ArrayList<CATrajectory> extractPreceeding(List<CATrajectory> CATs, SubCAT extractee)
    {
        ArrayList<CATrajectory> CATsExtracted = new ArrayList<>();
        //to-do must work for converted sub-CATs
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
