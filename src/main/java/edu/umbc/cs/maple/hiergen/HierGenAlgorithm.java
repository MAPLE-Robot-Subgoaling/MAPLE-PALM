package edu.umbc.cs.maple.hiergen;

import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.SubCAT;
import edu.umbc.cs.maple.hiergen.CAT.VariableTree;

import java.util.*;

/**
 *
 */
public class HierGenAlgorithm {

    public static HierGenTask generate(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories) {

        System.out.println("Gen");

        Map<Object, Object> goalVariables = determineGoal(CATrajectories);
        if (goalVariables.isEmpty()) {
            return null;
        }

        ArrayList<HierGenTask> tasks;

        ArrayList<String> uniqueActions = new ArrayList<>();

        for (CATrajectory cat : CATrajectories) {
            List<String> catActions = cat.uniqueActions();
            for (String catAction : catActions) {
                if (!uniqueActions.contains(catAction)) {
                    uniqueActions.add(catAction);
                }
            }
        }

        if (uniqueActions.size() > 1) {
            tasks = builder(trees, CATrajectories);
            if (tasks != null && !tasks.isEmpty()) {
                ArrayList<String> actions = new ArrayList<>();
                ArrayList<Object> variables = new ArrayList<>();
                for (HierGenTask task : tasks) {
                    for (Object variable : task.variables) {
                        if (!variables.contains(variable))
                            variables.add(variable);
                    }
                    for (String action : task.actions) {
                        if (!actions.contains(action)) {
                            actions.add(action);
                        }
                    }
                }
                return new HierGenTask(goalVariables, actions, variables, tasks);
            }

            ArrayList<CATrajectory> ultimateCATs = new ArrayList<>();
            ArrayList<CATrajectory> nonUltimateCATs = new ArrayList<>();
            for (CATrajectory c : CATrajectories) {
                CATrajectory ultimateCAT = c.getUltimateActions(goalVariables);
                ultimateCATs.add(ultimateCAT);
                SubCAT subCAT = (SubCAT) ultimateCATs.get(ultimateCATs.size() - 1);
                CATrajectory preceeding = extractPreceeding(subCAT);
                nonUltimateCATs.add(preceeding);
            }

            ArrayList<HierGenTask> builtTasksQ = builder(trees, nonUltimateCATs);
            if (builtTasksQ != null && !builtTasksQ.isEmpty()) {

                // still need to parse / rename this

                HierGenTask finalTask = generate(trees, ultimateCATs);
                Map<Object, Object> goalQ = finalTask.goal;

                Set<Object> keys = goalQ.keySet();
                ArrayList<Object> rv = new ArrayList<>(keys);
                for (Object v : rv) {
                    if (!finalTask.actions.contains(v)) {
                        finalTask.variables.add(v);
                    }
                }

                for (HierGenTask task : builtTasksQ) {
                    finalTask.subTasks.add(task);
                    for (Object variable : task.variables) {
                        if (!finalTask.variables.contains(variable)) {
                            finalTask.variables.add(variable);
                        }
                    }
                    for (String action : task.actions) {
                        if (!finalTask.actions.contains(action)) {
                            finalTask.actions.add(action);
                        }
                    }
                }
                return finalTask;
            }

        }

        Set<Object> k = goalVariables.keySet();
        ArrayList<Object> rv = new ArrayList<Object>(k);
        return new HierGenTask(goalVariables, uniqueActions, rv);

    }

