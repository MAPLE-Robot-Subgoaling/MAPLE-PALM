package edu.umbc.cs.maple.hiergen.CAT;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import edu.umbc.cs.maple.utilities.Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.IntStream;

public class CATrajectory {

    public static final String CAT_PSEUDOACTION_START = "START";
    public static final String CAT_PSEUDOACTION_END = "END";
    public static final String CAT_REWARD_NAME = "R";

//    protected int start;
//    protected int end;
//    protected List<Integer> actionInds = null;
//    public int lastAction;

    protected String name;
    protected Map<Integer,String> actions;
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

        List<Integer> indexes = new ArrayList<>(range.getPrecedingIndexes());
        Collections.sort(indexes);
        this.actions = new LinkedHashMap<>();
        for (int index : indexes) {
            this.actions.put(index, original.actions.get(index));
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

        // add a pseudoaction for END?

    }

    //parent structure  action -> variable/ R(reward) -> relevant var
    public void annotateTrajectory(Episode e, Map<String, Map<String, VariableTree>> decisions, FullModel model) {
        baseTrajectory = e;
        int numRealActions = e.actionSequence.size();
        int numAllActions = numRealActions + 2; // two pseudoactions
//        actions = new String[numAllActions];
        actions = new LinkedHashMap<>();

//        actions[0] = CAT_PSEUDOACTION_START;
//        actions[numAllActions - 1] = CAT_PSEUDOACTION_END;

        // there are always two pseudoactions, START and END bookending the action trajectory
        actions.put(0, CAT_PSEUDOACTION_START);
        // the rest of the actions are sandwiched in between START and END in order
        int offsetDueToStart = 1;
        for (int i = 0; i < e.actionSequence.size(); i++) {
            Action action = e.actionSequence.get(i);
            int actionIndex = i + offsetDueToStart;
            String actionName = action.actionName();
            actions.put(actionIndex, actionName);
        }
        actions.put(numAllActions - 1, CAT_PSEUDOACTION_END);

        checkedVariables = new Set[numAllActions];
        changedVariables = new Set[numAllActions];

        for (int trajectoryIndex = 0; trajectoryIndex < actions.size(); trajectoryIndex++) {
            String actionName = actions.get(trajectoryIndex);
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

        int numActionsSansFinal = actions.size() - 1;
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

    @Override
    public String toString() {
        StringBuilder out;
        if (actions.size() == 0) {
            out = new StringBuilder("No actions");
        } else {
            out = new StringBuilder("Actions: ");
            List<Integer> indexes = new ArrayList<>(actions.keySet());
            Collections.sort(indexes);
            for (Integer i : indexes) {
                out.append(actions.get(i)).append(" ");
            }
            out.append("\n");

            for (CausalEdge edge : edges) {
                out.append(actions.get((edge.getStart()))).append(" ").append(actions.get((edge.getEnd()))).append(" ").append(edge.getRelevantVariable()).append("\n");
            }
        }
        return out.toString();
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
        int lastRealIndex = getLastRealIndex();
        // just to be overly specific and crystal clear what is going on here
        // we would translate the indexes by -1 to account for START
        // and then we want the state *after* the action, so add +1
        // ... I realize it is a bit much :|
        int translateToBaseTrajectoryIndexes = lastRealIndex - 1;
        int indexOfStateAfterAction = translateToBaseTrajectoryIndexes + 1;
        return baseTrajectory.stateSequence.get(indexOfStateAfterAction);
    }

    public State getState(int index) {
        return baseTrajectory.stateSequence.get(index);
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

    public int getStartIndex() {
        Set<Integer> actionIndexes = Utils.getKeysByValue(actions, CAT_PSEUDOACTION_START);
        if (actionIndexes.size() != 1) {
            throw new RuntimeException("Error: wrong # keys for START pseudoaction");
        }
        return (int) actionIndexes.toArray()[0];
    }

    public int getEndIndex() {
        Set<Integer> actionIndexes = Utils.getKeysByValue(actions, CAT_PSEUDOACTION_END);
        int endIndex;
        if (actionIndexes.size() > 1) {
            throw new RuntimeException("Error: wrong # keys for END pseudoaction");
        } else if (actionIndexes.size() < 1) {
            endIndex = Collections.max(actions.keySet());
        } else {
            endIndex = (int) actionIndexes.toArray()[0];
        }
        return endIndex;
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

    public Map<Integer, String> getActions() {
        return actions;
    }

    public void setActions(Map<Integer, String> actions) {
        this.actions = actions;
    }

    public int getLastIndex() {
        return Collections.max(actions.keySet());
    }

    public int getFirstIndex() {
        return Collections.min(actions.keySet());
    }

    public int getLastRealIndex() {
        return actions.keySet()
                .stream()
                .filter(k -> !actions.get(k).equals(CAT_PSEUDOACTION_START) && !actions.get(k).equals(CAT_PSEUDOACTION_END))
                .max(Integer::compareTo)
                .orElse(Integer.MIN_VALUE);
    }

    public int getFirstRealIndex() {
        return actions.keySet()
                .stream()
                .filter(k -> !actions.get(k).equals(CAT_PSEUDOACTION_START) && !actions.get(k).equals(CAT_PSEUDOACTION_END))
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }
}
