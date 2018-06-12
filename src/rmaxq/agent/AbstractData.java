package rmaxq.agent;

import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.HashMap;
import java.util.Map;

public class AbstractData extends RMAXQStateData {

    public static double DEFAULT_INITIAL_Q_VALUE = 0.0;

//    // stored R, expected over k
//    private HashMap<HashableState, Double> storedExpectedReward;
//
//    // stored P, expected over k
//    private HashMap<HashableState, Double> storedExpectedTransitionProbability;

    // R^a(s, s', k) for non-primitive tasks
    private HashMap<HashableState, HashMap<Integer, Double>> storedRewardBySteps;

    // P^a(s, s', k) for non-primitive tasks
    private HashMap<HashableState, HashMap<Integer, Double>> storedTransitionsBySteps;

    private Double storedValue;

    // Q(s,a)
    private HashMap<GroundedTask, Double> storedQValues;    // V(s)

    // pi(s)
    private GroundedTask storedPolicyAction;

    public AbstractData(RMAXQTaskData taskData, HashableState hs) {
        super(taskData, hs);
        this.storedValue = DEFAULT_INITIAL_Q_VALUE;
        this.storedQValues = new HashMap<>();
        this.storedPolicyAction = null;
        this.storedRewardBySteps = new HashMap<>();
        this.storedTransitionsBySteps = new HashMap<>();
//        this.storedExpectedTransitionProbability = new HashMap<>();
//        this.storedExpectedReward = new HashMap<>();

    }


    public GroundedTask getStoredPolicyAction() {
        return storedPolicyAction;
    }

    public Double getStoredValue() {
        return storedValue;
    }

    public double getP(HashableState hsPrime) {
        double transitionProbability = 0.0;
        Map<Integer, Double> stepsToProbability = storedTransitionsBySteps.get(hsPrime);
        for (Integer kSteps : stepsToProbability.keySet()) {
            // multi-time model
            double gamma = getTaskData().getGamma();
            double discount = Math.pow(gamma, kSteps);
            double probability = stepsToProbability.get(kSteps);
            double product = discount * probability;
            transitionProbability += product;
            if (transitionProbability > 1.0 || transitionProbability < 0.0) {
                System.err.println("Error: invalid probability");
            }
        }
        return transitionProbability;
    }

}
