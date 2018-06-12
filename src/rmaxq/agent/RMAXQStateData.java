package rmaxq.agent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.*;

public class RMAXQStateData {

    public static double DEFAULT_INITIAL_Q_VALUE = 0.0;

    // R^a(s, s', k) for non-primitive tasks
    private HashMap<HashableState, HashMap<Integer, Double>> storedRewardBySteps;

    //P^a(s, s', k) for non-primitive tasks
    private HashMap<HashableState, HashMap<Integer, Double>> storedTransitionsBySteps;

    // stored R, expected over k
    private HashMap<HashableState, Double> storedExpectedReward;

    // stored P, expected over k
    private HashMap<HashableState, Double> storedExpectedTransitionProbability;

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
    private Map<HashableState, Double> totalReward;

    private final RMAXQTaskData taskData;
    private final GroundedTask task;
    private final HashableState hs;

    public RMAXQStateData (RMAXQTaskData taskData, GroundedTask task, HashableState hs, double initialValue) {
        this.taskData = taskData;
        this.task = task;
        this.hs = hs;
        this.storedQValues = new HashMap<>();
        this.storedValue = initialValue;
        this.storedPolicyAction = null;
        this.storedRewardBySteps = new HashMap<>();
        this.storedTransitionsBySteps = new HashMap<>();
        this.totalReward = new HashMap<>();
        this.totalTransitionCount = new HashMap<>();
        this.storedExpectedTransitionProbability = new HashMap<>();
        this.storedExpectedReward = new HashMap<>();
        this.stateActionCount = 0;

    }

    public Double getStoredTransitionProbabilityForSteps(HashableState hsPrime, int k) {
        Map<Integer,Double> map = storedTransitionsBySteps.computeIfAbsent(hsPrime, i -> new HashMap<>());
        Double probability = map.get(k);// do not use computeIfAbsent(k, i -> 0.0), must be sparse
        if (probability == null) {
            return 0.0;
        }
        return probability;
    }

    public Double getStoredRewardBySteps(HashableState hsPrime, int k) {
        Map<Integer,Double> map = storedRewardBySteps.computeIfAbsent(hsPrime, i -> new HashMap<>());
        Double reward = map.get(k);
        return reward;
    }

    public void setStoredRewardBySteps(HashableState hsPrime, double reward, int k) {
        Map<Integer, Double> map = this.storedRewardBySteps.computeIfAbsent(hsPrime, i -> new HashMap<>());
        map.put(k, reward);
    }

    public Double getStoredValue() {
        return storedValue;
    }

    public void setStoredValue(Double storedValue) {
        this.storedValue = storedValue;
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

    public Double getTotalReward(HashableState hsPrime) {
        return totalReward.computeIfAbsent(hsPrime, k -> 0.0);
    }

    public void setTotalReward(HashableState hsPrime, Double totalReward) {
        this.totalReward.put(hsPrime, totalReward);
    }

    public void setStoredTransitionsBySteps(HashableState hsPrime, double probability, int k) {
        Map<Integer, Double> map = this.storedTransitionsBySteps.computeIfAbsent(hsPrime, i -> new HashMap<>());
        map.put(k, probability);
    }

    public double getQValue(GroundedTask childTask) {
        if (storedQValues.containsKey(childTask)) {
            return storedQValues.get(childTask);
        }
        return DEFAULT_INITIAL_Q_VALUE;
    }

    public int getTotalTransitionCount(HashableState hsPrime) {
        if (totalTransitionCount.containsKey(hsPrime)) {
            return totalTransitionCount.get(hsPrime);
        }
        return 0;
    }

    public void setTotalTransitionCount(HashableState hsPrime, int transitionCount) {
        totalTransitionCount.put(hsPrime, transitionCount);
    }

    public void setQValue(GroundedTask childTask, double newQ) {
        storedQValues.put(childTask, newQ);
    }


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
        return task.toString() + " " + hs.s().toString();// hs.hashCode();
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
            if (Double.isNaN(qValue) || Double.isInfinite(qValue)) {
                System.err.println("invalid qValue in RMAXQStateData");
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
    }

    public Collection<HashableState> getHSPrimes() {
        return new ArrayList<>(storedTransitionsBySteps.keySet());
    }

    public Map<HashableState,Integer> getTotalTransitionCount() {
        return totalTransitionCount;
    }

    public Double getStoredExpectedTransitionProbabilityForState(HashableState hsPrime) {
        Double p = storedExpectedTransitionProbability.get(hsPrime);
        if (p == null) { return 0.0; }
        return p;
    }

    public Double getStoredExpectedRewardForState(HashableState hsPrime) {
        Double r = storedExpectedReward.get(hsPrime);
        if (r == null) { return 0.0; }
        return r;
    }


    public void setStoredExpectedTransitionProbabilityForState(HashableState hsPrime, double probability) {
        this.storedExpectedTransitionProbability.put(hsPrime, probability);
    }
}
