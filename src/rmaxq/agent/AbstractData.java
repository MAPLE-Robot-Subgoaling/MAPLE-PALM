package rmaxq.agent;

import burlap.behavior.valuefunction.QProvider;
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

    public AbstractData(RMAXQTaskData taskData, HashableState hs) {
        super(taskData, hs);
        this.storedRewardBySteps = new HashMap<>();
        this.storedTransitionsBySteps = new HashMap<>();
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

    @Override
    public double getR(HashableState hsPrime) {

        // TODO

        return 0.0;
    }

    public GroundedTask selectNextAction() {
        RMAXQTaskData taskData = getTaskData();
        GroundedTask action = taskData.selectActionForState(getHs());
        return action;
    }
}
