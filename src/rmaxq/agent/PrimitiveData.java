package rmaxq.agent;

import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveData extends RMAXQStateData {

    //n(s,a)
    private Integer stateActionCount;

    //n(s,a,s')
    private HashMap<HashableState, Integer> totalTransitionCount;

    //r(s,a,s')
    private Map<HashableState, Double> totalReward;

    public PrimitiveData(RMAXQTaskData taskData, HashableState hs) {
        super(taskData, hs);
        this.totalReward = new HashMap<>();
        this.totalTransitionCount = new HashMap<>();
        this.stateActionCount = 0;

    }

    public Integer getStateActionCount() {
        return stateActionCount;
    }


    // P^a(s,sPrime) <- n(s,a,sPrime) / n(s,a)
    // only primitives
//    private void setTransitionProbability_eq7(HashableState hsPrime) {
//        int countForThisTransition = totalTransitionCount.get(hsPrime);
//        if (countForThisTransition <= 0) {
//            return; // sparse -- do not store zero probs
//        }
//        double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
//        int k = 1;
//        double probability = approximateTransitionProbability;
//        setStoredTransitionsBySteps(hsPrime, probability, k);
//    }

    public double getP(HashableState hsPrime) {

        // what if this is below threshold?  should return 0

        int countForThisTransition = totalTransitionCount.get(hsPrime);
        if (countForThisTransition <= 0) {
            return 0.0; // sparse -- do not store zero probs
        }
        double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
        return approximateTransitionProbability;
    }
}
