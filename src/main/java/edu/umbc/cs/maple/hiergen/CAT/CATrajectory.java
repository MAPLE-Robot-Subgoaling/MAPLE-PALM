package edu.umbc.cs.maple.hiergen.CAT;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class CATrajectory {

    public static final String CAT_PSEUDOACTION_START = "START";
    public static final String CAT_PSEUDOACTION_END = "END";
    public static final String CAT_REWARD_NAME = "R";

//    protected int start;
//    protected int end;
//    protected List<Integer> actionInds = null;
//    public int lastAction;

    protected String name;
    protected String[] actions;
    protected Set<CausalEdge> edges;
    protected Set<String>[] checkedVariables;
    protected Set<String>[] changedVariables;
    protected Episode baseTrajectory;

    public CATrajectory() {
        this(null);
    }

    public CATrajectory(String name) {
        this.name = name;
        this.edges = new TreeSet<>();
    }

    public CATrajectory(CATrajectory original, InvertedSubCAT range) {
        this.name = original.name + "_x";
        Set<Integer> indexes = range.getPrecedingIndexes();
        this.actions = new String[indexes.size()];
        for (int index : indexes) {
            this.actions[index] = original.actions[index];
        }
        this.edges = new TreeSet<>();
        for (CausalEdge edge : original.edges) {
            int edgeStart = edge.getStart();
            int edgeEnd = edge.getEnd();
            if (indexes.contains(edgeStart) && indexes.contains(edgeEnd)) {
                this.edges.add(edge);
            }
        }
        this.baseTrajectory = original.baseTrajectory;
        // TODO: these may need to be recomputed...
        this.changedVariables = original.changedVariables;
        this.checkedVariables = original.checkedVariables;
    }

    //parent structure  action -> variable/ R(reward) -> relevant var
    public void annotateTrajectory(Episode e, Map<String, Map<String, VariableTree>> decisions, FullModel model) {
        baseTrajectory = e;
        int numRealActions = e.actionSequence.size();
        int numAllActions = numRealActions + 2; // two pseudoactions
        actions = new String[numAllActions];

        // there are always two pseudoactions, START and END bookending the action trajectory
        actions[0] = CAT_PSEUDOACTION_START;
        actions[numAllActions - 1] = CAT_PSEUDOACTION_END;

        // the rest of the actions are sandwiched in between START and END in order
        int offset = 1;
        for (int i = 0; i < e.actionSequence.size(); i++) {
            Action action = e.actionSequence.get(i);
            actions[i + offset] = action.actionName();
        }

        checkedVariables = new Set[numAllActions];
        changedVariables = new Set[numAllActions];

        for (int trajectoryIndex = 0; trajectoryIndex < actions.length; trajectoryIndex++) {
            String actionName = actions[trajectoryIndex];
            checkedVariables[trajectoryIndex] = new HashSet<String>();
            changedVariables[trajectoryIndex] = new HashSet<String>();

            // both pseudoaction always check/change the variables from the starting state (?)
            if (actionName.equals(CAT_PSEUDOACTION_START) || actionName.equals(CAT_PSEUDOACTION_END)) {
                State s = e.stateSequence.get(0);
                OOState oos = (OOState) s;
                for (Object variableKey : oos.variableKeys()) {
                    String variable = variableKey.toString();
                    checkedVariables[trajectoryIndex].add(variable);
                    changedVariables[trajectoryIndex].add(variable);
                }
                continue;
            }

            int stateActionIndex = trajectoryIndex - 1; // offset by one, for START pseudoaction
            State state = e.stateSequence.get(stateActionIndex);
            Action action = e.actionSequence.get(stateActionIndex);
//            State sPrime = e.stateSequence.get(stateActionIndex + 1);

            //add the vars which were used to get reward
            Map<String, VariableTree> treeMap = decisions.get(action.actionName());
            VariableTree rewardTree = treeMap.get(CAT_REWARD_NAME);
            List<String> rewardChecked = rewardTree.getCheckedVariables(state);
            checkedVariables[trajectoryIndex].addAll(rewardChecked);

            for (Object variable : state.variableKeys()) {
                Object valueInState = state.get(variable);
                boolean changed = false;
                List<TransitionProb> transitions = model.transitions(state, action);
                for (TransitionProb tp : transitions) {
                    if (tp.p > 0) {
                        Object valueInStatePrime = tp.eo.op.get(variable);
                        if (!valueInState.equals(valueInStatePrime)) {
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    changedVariables[trajectoryIndex].add(variable.toString());
                    VariableTree tree = treeMap.get(variable.toString());
                    List<String> checked = tree.getCheckedVariables(state);
                    checkedVariables[trajectoryIndex].addAll(checked);
                }
            }
        }

        int numActionsSansFinal = actions.length - 1;
        //created edges - a changes x, b checks x, and x is not changed by action in between
        for (int i = 0; i < numActionsSansFinal; i++) {
            for (String variable : changedVariables[i]) {
                int end = i + 1;
                boolean createEdge = true;
                while (!checkedVariables[end].contains(variable)) {
                    if (changedVariables[end].contains(variable)) {
                        createEdge = false;
                    }
                    end++;
                }
                if (createEdge) {
                    edges.add(new CausalEdge(i, end, variable));
                }
            }
        }
    }

//    public int findEdge(int s, String variable) {
//        for (CausalEdge edge : edges) {
//            if (edge.getStart() == s && edge.getRelevantVariable().equals(variable)) {
//                return edge.getEnd();
//            }
//        }
//        return -1;
//    }

    public List<CausalEdge> findIncomingEdges(int endIndex) {
        List<CausalEdge> ai = new ArrayList<>();
        for (CausalEdge edge : edges) {
            if (edge.getEnd() == endIndex) {
                ai.add(edge);
            }
        }
        return ai;
    }

    public List<CausalEdge> findOutgoingEdges(int startIndex) {
        List<CausalEdge> ai = new ArrayList<>();
        for (CausalEdge edge : edges) {
            if (edge.getStart() == startIndex) {
                ai.add(edge);
            }
        }
        return ai;
    }

    public List<Integer> findEdges(int startIndex) {
        List<Integer> ai = null;
        for (CausalEdge edge : edges) {
            if (edge.getStart() == startIndex) {
                if (ai == null) { ai = new ArrayList<>(); }
                ai.add(edge.getEnd());
            }
        }
        return ai;
    }

//    public List<CausalEdge> findCausalEdges(int s) {
//        List<CausalEdge> ai = null;
//        for (CausalEdge edge : edges) {
//            if (edge.getStart() == s) {
//                if (ai == null)
//                    ai = new ArrayList<>();
//                ai.add(edge);
//            }
//
//        }
//
//        return ai;
//    }
//
//    public List<Integer> reverseFindEdges(int end) {
//        List<Integer> ai = null;
//        for (CausalEdge edge : edges) {
//            if (edge.getEnd() == end) {
//                if (ai == null)
//                    ai = new ArrayList<Integer>();
//                ai.add(edge.getStart());
//            }
//        }
//
//        return ai;
//    }
//
//    public List<Integer> reverseFindEdges(int end, String variable) {
//        List<Integer> ai = null;
//        for (CausalEdge edge : edges) {
//            if (edge.getEnd() == end && edge.getRelevantVariable().equals(variable)) {
//                if (ai == null)
//                    ai = new ArrayList<Integer>();
//                ai.add(edge.getStart());
//            }
//        }
//        return ai;
//    }
//
//    public int actionCount() {
//        if (baseTrajectory == null) {
//            return 0;
//        } else if (actionInds != null) {
//            return actionInds.size();
//        } else {
//            //return actions.size();
//            return baseTrajectory.actionSequence.size();
//        }
//    }

    public List<String> uniqueActions() {
        List<String> uniqActs = new ArrayList<>();
        for (String a : actions) {
            if (!uniqActs.contains(a))
                uniqActs.add(a);
        }
        return uniqActs;
    }

    @Override
    public String toString() {
        String out = "";
        if (actions.length == 0) {
            out = "No actions";
        } else {
            out = "Actions: ";
            for (String a : actions) {
                out += a + " ";
            }
            out += "\n";

            for (CausalEdge edge : edges) {
                out += actions[(edge.getStart())] + " " +
                        actions[(edge.getEnd())] + " " +
                        edge.getRelevantVariable() + "\n";
            }
        }
        return out;
    }

    public Set<String> getNontrivialCheckedVariables() {
        return getNontrivialVariables(checkedVariables);
    }

    public Set<String> getNontrivialChangedVariable() {
        return getNontrivialVariables(changedVariables);
    }

    protected Set<String> getNontrivialVariables(Set<String>[] setOfVariables) {
        int skipStart = 1; // don't read the 0th entry
        int skipEnd = setOfVariables.length-1; // don't read the last entry
        Set<String> nontrivialVariables = new HashSet<>();
        for (int i = skipStart; i < skipEnd; i++ ) {
            Set<String> variables = setOfVariables[i];
            nontrivialVariables.addAll(variables);
        }
        return nontrivialVariables;
    }

    public State getUltimateState() {
        int lastIndex = baseTrajectory.stateSequence.size() - 1;
        return baseTrajectory.stateSequence.get(lastIndex);
    }

    public State getState(int index) {
        return baseTrajectory.stateSequence.get(index);
    }

    public String[] getActions() {
        return actions;
    }

    public void setActions(String[] actions) {
        this.actions = actions;
    }

    public Set<CausalEdge> getEdges() {
        return edges;
    }

    public void setEdges(Set<CausalEdge> edges) {
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

//    public int getLastRealActionIndex() {
//        return actions.length - 2;
//    }

    public int getStartIndex() {
        return 0;
    }

    public int getEndIndex() {
        return actions.length - 1;
    }

    public String serialize(){
        Yaml yaml = new Yaml();
        String yamlOut = yaml.dump(this);
        return yamlOut;
    }

    public static CATrajectory read(String path) {
        String fcont = null;
        try{
            fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
        }catch(Exception E){
            System.out.println(E);
        }
        Yaml yaml = new Yaml();
        CATrajectory cat = (CATrajectory)yaml.load(fcont);
        return cat;
    }

    public void write(String path){

        File f = (new File(path)).getParentFile();
        if(f != null){
            f.mkdirs();
        }


        try{

            String str = this.serialize();
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(str);
            out.close();


        }catch(Exception e){
            System.out.println(e);
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
