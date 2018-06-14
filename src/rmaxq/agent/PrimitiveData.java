package rmaxq.agent;

import burlap.statehashing.HashableState;
import hierarchy.framework.GroundedTask;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveData extends RMAXQStateData {

    private RmaxQLearningAgent agent;

    //n(s,a)
    private Integer stateActionCount;

    //n(s,a,s')
    private HashMap<HashableState, Integer> totalTransitionCount;

    //r(s,a,s')
    private Map<HashableState, Double> totalReward;

    public PrimitiveData(RmaxQLearningAgent agent, RMAXQTaskData taskData, HashableState hs) {
        super(taskData, hs);
        this.totalReward = new HashMap<>();
        this.totalTransitionCount = new HashMap<>();
        this.stateActionCount = 0;
        this.agent = agent;
    }

    public Integer getStateActionCount() {
        return stateActionCount;
    }

    public Integer getTotalTransitionCountForState(HashableState hsPrime) {
        return totalTransitionCount.get(hsPrime);
    }

    public Double getTotalRewardForState(HashableState hsPrime) {
        return totalReward.get(hsPrime);
    }

    @Override
    public double getP(HashableState hsPrime) {

        // if below threshold, treat as unreachable
        if (stateActionCount <= agent.getThreshold()) {
            return 0.0;
        }

        // otherwise, approximate it based on previous experience
        int countForThisTransition = totalTransitionCount.get(hsPrime);
        double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
        return approximateTransitionProbability;
    }

    @Override
    public double getR(HashableState hsPrime) {

        // if below threshold, treat as ideal
        if (stateActionCount <= agent.getThreshold()) {
            return agent.getVMax();
        }

        double rewardTotalForThisTransition = totalReward.get(hsPrime);
        double approximateReward = rewardTotalForThisTransition / (1.0 * stateActionCount);
        return approximateReward;

    }

    public void updateTotalReward(HashableState hsPrime, double reward) {
        double total = totalReward.get(hsPrime);
        total = total + reward;
        totalReward.put(hsPrime, total);
    }

    public void incrementStateActionCount(int amount) {
        stateActionCount = stateActionCount + amount;
    }

    public void incrementTotalTransitionCount(HashableState hsPrime, int amount) {
        int transitionCount = totalTransitionCount.get(hsPrime);
        transitionCount = transitionCount + amount;
        totalTransitionCount.put(hsPrime, transitionCount);
    }


//    // R^a(s,s') <- r(s,a,s') / n(s,a,s')
//    // only primitives
//    private void setReward_eq6(RMAXQStateData taskStatePair, HashableState hsPrime) {
////        int stateActionCount = taskStatePair.getStateActionCount();
//        Map<HashableState,Integer> transitions = taskStatePair.getTotalTransitionCount();
//        int stateActionSPrimeCount = transitions.containsKey(hsPrime) ? transitions.get(hsPrime) : 0;
//        if (stateActionSPrimeCount == 0) {
//            return;
//        }
//        double totalReward =  taskStatePair.getTotalReward(hsPrime);
//        double approximateReward = totalReward / (1.0 * stateActionSPrimeCount);
//        int k = 1;
//        taskStatePair.setStoredRewardBySteps(hsPrime, approximateReward, k);
//    }

}