    public static ArrayList<HierGenTask> builder(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories) {

        System.out.println("Builder");

        Map<Object, Object> goal = HierGenAlgorithm.determineGoal(CATrajectories);
        ArrayList<HierGenTask> finalTasks = new ArrayList<>();
        if (goal.isEmpty()) {
            return null;
        }

        Set<Object> keys = goal.keySet();
        ArrayList<Object> relevantVariables = new ArrayList<>(keys);
        List<List<SubCAT>> subCATs = new ArrayList<>();
        for (Object relevantVariable : relevantVariables) {
            ArrayList<Object> currentVariables = new ArrayList<>();
            currentVariables.add(relevantVariable);
            List<SubCAT> tempCAT = CATScan.scan(CATrajectories, currentVariables);
            if (tempCAT != null) {
                subCATs.add(tempCAT);
            }
        }

        if (subCATs.isEmpty()) {
            return null;
        }

        List<SubCAT> unifiedSubCATs = new ArrayList<>();

        for (int i = 0; i < CATrajectories.size(); i++) {
            SubCAT unity = null;
            for (int j = 0; j < subCATs.size(); j++) {
                if (subCATs.get(j) != null && !subCATs.get(j).isEmpty()) {
                    if (unity == null) {
                        unity = new SubCAT(subCATs.get(j).get(i));
                    } else {
                        unity = unity.Unify(subCATs.get(j).get(i));
                    }
                }
            }
            unifiedSubCATs.add(unity);
        }

        ArrayList<Integer> remove = new ArrayList<>();
        int i = -1;
        if (!unifiedSubCATs.isEmpty()) {
            for (SubCAT sub : unifiedSubCATs) {
                i++;
                ArrayList<CATrajectory> extractedCATrajectories = new ArrayList<>();
                CATrajectory temp = extractPreceeding(sub);
                if (temp != null)
                    extractedCATrajectories.add(temp);
                ArrayList<HierGenTask> Q = null;
                if (extractedCATrajectories != null && !extractedCATrajectories.isEmpty())
                    Q = builder(trees, extractedCATrajectories);
                if (Q != null && Q.size() > 0) {
                    CATrajectory ct = CATrajectory.subCATToCAT(sub);
                    ArrayList<CATrajectory> fin = new ArrayList<>();
                    fin.add(ct);
                    HierGenTask q = generate(trees, fin);
                    Map<Object, Object> goalQ = q.goal;

                    Set<Object> k = goalQ.keySet();
                    ArrayList<Object> rv = new ArrayList<Object>(k);
                    for (Object v : rv) {
                        if (!q.actions.contains(v))
                            q.variables.add(v);
                    }

                    for (HierGenTask s : Q) {
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
                    finalTasks.add(q);
                    remove.add(i);
                }
            }
        }

        for (Integer s : remove) {
            subCATs.remove(s);
            unifiedSubCATs.remove(s);
        }

        if (!subCATs.isEmpty()) {
            List<SubCAT> merged = merge(unifiedSubCATs);
            if (merged != null || !merged.isEmpty()) {
                ArrayList<CATrajectory> nonMerged = new ArrayList<>();
                ArrayList<CATrajectory> ooSubCAT = new ArrayList<>();
                for (SubCAT m : merged) {
                    if (merged != null) {
                        CATrajectory temp = extractPreceeding(m);
                        if (temp != null)
                            nonMerged.add(temp);
                        ooSubCAT.add(m);
                    }
                }

                List<HierGenTask> Q = null;
                if (!nonMerged.isEmpty()) {
                    Q = builder(trees, nonMerged);
                }
                if (Q == null || Q.isEmpty())
                    return null;
                else {

                    HierGenTask q = generate(trees, ooSubCAT);
                    Map<Object, Object> goalQ = q.goal;

                    Set<Object> k = goalQ.keySet();
                    ArrayList<Object> rv = new ArrayList<Object>(k);
                    for (Object v : rv) {
                        if (!q.actions.contains(v))
                            q.variables.add(v);
                    }

                    for (HierGenTask s : Q) {
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

    public static Map<Object, Object> determineGoal(ArrayList<CATrajectory> CATrajectories) {
        List<Object> vars = CATrajectories.get(0).getBaseTrajectory().state(0).variableKeys();
        Map<Object, Object> goal = new HashMap<>();
        for (Object var : vars) {
            Object obj;
            if (CATrajectories.get(0).getSub() != null) {
                obj = CATrajectories.get(0).getBaseTrajectory().state(CATrajectories.get(0).getSub().getEnd() - 1).get(var);
            } else {
                obj = CATrajectories.get(0).getBaseTrajectory().state(CATrajectories.get(0).getBaseTrajectory().stateSequence.size() - 1).get(var);
            }
            goal.put(var, obj);
        }
        for (CATrajectory c : CATrajectories) {
            List<Object> remove = new ArrayList<>();
            for (Object key : goal.keySet()) {
                Object obj;
                if (c.getSub() != null) {
                    obj = c.getBaseTrajectory().state(c.getSub().getEnd() - 1).get(key);
                } else {
                    obj = c.getBaseTrajectory().state(c.getBaseTrajectory().stateSequence.size() - 1).get(key);
                }
                if (!obj.equals(goal.get(key))) {
                    remove.add(key);
                }
            }

            for (Object r : remove) {
                goal.remove(r);
            }
        }
        return goal;
    }

    public static ArrayList<CATrajectory> extractPreceeding(List<CATrajectory> CATs, SubCAT extractee) {
        if (extractee == null || extractee.getStart() == 0)
            return null;
        ArrayList<CATrajectory> CATsExtracted = new ArrayList<>();
        for (CATrajectory c : CATs) {
            if (c.getBaseTrajectory() == extractee.getBaseTrajectory()) {
                CATrajectory temp = c;
                //c.setEdges(c.getEdges().subList(0,extractee.getStart()));
                temp.setEnd(extractee.getStart());
                CATsExtracted.add(temp);
            }
        }

        return CATsExtracted;

    }

    public static CATrajectory extractPreceeding(SubCAT extractee) {
        if (extractee.getStart() <= 1)
            return null;
        List<Integer> inds = new ArrayList<>();

        for (int i = 0; i < extractee.getStart(); i++)
            inds.add(i);

        return new SubCAT(0, extractee.getStart() - 1, inds, extractee.getRelVars(), extractee.getCAT());
    }

    public static List<SubCAT> merge(List<SubCAT> subCats) {
        List<SubCAT> merged;
        List<Object> relVars = new ArrayList<>();
        ArrayList<CATrajectory> cats = new ArrayList<>();
        //cats.addAll(subCats);

        for (SubCAT c : subCats) {
            if (!cats.contains(c.getCAT())) {
                cats.add(c.getCAT());
            }
            for (Object v : c.getRelVars()) {
                if (!relVars.contains(v)) {
                    relVars.add(v);
                }
            }
        }

        merged = CATScan.scan(cats, relVars);
        return merged;
    }


}
