package edu.umbc.cs.maple.palm.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.HashMap;
import java.util.Map;

public class PossibleOutcome {

    // reference to the HashableStateFactory used by the given domain
    protected HashableStateFactory hashingFactory;

    // approximated model
    protected EnvironmentOutcome outcome;
    protected TransitionProb transitionProb;

    // totals needed in RMAX
//    protected int transitionCount;
    protected Map<Integer, Integer> stepsTakenToTransitionCount;
    protected Map<Integer, Double> stepsTakenToRewardTotal;

    public PossibleOutcome(HashableStateFactory hashingFactory, EnvironmentOutcome outcome, double probability) {
        this.hashingFactory = hashingFactory;
        this.outcome = outcome;
        this.transitionProb = new TransitionProb(probability, this.outcome);
        this.stepsTakenToTransitionCount = new HashMap<Integer, Integer>();
        this.stepsTakenToRewardTotal = new HashMap<Integer, Double>();
    }

    public double getExpectedNumberOfSteps() {
        double expected = 0.0;
        double totalTranstions = 0.0;
        for (Integer stepsTaken : stepsTakenToTransitionCount.keySet()) {
            Integer occurrence = stepsTakenToTransitionCount.get(stepsTaken);
            expected += stepsTaken * occurrence;
            totalTranstions += occurrence;
        }
        if (totalTranstions < 1) {
            return 1.0;
        }
        double estimate = expected / totalTranstions;
        double limit = Math.max(1.0, estimate); // can never have smaller than 1.0 step
        return limit;
    }

    public EnvironmentOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(EnvironmentOutcome outcome) {
        this.outcome = outcome;
        this.transitionProb.eo = this.outcome;
    }

    public HashableStateFactory getHashingFactory() {
        return hashingFactory;
    }

    public void setHashingFactory(HashableStateFactory hashingFactory) {
        this.hashingFactory = hashingFactory;
    }

    public double getTransitionProbability() {
        return transitionProb.p;
    }

    public void setTransitionProbability(double transitionProbability) {
        this.transitionProb.p = transitionProbability;
    }

    public double getReward() {
        return this.outcome.r;
    }

    public void setReward(double reward) {
        this.outcome.r = reward;
    }

    public Map<Integer, Integer> getStepsTakenToTransitionCount() {
        return stepsTakenToTransitionCount;
    }

    public Map<Integer, Double> getStepsTakenToRewardTotal() {
        return stepsTakenToRewardTotal;
    }

    public int getTransitionCountSummation() {
        int sum = 0;
        for (Integer stepsTaken : stepsTakenToTransitionCount.keySet()) {
            sum += stepsTakenToTransitionCount.get(stepsTaken);
        }
        return sum;
    }

    public void setTransitionCount(int stepsTaken, int count) {
        stepsTakenToTransitionCount.put(stepsTaken, count);
    }

    public int getTransitionCount(int stepsTaken) {
        if (!stepsTakenToTransitionCount.containsKey(stepsTaken)) {
            stepsTakenToTransitionCount.put(stepsTaken, 0);
        }
        return stepsTakenToTransitionCount.get(stepsTaken);
    }

    public double getRewardTotal(int stepsTaken) {
        if (!stepsTakenToRewardTotal.containsKey(stepsTaken)) {
            stepsTakenToRewardTotal.put(stepsTaken, 0.0);
        }
        return stepsTakenToRewardTotal.get(stepsTaken);
    }

    public void setRewardTotal(int stepsTaken, double rewardTotal) {
        this.stepsTakenToRewardTotal.put(stepsTaken, rewardTotal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (outcome != null && transitionProb != null && transitionProb.eo != null && outcome != transitionProb.eo) {
            throw new RuntimeException("ERROR: the EnvironmentOutcome stored in the TransitionProb was not identical to the one in its own PossibleOutcome");
        }

        PossibleOutcome that = (PossibleOutcome) o;

        if (transitionProb.p != that.transitionProb.p) {
            return false;
        }

        if (hashingFactory != null ? !hashingFactory.equals(that.hashingFactory) : that.hashingFactory != null) {
            return false;
        }

        // handle equality by the State x Action x StatePrime components of the outcome, hashed
        HashableState hs = hashingFactory.hashState(outcome.o);
        HashableState thatHs = that.hashingFactory.hashState(that.outcome.o);
        if(hs != null ? !hs.equals(thatHs) : thatHs != null) {
            return false;
        }

        Action action = outcome.a;
        Action thatAction = that.outcome.a;
        if(action != null ? !action.equals(thatAction) : thatAction != null) {
            return false;
        }

        HashableState hsPrime = hashingFactory.hashState(outcome.op);
        HashableState thatHsPrime = that.hashingFactory.hashState(that.outcome.op);
        if(hsPrime != null ? !hsPrime.equals(thatHsPrime) : thatHsPrime != null) {
            return false;
        }

        Double r = outcome.r;
        Double thatR = that.outcome.r;
        if(!r.equals(thatR)) {
            return false;
        }

        Boolean terminated = outcome.terminated;
        Boolean thatTerminated = that.outcome.terminated;
        if(!terminated.equals(thatTerminated)) {
            return false;
        }

        if (stepsTakenToTransitionCount != null ? !stepsTakenToTransitionCount.equals(that.stepsTakenToTransitionCount) : that.stepsTakenToTransitionCount != null) {
            return false;
        }

        if (stepsTakenToRewardTotal != null ? !stepsTakenToRewardTotal.equals(that.stepsTakenToRewardTotal) : that.stepsTakenToRewardTotal != null) {
            return false;
        }

        return true;
    }

    public EnvironmentOutcome getPossibleOutpume(){
        return outcome;
    }

    public TransitionProb getProbability(){
        return transitionProb;
    }
    @Override
    public int hashCode() {
        // hashes and collides on hashedState, action, hashedStatePrime
//        int result = hashingFactory != null ? hashingFactory.hashCode() : 0;
//        HashableState hs = hashingFactory.hashState(outcome.o);
//        Action action = outcome.a;
//        HashableState hsPrime = hashingFactory.hashState(outcome.op);
//        result = 31 * result + (hs != null ? hs.hashCode() : 0);
//        result = 31 * result + (action != null ? action.hashCode() : 0);
//        result = 31 * result + (hsPrime != null ? hsPrime.hashCode() : 0);
//        return result;
        throw new RuntimeException("not implemented");
    }

    @Override
    public String toString() {
        String out = "PossibleOutcome{" +
                "a=" + outcome.a +
                ", p=" + getTransitionProbability() +
                ", r=" + getReward() +
                ", {";
        for (Integer stepsTaken : stepsTakenToTransitionCount.keySet()) {
            out += "(k="+stepsTaken;
            out += ", tc="+stepsTakenToTransitionCount.get(stepsTaken);
            out += ")";
        }
        out += "}, {";
        for (Integer stepsTaken : stepsTakenToRewardTotal.keySet()) {
            out += "(k="+stepsTaken;
            out += ", rt="+stepsTakenToRewardTotal.get(stepsTaken);
            out += ")";
        }
        out += "}" +
                ", s=" + outcome.o +
                ", sp=" + outcome.op +
                '}';
        return out;
    }

    public boolean isVisitedAtLeastOnce() {
        return this.stepsTakenToTransitionCount.keySet().size() > 0;
    }
}
