package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PossibleOutcome;
import edu.umbc.cs.maple.utilities.BurlapConstants;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.ExpectedStepsDiscountProvider;
import edu.umbc.cs.maple.utilities.OnlyInternalDiscountProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RmaxModel extends PALMModel {

    protected Task task;

    protected HashableState hImaginedState;
    /**
     * n(s, a, s') - transitionCount of executing a in s resulted in s'
     * and
     * r(s, a, s') - total sum of rewardTotal received after taking a in s and going to s'
     * and
     * the approximate model
     */
    protected Map<HashableStateActionPair, Map<HashableState, PossibleOutcome>> approximateTransitions;

    /**
     * the rmax sample parameter
     */
    protected int mThreshold;

    /**
     * the provided hashing factory
     */
    protected HashableStateFactory hashingFactory;


    /**
     * the max rewardTotal for the domain
     */
    protected double rmax;

    protected DiscountProvider discountProvider;

    /**
     * creates a rmax model
     * @param task the grounded task to model
     * @param threshold rmax sample threshold
     * @param rmax max rewardTotal in domain
     * @param hsf provided hashing factory
     */
    public RmaxModel(Task task, int threshold, double rmax, HashableStateFactory hsf, double gamma) {
        this.task = task;
        this.hashingFactory = hsf;
        this.mThreshold = threshold;
        this.approximateTransitions = new HashMap<>();
        this.rmax = rmax;
        this.initializeDiscountProvider(gamma);
    }

    public abstract void initializeDiscountProvider(double gamma);

    @Override
    public EnvironmentOutcome sample(State s, Action a) {
        List<TransitionProb> tps = transitions(s, a);
        double sample = RandomFactory.getMapped(0).nextDouble();
        double sum = 0;
        for(TransitionProb tp : tps){
            sum += tp.p;
            if(sample < sum){
                return tp.eo;
            }
        }

//        transitions(s, a);
//        throw new RuntimeException("Probabilities do not sum to 1.0 in RmaxModel");
        return sample(s,a);

        // IMPORTANT:
        // with the Multi-time model, the totalProbability will not sum to 1.0 but to GAMMA, or less even
//        throw new RuntimeException("Probabilities don't sum to 1.0: " + sum);
        // thus, handle edge case here
//        sample = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX).nextDouble()*sum;
//        sum = 0;
//        for(TransitionProb tp : tps){
//            sum += tp.p;
//            if(sample <= sum){
//                return tp.eo;
//            }
//        }
//        throw new RuntimeException("Error: incorrect RNG logic inside RmaxModel");
    }

    //the model is terminal if the task is completed or if it fails, or is the imagined state
    @Override
    public boolean terminal(State s) {
        boolean failOrComplete = task.isFailure(s, params, true) || task.isComplete(s, params, true);
        if (failOrComplete) { return true; }
        if (hImaginedState == null) { return false; }
        HashableState hs = hashingFactory.hashState(s);
        boolean isImaginedState = hImaginedState.equals(hs);
        if (isImaginedState) { return true; }
        return false;
    }

    // the transitions come from the recorded rewards and probabilities in the maps
    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        HashableState hs = this.hashingFactory.hashState(s);
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
        List<TransitionProb> tps = new ArrayList<>();
        int transitionCount = getStateActionCount(hsPrimeToOutcomes, hs, a);
        if (transitionCount < mThreshold || hsPrimeToOutcomes.size() < 1) {
            TransitionProb imaginedTransition = makeImaginedTransition(hs, a);
            tps.add(imaginedTransition);
            return tps;
        }
        double totalProbability = 0.0;
        for (HashableState hsPrime : hsPrimeToOutcomes.keySet()) {
            PossibleOutcome outcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
            TransitionProb probability = outcome.getProbability();
            if (outcome.getOutcome() != probability.eo) {
                throw new RuntimeException("ERROR: the EnvironmentOutcome stored in the TransitionProb was not identical to the one in its own PossibleOutcome");
            }
            tps.add(probability);
            totalProbability += probability.p;
        }
        // IMPORTANT:
        // with the Multi-time model, the totalProbability will not sum to 1.0 but to GAMMA, or less even
        // the rationale for this is explained in Jong's RMAXQ paper (essentially the remainder is prob. of termination)
//        if (totalProbability != 1.0 && totalProbability != getDiscountProvider().getGamma()) {
//            System.err.println("total probability does not sum to 1.0: " + totalProbability);
//        }
        return tps;
    }

    /**
     * updates the model counts, rewards and probabilities given the
     * information in the outcome
     * @param result the outcome of the latest action specific to the task rewards and abstractions
     */
    public boolean updateModel(EnvironmentOutcome result, int stepsTaken, String[] params){

        // set any parameterized variables for this model (from the grounding of the task)
        setParams(params);

        HashableState hs = this.hashingFactory.hashState(result.o);
        double reward = result.r;
        Action a = result.a;
        HashableState hsPrime = this.hashingFactory.hashState(result.op);
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);

        //add to the transitionCount the information in the outcome
        PossibleOutcome outcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
        int newTransitionCountSASP = outcome.getTransitionCount(stepsTaken) + 1;
        double newRewardTotalSASP = outcome.getRewardTotal(stepsTaken) + reward;

        // update the totals for this transition
        outcome.setTransitionCount(stepsTaken, newTransitionCountSASP);
        outcome.setRewardTotal(stepsTaken, newRewardTotalSASP);

        int stateActionCount = getStateActionCount(hsPrimeToOutcomes, hs, a);
        boolean atOrBeyondThreshold = stateActionCount >= mThreshold;
        if (atOrBeyondThreshold) {
            updateApproximationModels(hsPrimeToOutcomes, hs, a, hsPrime, outcome, stateActionCount);
            // remove imagined transition
            hsPrimeToOutcomes.remove(hImaginedState);
        } else {
            hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
            double ignoreR = 0.0;
            double ignoreP = 0.0;
            double imaginedR = rmax;
            double imaginedP = 1.0;
            outcome.setReward(ignoreR);
            outcome.setTransitionProbability(ignoreP);
            for(HashableState otherHsPrime : hsPrimeToOutcomes.keySet()) {
                if (otherHsPrime.equals(hsPrime)) {
                    // this is the transition we just updated, so skip it
                    continue;
                } else if (otherHsPrime.equals(hImaginedState)) {
                    PossibleOutcome imaginedOutcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, otherHsPrime);
                    imaginedOutcome.setReward(imaginedR);
                    imaginedOutcome.setTransitionProbability(imaginedP);
                    continue;
                }
                PossibleOutcome otherOutcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, otherHsPrime);
                otherOutcome.setReward(ignoreR);
                otherOutcome.setTransitionProbability(ignoreP);
            }
        }
        setParams(null);
        return atOrBeyondThreshold;
    }

    @Override
    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }

    protected void updateApproximationModels(Map<HashableState, PossibleOutcome> hsPrimeToOutcomes, HashableState hs, Action a, HashableState thisHsPrime, PossibleOutcome thisOutcome, int stateActionCount) {

        updateApproximateModelFor(thisOutcome, stateActionCount);

        // update the approximate model for the other known possible transitions
        for(HashableState hsPrime : hsPrimeToOutcomes.keySet()) {
            if (hsPrime.equals(thisHsPrime)) {
                // this is the transition we just updated, so skip it
                continue;
            }
            PossibleOutcome otherOutcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
            if (otherOutcome.isVisitedAtLeastOnce()) {
                updateApproximateModelFor(otherOutcome, stateActionCount);
            }
        }
    }

    protected void updateApproximateModelFor(PossibleOutcome outcome, int transitionCountSA) {
        Map<Integer, Integer> stepsTakenToTransitionCount = outcome.getStepsTakenToTransitionCount();
        Map<Integer, Double> stepsTakenToRewardTotal = outcome.getStepsTakenToRewardTotal();
        double transitionCountSASP = 0.0;
        double probabilitySASP = 0.0;
        double rewardTotalSASP = 0.0;
        for (int k : stepsTakenToTransitionCount.keySet()) {

            // approximate the transition probability
            int transitionCountSASPK = stepsTakenToTransitionCount.get(k);
            transitionCountSASP += transitionCountSASPK;
            double probabilitySASPK = transitionCountSASPK / (1.0 * transitionCountSA);
            double discountProbability = getInternalDiscountProbability(outcome.getOutcome(), k);
            double discountedProbability = probabilitySASPK * discountProbability;
            probabilitySASP += discountedProbability;

            // approximate the reward
            double rewardTotalSASPK = stepsTakenToRewardTotal.get(k);
            double discountReward = getInternalDiscountReward(outcome.getOutcome(), k);
            rewardTotalSASPK *= discountReward;
            rewardTotalSASP += rewardTotalSASPK;
        }
//        double transitionProbabilitySASP = transitionCountSASP / (1.0 * transitionCountSA);
        outcome.setTransitionProbability(probabilitySASP);

        double rewardEstimateSASP = rewardTotalSASP / transitionCountSASP;
        outcome.setReward(rewardEstimateSASP);
    }

    // allows for Multi-time model discounting
    public abstract double getInternalDiscountProbability(EnvironmentOutcome eo, int k);
    public abstract double getInternalDiscountReward(EnvironmentOutcome eo, int k);

    public int getStateActionCount(State s, Action a) {
        HashableState hs = hashingFactory.hashState(s);
        Map<HashableState,PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
        int stateActionCount = getStateActionCount(hsPrimeToOutcomes,hs,a);
        return stateActionCount;
    }

    /**
     * get the number of times a was executed in s
     * @param hs current state
     * @param a the action
     * @return n(s, a)
     */
    public int getStateActionCount(Map<HashableState, PossibleOutcome> hsPrimeToOutcomes, HashableState hs, Action a){
        int totalCount = 0;
        if (hsPrimeToOutcomes == null) { return totalCount; }
        for(HashableState hsPrime : hsPrimeToOutcomes.keySet()) {
            PossibleOutcome outcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
            int transitionCount = outcome.getTransitionCountSummation();
            totalCount += transitionCount;
        }
        return totalCount;
    }

    public PossibleOutcome getPossibleOutcome(Map<HashableState, PossibleOutcome> hsPrimeToOutcomes, HashableState hs, Action a, HashableState hsPrime) {
        if (hsPrimeToOutcomes == null) {
            String actionName = StringFormat.parameterizedActionName(a);
            HashableStateActionPair pair = new HashableStateActionPair(hs, actionName);
            hsPrimeToOutcomes = new HashMap<HashableState, PossibleOutcome>();
            approximateTransitions.put(pair, hsPrimeToOutcomes);
        }
        PossibleOutcome outcome = hsPrimeToOutcomes.get(hsPrime);
        if (outcome == null) {
            double initialReward = 0.0;
            double initialProbability = 0.0;
            EnvironmentOutcome eo = new EnvironmentOutcome(hs.s(), a, hsPrime.s(), initialReward, terminal(hsPrime.s()));
            outcome = new PossibleOutcome(hashingFactory, eo, initialProbability);
            hsPrimeToOutcomes.put(hsPrime, outcome);
        }
        return outcome;
    }

    private HashableStateActionPair tempPair = new HashableStateActionPair(null,null);
    protected Map<HashableState, PossibleOutcome> getHsPrimeToOutcomes(HashableState hs, Action a) {
        String actionName = StringFormat.parameterizedActionName(a);
        tempPair.setHs(hs);
        tempPair.setActionName(actionName);
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = approximateTransitions.get(tempPair);
        tempPair.setHs(null);
        tempPair.setActionName(null);
        return hsPrimeToOutcomes;
    }

    public TransitionProb makeImaginedTransition(HashableState hs, Action a) {
        if (hImaginedState == null) {
            hImaginedState = createImaginedState(hs);
        }
        EnvironmentOutcome imaginedOutcome = new EnvironmentOutcome(hs.s(), a, hImaginedState.s(), rmax, true);
        TransitionProb imaginedTransition = new TransitionProb(1.0, imaginedOutcome);
        return imaginedTransition;
    }

    protected HashableState createImaginedState(HashableState basis) {
        MutableOOState imaginedState = (MutableOOState) basis.s().copy();
        List<ObjectInstance> objectInstances = new ArrayList<ObjectInstance>(imaginedState.objects());
        for (ObjectInstance objectInstance : objectInstances) {
            imaginedState.removeObject(objectInstance.name());
        }
        HashableState hImaginedState = hashingFactory.hashState(imaginedState);
        return hImaginedState;
    }

    /**
     * get the number of action attempts needed for a transition to be known
     * @return the rmax m coefficient
     */
    public int getThreshold(){
        return mThreshold;
    }

    public double getRmax() { return rmax; }

    @Override
    public boolean isConvergedFor(State s, Action a, State sPrime) {
        int stateTransitionCount = getStateActionCount(s, a);
        return stateTransitionCount >= getThreshold();
    }

}
