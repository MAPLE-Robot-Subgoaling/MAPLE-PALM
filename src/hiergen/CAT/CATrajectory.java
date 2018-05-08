package hiergen.CAT;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import jdk.nashorn.internal.ir.CatchNode;
import scpsolver.graph.Edge;

import java.util.*;

public class CATrajectory {

    protected int start, end;
    protected List<String> actions;
    protected List<Integer> actionInds = null;
    protected SubCAT sub = null;
    protected List<CausalEdge> edges;
    protected Set<String>[] checkedVariables, changedVariables;
    protected Episode baseTrajectory;
    public int lastAction;

    public CATrajectory() {
        this.actions = new ArrayList<String>();
        this.edges = new ArrayList<CausalEdge>();
    }

    //parent structure  action -> variable/ R(reward) -> relevant var
    public void annotateTrajectory(Episode e, Map<String, Map<String, VariableTree>> decisions, FullModel model) {
        baseTrajectory = e;
        actions.add("START");
        for (Action a : e.actionSequence) {
            actions.add(a.actionName());
        }
        actions.add("END");
        lastAction = actions.size() - 1;

        checkedVariables = new Set[actions.size()];
        changedVariables = new Set[actions.size()];

        for (int i = 0; i < actions.size(); i++) {
            String action = actions.get(i);
            checkedVariables[i] = new HashSet<String>();
            changedVariables[i] = new HashSet<String>();

            if (action.equals("START") || action.equals("END")) {
                State s = e.stateSequence.get(0);
                for (Object var : s.variableKeys()) {
                    checkedVariables[i].add(var.toString());
                    changedVariables[i].add(var.toString());
                }
                continue;

            }

            State s = e.stateSequence.get(i - 1);
            Action a = e.actionSequence.get(i - 1);

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
    }

    public int findEdge(int s, String variable) {
        for (CausalEdge edge : edges) {
            if (edge.getStart() == s && edge.getRelavantVariable().equals(variable)) {
                return edge.getEnd();
            }
        }
        return -1;
    }

    public List<Integer> findEdges(int s)
    {
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

    public List<CausalEdge> findCausalEdges(int s)
    {
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

    public List<Integer> reverseFindEdges(int end){
        List<Integer> ai = null;
        for (CausalEdge edge : edges) {
            if (edge.getEnd() == end) {
                if(ai == null)
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
        }
        else if(actionInds != null)
        {
            return actionInds.size();
        }
        else {
            //return actions.size();
            return baseTrajectory.actionSequence.size();
        }
    }

    public int edges()
    {
        if(actions != null)
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

    public List<Integer> getActionInds()
    {
        return actionInds;
    }

    public void setActionInds(List<Integer> actionInds)
    {
        this.actionInds = actionInds;
    }

    public SubCAT getSub() {
        return sub;
    }

    public void setSub(SubCAT sub) {
        this.sub = sub;
    }

    public static CATrajectory subCATToCAT(SubCAT sc)
    {
        CATrajectory traj = sc.getCAT();
        traj.setSub(sc);
        ArrayList<CausalEdge> convertEdges = new ArrayList<>();
        for(Integer i : sc.getActionInds())
        {
            List<CausalEdge> tempEdges = traj.findCausalEdges(i);
            for(CausalEdge ce: tempEdges)
            {
                convertEdges.add(ce);
            }
        }
        traj.setEdges(convertEdges);
        traj.setActionInds(sc.getActionInds());
        return traj;
    }

    public CATrajectory getUltimateActions()
    {
        ArrayList<Integer> actionInds = new ArrayList<>();
        actionInds.add(lastAction);
        SubCAT last = new SubCAT(lastAction, lastAction+1, actionInds, null, this);

        return last;
    }

    public CATrajectory getNonUltimateActions()
    {
        ArrayList<Integer> actionInds = new ArrayList<>();
        for(int i = 0; i < lastAction; i++)
        {
            actionInds.add(i);
        }
        SubCAT antiLast = new SubCAT(0, lastAction-1, actionInds, null, this);

        return antiLast;
    }


}
