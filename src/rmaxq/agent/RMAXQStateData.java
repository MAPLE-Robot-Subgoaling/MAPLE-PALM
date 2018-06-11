package rmaxq.agent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RMAXQStateData {

    public static double DEFAULT_INITIAL_Q_VALUE = 0.0;

    //R^a(s) for non-primitive tasks
    private HashMap<HashableState, Double> storedReward;

    //P^a(s, s') for non-primitive tasks
    private HashMap<HashableState, HashMap<Integer, Double>> storedTransitionsBySteps;

    // V(s)
    private Double storedValue;

    // Q(s,a)
    private HashMap<GroundedTask, Double> storedQValues;

    // pi(s)
    private GroundedTask storedPolicyAction;

    //n(s,a)
    private Integer stateActionCount;

    //n(s,a,s')
    private HashMap<HashableState, Integer> totalTransitionCount;

    //r(s,a,s')
    private Map<HashableState,Double> totalReward;

    private final RMAXQTaskData taskData;
    private final GroundedTask task;
    private final HashableState hs;

    public RMAXQStateData (RMAXQTaskData taskData, GroundedTask task, HashableState hs, double initialValue) {
        this.taskData = taskData;
        this.task = task;
        this.hs = hs;
        this.storedTransitionsBySteps = new HashMap<>();
        this.storedQValues = new HashMap<>();
        this.totalTransitionCount = new HashMap<>();
        this.storedValue = initialValue;
        this.storedPolicyAction = null;
        this.totalReward = new HashMap<>();
        this.storedReward = new HashMap<>();
        this.stateActionCount = 0;

    }

//    public Map<HashableState, Double> getStoredReward() {
//        return storedReward;
//    }
    public double getStoredReward(HashableState hsPrime) {
        return storedReward.containsKey(hsPrime) ? storedReward.get(hsPrime) : 0.0;
    }
//    public void setStoredReward(HashMap<HashableState,Double> storedReward) {
//        this.storedReward = storedReward;
//    }
    public void setStoredReward(HashableState hsPrime, double reward) {
        if (Double.isNaN(reward)) {
            System.out.println("NaN reward");
        }
        this.storedReward.put(hsPrime,reward);
    }

    public HashMap<HashableState, HashMap<Integer, Double>> getStoredTransitionsBySteps() {
        return storedTransitionsBySteps;
    }

    public Double getStoredTransitionProbabilityForSteps(HashableState hsPrime, int k) {
//        if (initialize) {
            Map<Integer,Double> map = storedTransitionsBySteps.computeIfAbsent(hsPrime, i -> new HashMap<>());
            Double probability = map.get(k);
            if (probability == null) {
                return 0.0;
            }
            return probability;
    }

    public Double getStoredValue() {
        return storedValue;
    }

    public void setStoredValue(Double storedValue) {
        this.storedValue = storedValue;
    }

    public HashMap<GroundedTask, Double> getStoredQValues() {
        return storedQValues;
    }

    public void setStoredQValues(HashMap<GroundedTask, Double> storedQValues) {
        this.storedQValues = storedQValues;
    }

    public GroundedTask getStoredPolicyAction() {
        return storedPolicyAction;
    }

    public void setStoredPolicyAction(GroundedTask storedPolicyAction) {
        this.storedPolicyAction = storedPolicyAction;
    }

    public Integer getStateActionCount() {
        return stateActionCount;
    }

    public void setStateActionCount(Integer stateActionCount) {
        this.stateActionCount = stateActionCount;
    }

    public HashMap<HashableState, Integer> getTotalTransitionCount() {
        return totalTransitionCount;
    }

    public void setTotalTransitionCount(HashMap<HashableState, Integer> totalTransitionCount) {
        this.totalTransitionCount = totalTransitionCount;
    }

    public Double getTotalReward(HashableState hsPrime) {
        return totalReward.computeIfAbsent(hsPrime, k -> 0.0);
    }

    public void setTotalReward(HashableState hsPrime, Double totalReward) {
        this.totalReward.put(hsPrime, totalReward);
    }

    public void setStoredTransitionsBySteps(HashableState hsPrime, double probability, int k) {
        if (probability == 0) {
            return;
        }
        Map<Integer, Double> map = this.storedTransitionsBySteps.computeIfAbsent(hsPrime, i -> new HashMap<>());
        map.put(k, probability);
    }

    public double getQValue(GroundedTask childTask) {
//        double qValue = this.storedQValues.computeIfAbsent(childTask, i -> 0.0);
        if (storedQValues.containsKey(childTask)) {
            return storedQValues.get(childTask);
        }
        return DEFAULT_INITIAL_Q_VALUE;
    }

    public int getTotalTransitionCount(HashableState hsPrime) {
//        return totalTransitionCount.computeIfAbsent(hsPrime, i -> 0);
        if (totalTransitionCount.containsKey(hsPrime)) {
            return totalTransitionCount.get(hsPrime);
        }
        return 0;
    }

    public void setTotalTransitionCount(HashableState hsPrime, int transitionCount) {
        totalTransitionCount.put(hsPrime, transitionCount);
    }

    public void setStoredQValues(GroundedTask childTask, double newQ) {
        storedQValues.put(childTask, newQ);
    }

//    public void setStoredExpectedProbability(HashableState hsX, double newP) {
//        storedExpectedProbability.put(hsX, newP);
//    }
//
//    public HashMap<HashableState, Double> getStoredExpectedProbability() {
//        return storedExpectedProbability;
//    }
//
//    public void setStoredExpectedProbability(HashMap<HashableState, Double> storedExpectedProbability) {
//        this.storedExpectedProbability = storedExpectedProbability;
//    }
//
//    public double getStoredExpectedProbability(HashableState hsPrime, boolean initialize) {
//        Double probability;
//        if (initialize) {
//            probability = storedExpectedProbability.computeIfAbsent(hsPrime, i -> 0.0);
//        } else {
//            probability = storedExpectedProbability.get(hsPrime);
//            if (probability == null) {
//                probability = 0.0;
//            }
//        }
//        return probability;
//    }

    public RMAXQTaskData getTaskData() {
        return taskData;
    }

    public GroundedTask getTask() {
        return task;
    }

    public HashableState getHs() {
        return hs;
    }

    @Override
    public String toString() {
        return task.toString() + " " + hs.hashCode();
    }

    public void setStoredPolicy() {
        List<GroundedTask> childTasks = this.getTask().getGroundedChildTasks(getMappedState());
        double maxQ = Double.NEGATIVE_INFINITY;
        List<GroundedTask> maxChildTasks = new ArrayList<>();
        for (GroundedTask childTask : childTasks) {
            double qValue = getQValue(childTask);
            if (qValue > maxQ) {
                maxQ = qValue;
                maxChildTasks.clear();
                maxChildTasks.add(childTask);
            } else if (qValue == maxQ) {
                maxChildTasks.add(childTask);
            }
        }
        int size = maxChildTasks.size();
        int bound = RandomFactory.getMapped(0).nextInt(size);
        // get a random child task among the equally good actions
        GroundedTask maxChildTask = maxChildTasks.get(bound);
        setStoredPolicyAction(maxChildTask);
    }

    private HashMap<GroundedTask, HashMap<HashableState, State>> cachedStateMapping = new HashMap<>();
    public State getMappedState() {
        GroundedTask task = this.getTask();
        HashableState hs = this.getHs();
        Map<HashableState, State> stateMapping = cachedStateMapping.computeIfAbsent(task, i -> new HashMap<>());
        State s = stateMapping.computeIfAbsent(hs, i -> task.mapState(hs.s()));
        return s;
//		return task.mapState(hs.s());
    }

}
