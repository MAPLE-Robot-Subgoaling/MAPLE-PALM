package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import hiergen.CAT.CATrajectory;
import hiergen.CAT.SubCAT;
import hiergen.CAT.VariableTree;

import java.beans.SimpleBeanInfo;
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
        ArrayList<Task> tasks = new ArrayList<>();
        boolean mult = true;
        ArrayList<String> uniqActions = new ArrayList<>();

        for(CATrajectory c: CATrajectories)
        {
            List<String> uniq = c.uniqueActions();
            for(String u: uniq)
            {
                if(!uniqActions.contains(u))
                    uniqActions.add(u);
            }
        }

        if(uniqActions.size() > 1)
        {
            tasks = builder(trees, CATrajectories);
            if(tasks != null && !tasks.isEmpty())
            {
                ArrayList<String> actions = new ArrayList<>();
                ArrayList<Object> vars = new ArrayList<>();
                for(Task t: tasks)
                {
                    for(Object var : t.variables) {
                        if(!vars.contains(var))
                            vars.add(var);
                    }
                    for(String a : t.actions) {
                        if(!actions.contains(a))
                            actions.add(a);
                    }
                }
                return new Task(goalVars, actions, vars, tasks);
            }

            ArrayList<CATrajectory> ult = new ArrayList<>();
            ArrayList<CATrajectory> nonUlt = new ArrayList<>();
            for(CATrajectory c: CATrajectories)
            {
                ult.add(c.getUltimateActions(goalVars));
                nonUlt.add(extractPreceeding((SubCAT)ult.get(ult.size()-1)));
            }

            ArrayList<Task> Q = builder(trees, nonUlt);
            if(Q != null && !Q.isEmpty())
            {
                Task fin = generate(trees, ult);
                Map<Object, Object> goalQ = fin.goal;

                Set<Object> k = goalQ.keySet();
                ArrayList<Object> rv = new ArrayList<Object>(k);
                for(Object v:rv)
                {
                    if(!fin.actions.contains(v))
                        fin.variables.add(v);
                }

                for(Task s: Q) {
                    fin.subTasks.add(s);
                    for(Object var : s.variables) {
                        if(!fin.variables.contains(var))
                            fin.variables.add(var);
                    }
                    for(String a:s.actions) {
                        if(!fin.actions.contains(a))
                            fin.actions.add(a);
                    }
                }
                return fin;
            }

        }

        Set<Object> k = goalVars.keySet();
        ArrayList<Object> rv = new ArrayList<Object>(k);
        return new Task(goalVars, uniqActions, rv);
    }

    public static ArrayList<Task> builder(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories)
    {
        //System.out.println("Builder");

        Map<Object, Object> goal = hiergen.determineGoal(CATrajectories);
        ArrayList<Task> finalTasks = new ArrayList<>();
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

        if(subCATs.isEmpty())
            return null;
        List<SubCAT> unifiedSubCATs = new ArrayList<>();

        for(int i = 0;i < CATrajectories.size(); i ++)
        {
            SubCAT unity = null;
            for (int j = 0; j < subCATs.size(); j++) {
                if(subCATs.get(j) != null && !subCATs.get(j).isEmpty()) {
                    if(unity == null)
                        unity = new SubCAT(subCATs.get(j).get(i));
                    else
                        unity = unity.Unify(subCATs.get(j).get(i));
                }
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
                ArrayList<CATrajectory> extractedCATrajectories = new ArrayList<>();
                CATrajectory temp = extractPreceeding(sub);
                if(temp != null)
                    extractedCATrajectories.add(temp);
                ArrayList<Task> Q = null;
                if(extractedCATrajectories != null && !extractedCATrajectories.isEmpty())
                    Q = builder(trees, extractedCATrajectories);
                if(Q != null && Q.size() > 0)
                {
                    CATrajectory ct = CATrajectory.subCATToCAT(sub);
                    ArrayList<CATrajectory> fin = new ArrayList<>();
                    fin.add(ct);
                    Task q = generate(trees, fin);
                    Map<Object, Object> goalQ = q.goal;

                    Set<Object> k = goalQ.keySet();
                    ArrayList<Object> rv = new ArrayList<Object>(k);
                    for(Object v:rv)
                    {
                        if(!q.actions.contains(v))
                                q.variables.add(v);
                    }

                    for(Task s:Q) {
                        q.subTasks.add(s);
                        for(Object var : s.variables) {
                            if(!q.variables.contains(var))
                                q.variables.add(var);
                        }
                        for(String a:s.actions) {
                            if(!q.actions.contains(a))
                                q.actions.add(a);
                        }
                    }
                    finalTasks.add(q);
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
            List<SubCAT> merged = merge(unifiedSubCATs);
            if(merged != null || !merged.isEmpty()) {
                ArrayList<CATrajectory> nonMerged = new ArrayList<>();
                ArrayList<CATrajectory> ooSubCAT = new ArrayList<>();
                for (SubCAT m : merged) {
                    if(merged != null) {
                        CATrajectory temp = extractPreceeding(m);
                        if(temp != null)
                            nonMerged.add(temp);
                        ooSubCAT.add(m);
                    }
                }

                List<Task> Q = null;
                if(!nonMerged.isEmpty())
                    {Q = builder(trees, nonMerged);}
                if(Q == null || Q.isEmpty())
                    return null;
                else
                {

                    Task q = generate(trees, ooSubCAT);
                    Map<Object, Object> goalQ = q.goal;

                    Set<Object> k = goalQ.keySet();
                    ArrayList<Object> rv = new ArrayList<Object>(k);
                    for(Object v:rv)
                    {
                        if(!q.actions.contains(v))
                            q.variables.add(v);
                    }

                    for(Task s: Q) {
                        q.subTasks.add(s);

                        for (Object var : s.variables) {
                            if (!q.variables.contains(var))
                                q.variables.add(var);
                        }

                        for (String a : s.actions) {
                            if (!q.actions.contains(a))
                                q.actions.add(a);
                        }
                    }
                }
            }
        }

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
                obj = CATrajectories.get(0).getBaseTrajectory().state(CATrajectories.get(0).getSub().getEnd()-1).get(var);
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
                    obj = c.getBaseTrajectory().state(c.getSub().getEnd()-1).get(key);
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
        if(extractee == null || extractee.getStart() == 0)
            return null;
        ArrayList<CATrajectory> CATsExtracted = new ArrayList<>();
        for(CATrajectory c: CATs)
        {
            if(c.getBaseTrajectory() == extractee.getBaseTrajectory()) {
                CATrajectory temp = c;
                //c.setEdges(c.getEdges().subList(0,extractee.getStart()));
                temp.setEnd(extractee.getStart());
                CATsExtracted.add(temp);
            }
        }

        return CATsExtracted;

    }

    public static CATrajectory extractPreceeding(SubCAT extractee)
    {
        if(extractee.getStart() <= 1)
            return null;
        List<Integer> inds  = new ArrayList<>();

        for(int  i = 0; i < extractee.getStart();i++)
            inds.add(i);

        return new SubCAT(0, extractee.getStart()-1, inds, extractee.getRelVars(), extractee.getCAT());
    }

    public static List<SubCAT> merge(List<SubCAT> subCats)
    {
        List<SubCAT> merged;
        List<Object> relVars = new ArrayList<>();
        ArrayList<CATrajectory> cats = new ArrayList<>();
        //cats.addAll(subCats);

        for(SubCAT c: subCats)
        {
            if(!cats.contains(c.getCAT()))
            {
                cats.add(c.getCAT());
            }
            for(Object v: c.getRelVars())
            {
                if(!relVars.contains(v))
                {
                    relVars.add(v);
                }
            }
        }

        merged = CATScan.scan(cats, relVars);
        return merged;
    }



}
