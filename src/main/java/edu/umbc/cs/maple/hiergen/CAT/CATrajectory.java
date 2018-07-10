package edu.umbc.cs.maple.hiergen.CAT;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;

import java.util.*;

public class CATrajectory {

    public static final String CAT_PSEUDOACTION_START = "START";
    public static final String CAT_PSEUDOACTION_END = "END";

    protected int start;
    protected int end;
    protected List<Integer> actionInds = null;
    public int lastAction;

    protected String[] actions;
    protected List<CausalEdge> edges;
    protected Set[] checkedVariables;
    protected Set[] changedVariables;
    protected Episode baseTrajectory;

    public CATrajectory() {
        this.edges = new ArrayList<>();
    }

    //parent structure  action -> variable/ R(reward) -> relevant var
    public void annotateTrajectory(Episode e, Map<String, Map<String, VariableTree>> decisions, FullModel model) {
        baseTrajectory = e;
        int numActions = e.actionSequence.size() + 2; // two pseudoactions
        actions = new String[numActions];

        // there are always two pseudoactions, START and END bookending the action trajectory
        actions[0] = CAT_PSEUDOACTION_START;
        actions[numActions - 1] = CAT_PSEUDOACTION_END;

        // the rest of the actions are sandwiched in between START and END in order
        int offset = 1;
        for (int i = 0; i < e.actionSequence.size(); i++) {
            Action action = e.actionSequence.get(i);
            actions[i + offset] = action.actionName();
        }

        checkedVariables = new Set[numActions];
        changedVariables = new Set[numActions];

        for (int i = 0; i < actions.length; i++) {
            String action = actions[i];
            checkedVariables[i] = new HashSet<String>();
            changedVariables[i] = new HashSet<String>();

            // both pseudoaction always check/change the variables from the starting state (?)
            if (action.equals(CAT_PSEUDOACTION_START) || action.equals(CAT_PSEUDOACTION_END)) {
                State s = e.stateSequence.get(0);
                OOState oos = (OOState) s;
                for (Object variableKey : oos.variableKeys()) {
                    String variable = variableKey.toString();
                    checkedVariables[i].add(variable);
                    changedVariables[i].add(variable);
                }
                continue;
            }

            State state = e.stateSequence.get(i - 1);
            Action action = e.actionSequence.get(i - 1);
            State sPrime = e.stateSequence.get(i);

            //add the vars which were used to get reward
            VariableTree rewardTree = decisions.get(action).get("R");
            List<String> rewardChecked = rewardTree.getCheckedVariables(s);
            checkedVariables[i].addAll(rewardChecked);

            for (Object var : s.variableKeys()) {
                Object sVal = s.get(var);
                boolean changed = false;
                List<TransitionProb> transitions = model.transitions(s, a);
                for (TransitionProb tp : transitions) {
                    if (tp.p > 0) {
                        Object spVal = tp.eo.op.get(var);
                        if (!sVal.equals(spVal))
                            changed = true;
                    }
                }

                if (changed) {
                    changedVariables[i].add(var.toString());
                    VariableTree varTree = decisions.get(action).get(var.toString());
                    List<String> chexked = varTree.getCheckedVariables(s);
                    checkedVariables[i].addAll(chexked);
                }
            }
        }

        //created edges - a changes x, b checks x, and x is not changed by action in between
        for (int i = 0; i < actions.size() - 1; i++) {
            for (String var : changedVariables[i]) {
                int end = i + 1;
                boolean createEdge = true;
                while (!checkedVariables[end].contains(var)) {
                    if (changedVariables[end].contains(var)) {
                        createEdge = false;
                    }
                    end++;
                }
                if (createEdge) {
                    edges.add(new CausalEdge(i, end, var));
                }
            }
        }
        end = actions.size() - 1;
        lastAction = actions.size() - 1;
        actionInds = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            actionInds.add(i);
        }
    }

    public int findEdge(int s, String variable) {
        for (CausalEdge edge : edges) {
            if (edge.getStart() == s && edge.getRelavantVariable().equals(variable)) {
                return edge.getEnd();
            }
        }
        return -1;
    }

    public List<Integer> findEdges(int s) {
        List<Integer> ai = null;
        for (CausalEdge edge : edges) {
            if (edge.getStart() == s) {
                if (ai == null)
                    ai = new ArrayList<>();
                ai.add(edge.getEnd());
            }
        }

        return ai;
    }

    public List<CausalEdge> findCausalEdges(int s) {
        List<CausalEdge> ai = null;
        for (CausalEdge edge : edges) {
            if (edge.getStart() == s) {
                if (ai == null)
                    ai = new ArrayList<>();
                ai.add(edge);
            }

        }

        return ai;
    }

    public List<Integer> reverseFindEdges(int end) {
        List<Integer> ai = null;
        for (CausalEdge edge : edges) {
            if (edge.getEnd() == end) {
                if (ai == null)
                    ai = new ArrayList<Integer>();
                ai.add(edge.getStart());
            }
        }

        return ai;
    }

    public List<Integer> reverseFindEdges(int end, String variable) {
        List<Integer> ai = null;
        for (CausalEdge edge : edges) {
            if (edge.getEnd() == end && edge.getRelavantVariable().equals(variable)) {
                if (ai == null)
                    ai = new ArrayList<Integer>();
                ai.add(edge.getStart());
            }
        }
        return ai;
    }

    public int actionCount() {
        if (baseTrajectory == null) {
            return 0;
        } else if (actionInds != null) {
            return actionInds.size();
        } else {
            //return actions.size();
            return baseTrajectory.actionSequence.size();
        }
    }

    public List<String> uniqueActions() {
        List<String> uniqActs = new ArrayList<>();
        for (String a : actions) {
            if (!uniqActs.contains(a))
                uniqActs.add(a);
        }
        return uniqActs;
    }

    public int edges() {
        if (actions != null)
            return actions.size();
        return 0;
    }

    @Override
    public String toString() {
        String out = "";
        if (actions.size() == 0) {
            out = "No actiond";
        } else {
            out = "Actions: ";
            for (String a : actions) {
                out += a + " ";
            }
            out += "\n";

            for (CausalEdge edge : edges) {
                out += actions.get(edge.getStart()) + " " +
                        actions.get(edge.getEnd()) + " " +
                        edge.getRelavantVariable() + "\n";
            }
        }
        return out;
    }

    public State getState(int index) {
        return baseTrajectory.stateSequence.get(index);
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public List<CausalEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<CausalEdge> edges) {
        this.edges = edges;
    }

    public Set<String>[] getCheckedVariables() {
        return checkedVariables;
    }

    public void setCheckedVariables(Set<String>[] checkedVariables) {
        this.checkedVariables = checkedVariables;
    }

    public Set<String>[] getChangedVariables() {
        return changedVariables;
    }

    public void setChangedVariables(Set<String>[] changedVariables) {
        this.changedVariables = changedVariables;
    }

    public Episode getBaseTrajectory() {
        return baseTrajectory;
    }

    public void setBaseTrajectory(Episode baseTrajectory) {
        this.baseTrajectory = baseTrajectory;
    }

    public List<Integer> getActionInds() {
        return actionInds;
    }

    public void setActionInds(List<Integer> actionInds) {
        this.actionInds = actionInds;
    }

    public SubCAT getSub() {
        return sub;
    }

    public void setSub(SubCAT sub) {
        this.sub = sub;
    }

    public static CATrajectory subCATToCAT(SubCAT sc) {
        CATrajectory traj = sc.getCAT();
        traj.setSub(sc);
        ArrayList<CausalEdge> convertEdges = new ArrayList<>();
        for (Integer i : sc.getActionInds()) {
            List<CausalEdge> tempEdges = traj.findCausalEdges(i);
            for (CausalEdge ce : tempEdges) {
                convertEdges.add(ce);
            }
        }
        traj.setEdges(convertEdges);
        traj.setActionInds(sc.getActionInds());
        return traj;
    }

    public CATrajectory getUltimateActions(Map<OOVariableKey, Object> goal) {
        ArrayList<Integer> actionIndexes = new ArrayList<>();
        if (lastAction == 0)
            return null;
        if (sub != null) {
            actionIndexes.add(lastAction);
            return new SubCAT(lastAction, lastAction, actionIndexes, new ArrayList<>(goal.keySet()), this);
        } else {
            actionIndexes.add(lastAction - 1);
            return new SubCAT(lastAction - 1, lastAction - 1, actionIndexes, new ArrayList<>(goal.keySet()), this);
        }
    }

    public CATrajectory getNonUltimateActions() {
        ArrayList<Integer> actionInds = new ArrayList<>();
        for (int i = 0; i < lastAction; i++) {
            actionInds.add(i);
        }
        SubCAT antiLast = new SubCAT(0, lastAction - 1, actionInds, null, this);

        return antiLast;
    }

    public void setEnd(int end) {
        this.end = end;
        this.lastAction = end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }
}
