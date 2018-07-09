package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.SubCAT;
import edu.umbc.cs.maple.hiergen.CAT.VariableTree;

import java.util.*;

/**
 *
 */
public class HierGenAlgorithm {

    public static HierGenTask generate(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> cats) {

        System.out.println("Gen");

        Map<OOVariableKey, Object> goalVariables = determineGoal(cats);
        if (goalVariables.isEmpty()) {
            return null;
        }

        ArrayList<HierGenTask> tasks;

        ArrayList<String> uniqueActions = new ArrayList<>();

        for (CATrajectory cat : cats) {
            List<String> catActions = cat.uniqueActions();
            for (String catAction : catActions) {
                if (!uniqueActions.contains(catAction)) {
                    uniqueActions.add(catAction);
                }
            }
        }

        if (uniqueActions.size() > 1) {
            tasks = builder(trees, cats);
            if (tasks != null && !tasks.isEmpty()) {
                ArrayList<String> actions = new ArrayList<>();
                ArrayList<OOVariableKey> variables = new ArrayList<>();
                for (HierGenTask task : tasks) {
                    for (OOVariableKey variable : task.variables) {
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
            for (CATrajectory c : cats) {
                CATrajectory ultimateCAT = c.getUltimateActions(goalVariables);
                ultimateCATs.add(ultimateCAT);
                SubCAT subCAT = (SubCAT) ultimateCATs.get(ultimateCATs.size() - 1);
                CATrajectory preceeding = extractPreceding(subCAT);
                nonUltimateCATs.add(preceeding);
            }

            ArrayList<HierGenTask> builtTasksQ = builder(trees, nonUltimateCATs);
            if (builtTasksQ != null && !builtTasksQ.isEmpty()) {

                // still need to parse / rename this

                HierGenTask finalTask = generate(trees, ultimateCATs);
                Map<OOVariableKey, Object> goalQ = finalTask.goal;

                Set<OOVariableKey> keys = goalQ.keySet();
                ArrayList<OOVariableKey> relevantVariables = new ArrayList<>(keys);
                for (OOVariableKey relevantVariable : relevantVariables) {
                    if (!finalTask.actions.contains(relevantVariable)) {
                        finalTask.variables.add(relevantVariable);
                    }
                }

                for (HierGenTask task : builtTasksQ) {
                    finalTask.subTasks.add(task);
                    for (OOVariableKey variable : task.variables) {
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

        Set<OOVariableKey> goalKeys = goalVariables.keySet();
        ArrayList<OOVariableKey> goalRelevantVariables = new ArrayList<>(goalKeys);
        return new HierGenTask(goalVariables, uniqueActions, goalRelevantVariables);

    }

    public static ArrayList<HierGenTask> builder(Map<String, Map<String, VariableTree>> trees, ArrayList<CATrajectory> CATrajectories) {

        System.out.println("Builder");

        Map<OOVariableKey, Object> goal = HierGenAlgorithm.determineGoal(CATrajectories);
        ArrayList<HierGenTask> finalTasks = new ArrayList<>();
        if (goal.isEmpty()) {
            return null;
        }

        List<List<SubCAT>> subCATs = new ArrayList<>();
        for (OOVariableKey relevantVariable : goal.keySet()) {
            ArrayList<OOVariableKey> currentVariables = new ArrayList<>();
            currentVariables.add(relevantVariable);
            List<SubCAT> tempCAT = CATScan.scan(CATrajectories, currentVariables);
            if (tempCAT != null) {
                subCATs.add(tempCAT);
            }
        }

        if (subCATs.isEmpty()) {
            return null;
        }

        // compute UNIFY (Line 9 of Algorithm 6.2 HierBuilder)

        List<SubCAT> unifiedSubCATs = unify(CATrajectories, subCATs);

        ArrayList<Integer> remove = new ArrayList<>();
        int index = -1;
        if (!unifiedSubCATs.isEmpty()) {
            for (SubCAT sub : unifiedSubCATs) {
                index++;
                ArrayList<CATrajectory> extractedCATrajectories = new ArrayList<>();
                CATrajectory tempCAT = extractPreceding(sub);
                if (tempCAT != null) {
                    extractedCATrajectories.add(tempCAT);
                }
                ArrayList<HierGenTask> builtTasksQ = null;
                if (!extractedCATrajectories.isEmpty()) {
                    builtTasksQ = builder(trees, extractedCATrajectories);
                }
                if (builtTasksQ != null && !builtTasksQ.isEmpty()) {
                    CATrajectory cat = CATrajectory.subCATToCAT(sub);
                    ArrayList<CATrajectory> fin = new ArrayList<>();
                    fin.add(cat);
                    HierGenTask generatedTask = generate(trees, fin);
                    Map<OOVariableKey, Object> goalQ = generatedTask.goal;

                    for (OOVariableKey goalVariable : goalQ.keySet()) {
                        if (!generatedTask.actions.contains(goalVariable)) {
                            generatedTask.variables.add(goalVariable);
                        }
                    }

                    for (HierGenTask builtTask : builtTasksQ) {
                        generatedTask.subTasks.add(builtTask);
                        for (OOVariableKey variable : builtTask.variables) {
                            if (!generatedTask.variables.contains(variable)) {
                                generatedTask.variables.add(variable);
                            }
                        }
                        for (String action : builtTask.actions) {
                            if (!generatedTask.actions.contains(action)) {
                                generatedTask.actions.add(action);
                            }
                        }
                    }
                    finalTasks.add(generatedTask);
                    remove.add(index);
                }
            }
        }

        for (Integer s : remove) {
            subCATs.remove(s);
            unifiedSubCATs.remove(s);
        }

        if (!subCATs.isEmpty()) {
            List<SubCAT> merged = merge(unifiedSubCATs);
            if (merged == null || merged.isEmpty()) {
//                ArrayList<HierGenTask> emptyTaskList = new ArrayList<>();
//                return emptyTaskList; // return the empty list of tasks
                return null;
            }

            ArrayList<CATrajectory> nonMergedTrajectories = new ArrayList<>();
            ArrayList<CATrajectory> listOfSubCATs = new ArrayList<>();
            for (SubCAT m : merged) {
                if (merged != null) {
                    listOfSubCATs.add(m);
                    CATrajectory tempCAT = extractPreceding(m);
                    if (tempCAT != null) {
                        nonMergedTrajectories.add(tempCAT);
                    }
                }
            }

            List<HierGenTask> builtTasksQ = null;
            if (!nonMergedTrajectories.isEmpty()) {
                builtTasksQ = builder(trees, nonMergedTrajectories);
            }
            if (builtTasksQ == null || builtTasksQ.isEmpty()) {
                return null;
            } else {

                HierGenTask generatedTask = generate(trees, listOfSubCATs);
                Map<OOVariableKey, Object> goalQ = generatedTask.goal;

                for (OOVariableKey goalVariables : goalQ.keySet()) {
                    if (!generatedTask.actions.contains(goalVariables)) {
                        generatedTask.variables.add(goalVariables);
                    }
                }

                for (HierGenTask builtTask : builtTasksQ) {
                    generatedTask.subTasks.add(builtTask);

                    for (OOVariableKey variable : builtTask.variables) {
                        if (!generatedTask.variables.contains(variable)) {
                            generatedTask.variables.add(variable);
                        }
                    }

                    for (String action : builtTask.actions) {
                        if (!generatedTask.actions.contains(action)) {
                            generatedTask.actions.add(action);
                        }
                    }
                }
            }
        }

        return finalTasks;
    }

    public static Map<OOVariableKey, Object> determineGoal(ArrayList<CATrajectory> CATrajectories) {
        CATrajectory firstCAT = CATrajectories.get(0);
        State firstState = firstCAT.getBaseTrajectory().state(0);
        List<OOVariableKey> variables = (List<OOVariableKey>) (List<?>) firstState.variableKeys();
        Map<OOVariableKey, Object> goal = new HashMap<>();
        for (OOVariableKey variable : variables) {
            Object attributeValue;
            SubCAT subOfFirstCAT = firstCAT.getSub();
            Episode baseTrajectory = firstCAT.getBaseTrajectory();
            int lastStateIndex = subOfFirstCAT != null ? subOfFirstCAT.getEnd() - 1 : baseTrajectory.stateSequence.size() - 1;
            State lastState = baseTrajectory.state(lastStateIndex);
            attributeValue = lastState.get(variable);
            goal.put(variable, attributeValue);
        }
        for (CATrajectory cat : CATrajectories) {
            List<OOVariableKey> keysToRemove = new ArrayList<>();
            for (OOVariableKey key : goal.keySet()) {
                Object attributeValue;
                Episode baseTrajectory = cat.getBaseTrajectory();
                int lastStateIndex = cat.getSub() != null ? cat.getSub().getEnd() - 1 : cat.getBaseTrajectory().stateSequence.size() - 1;
                attributeValue = baseTrajectory.state(lastStateIndex).get(key);
                if (!attributeValue.equals(goal.get(key))) {
                    keysToRemove.add(key);
                }
            }

            for (OOVariableKey keyToRemove : keysToRemove) {
                goal.remove(keyToRemove);
            }
        }
        return goal;
    }

//    public static ArrayList<CATrajectory> extractPreceding(List<CATrajectory> CATs, SubCAT extractee) {
//        if (extractee == null || extractee.getStart() == 0)
//            return null;
//        ArrayList<CATrajectory> CATsExtracted = new ArrayList<>();
//        for (CATrajectory c : CATs) {
//            if (c.getBaseTrajectory() == extractee.getBaseTrajectory()) {
//                CATrajectory temp = c;
//                //c.setEdges(c.getEdges().subList(0,extractee.getStart()));
//                temp.setEnd(extractee.getStart());
//                CATsExtracted.add(temp);
//            }
//        }
//
//        return CATsExtracted;
//
//    }

    public static CATrajectory extractPreceding(SubCAT extractee) {

        if (extractee.getStart() <= 1) {
            return null;
        }

        List<Integer> actionIndexes = new ArrayList<>();

        for (int i = 0; i < extractee.getStart(); i++) {
            actionIndexes.add(i);
        }

        return new SubCAT(0, extractee.getStart() - 1, actionIndexes, extractee.getRelVars(), extractee.getCAT());
    }

    public static List<SubCAT> merge(List<SubCAT> subCATs) {

        List<SubCAT> mergedSubCATs;
        List<OOVariableKey> relevantVariables = new ArrayList<>();
        ArrayList<CATrajectory> cats = new ArrayList<>();
        //cats.addAll(subCats);

        for (SubCAT subCAT : subCATs) {
            if (!cats.contains(subCAT.getCAT())) {
                cats.add(subCAT.getCAT());
            }
            for (OOVariableKey relevantVariable : subCAT.getRelVars()) {
                if (!relevantVariables.contains(relevantVariable)) {
                    relevantVariables.add(relevantVariable);
                }
            }
        }

        mergedSubCATs = CATScan.scan(cats, relevantVariables);
        return mergedSubCATs;
    }

    public static List<SubCAT> unify(List<CATrajectory> CATs, List<List<SubCAT>> listOfSubCATLists) {
        List<SubCAT> unifiedSubCATs = new ArrayList<>();

        for (int i = 0; i < CATs.size(); i++) {
            SubCAT unity = null;
            for (List<SubCAT> subCATs : listOfSubCATLists) {
                if (subCATs != null && !subCATs.isEmpty()) {
                    SubCAT basis = subCATs.get(i);
                    if (unity == null) {
                        unity = new SubCAT(basis);
                    } else {
                        unity = unity.Unify(basis);
                    }
                }
            }
            unifiedSubCATs.add(unity);
        }

        return unifiedSubCATs;
    }

}
