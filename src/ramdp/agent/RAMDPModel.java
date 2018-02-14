package ramdp.agent;

import java.util.*;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.StringFormat;

public class RAMDPModel extends FactoredModel {

    protected HashableState hImaginedState;

    protected Map<HashableStateActionPair, HashMap<HashableState, PossibleOutcome>> approximateTransitions;

	/**
	 * the rmax sample parameter
	 */
	private int mThreshold;
	
	/**
	 * the provided hashing factory
	 */
	private HashableStateFactory hashingFactory;
	
	/**
	 * n(s, a, s') - transitionCount of executing a in s resulted in s'
     * and
     * r(s, a, s') - total sum of rewardTotal received after taking a in s and going to s'
	 */
	private Map<HashableState, Map<String, Map<HashableState, TransitionCountRewardTotalPair>>> stateActionSPrimeCountRewardPairs;
	
	/**
	 * the grounded task that is being modeled
	 */
	private GroundedTask task;
	
	/**
	 * the max rewardTotal for the domain
	 */
	private double rmax;
	
	/**
	 * creates a rmax model
	 * @param task the grounded task to model
	 * @param threshold rmax sample threshold
	 * @param rmax max rewardTotal in domain
	 * @param hs provided hashing factory
	 */
	public RAMDPModel( GroundedTask task, int threshold, double rmax, HashableStateFactory hs) {
		this.hashingFactory = hs;
		this.mThreshold = threshold;
//		this.rewards = new HashMap<HashableState, Map<String, Double>>();
//		this.transitionProbabilities = new HashMap<HashableState, Map<String, Map<HashableState,Double>>>();
        this.approximateTransitions = new HashMap<HashableStateActionPair, HashMap<HashableState, PossibleOutcome>>();
		this.stateActionSPrimeCountRewardPairs = new HashMap<HashableState, Map<String, Map<HashableState, TransitionCountRewardTotalPair>>>();
//		this.stateActionCount = new HashMap<HashableState, Map<String,Integer>>();
//		this.resultingStateCount = new HashMap<HashableState, Map<String, Map<HashableState, Integer>>>();
//		this.totalReward = new HashMap<HashableState, Map<String,Double>>();
		this.task = task;
		this.rmax = rmax;
	}

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
		
		throw new RuntimeException("Probabilities don't sum to 1.0: " + sum);
	}

	//the model is terminal if the task is completed or if it fails, or is the imagined state
	@Override
	public boolean terminal(State s) {
        boolean failOrComplete = task.isFailure(s) || task.isComplete(s);
        if (failOrComplete) { return true; }
        if (hImaginedState == null) { return false; }
	    HashableState hs = hashingFactory.hashState(s);
        boolean isImaginedState = hImaginedState.equals(hs);
        if (isImaginedState) { return true; }
        return false;
	}

    /**
     * get the number of action attempts needed for a transition to be known
     * @return the rmax m coefficient
     */
    public int getThreshold(){
        return mThreshold;
    }

    /**
     * get the number of times a was executed in s
     * @param hs current state
     * @param a the action
     * @return n(s, a)
     */
    public int getStateActionCount(HashableState hs, Action a){
        int totalCount = 0;
        Map<HashableState, TransitionCountRewardTotalPair> resultingStateToCount = getResultingStateMap(hs, a);
        for(HashableState hsPrime : resultingStateToCount.keySet()) {
            TransitionCountRewardTotalPair pair = resultingStateToCount.get(hsPrime);
            int count = pair.transitionCount;
            totalCount += count;
        }
        return totalCount;
    }

    public Map<HashableState, TransitionCountRewardTotalPair> getResultingStateMap(HashableState hs, Action a) {
        String actionName = StringFormat.parameterizedActionName(a);
        Map<String, Map<HashableState, TransitionCountRewardTotalPair>> actionToResultingStateToCount = stateActionSPrimeCountRewardPairs.get(hs);
        if (actionToResultingStateToCount == null){
            actionToResultingStateToCount = new HashMap<String, Map<HashableState, TransitionCountRewardTotalPair>>();
            stateActionSPrimeCountRewardPairs.put(hs, actionToResultingStateToCount);
        }
        Map<HashableState, TransitionCountRewardTotalPair> resultingStateToCount = actionToResultingStateToCount.get(actionName);
        if (resultingStateToCount == null){
            resultingStateToCount = new HashMap<HashableState, TransitionCountRewardTotalPair>();
            actionToResultingStateToCount.put(actionName, resultingStateToCount);
        }
        return resultingStateToCount;
    }

    public TransitionCountRewardTotalPair getStateCountRewardTotal(HashableState hs, Action a, HashableState hsPrime) {
        Map<HashableState, TransitionCountRewardTotalPair> resultingStateToCount = getResultingStateMap(hs, a);
        TransitionCountRewardTotalPair pair = resultingStateToCount.get(hsPrime);
        if (pair == null) {
            int count = 0;
            pair = new TransitionCountRewardTotalPair(count, 0.0);
            resultingStateToCount.put(hsPrime, pair);
        }
        return pair;
    }

    /**
     * get the number of times sprime was reached after executing a in s
     * @param hs the start state
     * @param a the action
     * @param hsPrime the resulting state
     * @return n(s, a, s')
     */
    protected int getResultingStateCount(HashableState hs, Action a, HashableState hsPrime){
        TransitionCountRewardTotalPair pair = getStateCountRewardTotal(hs, a, hsPrime);
        return pair.transitionCount;
    }

    /**
     * gets the cumulative rewardTotal over all attempts of action a in state s
     * @param hs the current state
     * @param a the action
     * @param hsPrime the resulting state
     * @return r(s, a)
     */
    protected double getResultingRewardTotal(HashableState hs, Action a, HashableState hsPrime){
        TransitionCountRewardTotalPair pair = getStateCountRewardTotal(hs, a, hsPrime);
        return pair.rewardTotal;
    }

	/**
	 * updates the model counts, rewards and probabilities given the
	 * information in the outcome
	 * @param result the outcome of the latest action specific to the task rewards and abstractions
	 */
	public void updateModel(EnvironmentOutcome result){
		HashableState hs = this.hashingFactory.hashState(result.o);
		double reward = result.r;
		Action a = result.a;
		HashableState hsPrime = this.hashingFactory.hashState(result.op);
		
		//add to the transitionCount the information in the outcome and restore
		int n_sa = getStateActionCount(hs, a) + 1;
		double r_sasp = getResultingRewardTotal(hs, a, hsPrime) + reward;
		int n_sasp = getResultingStateCount(hs, a, hsPrime) + 1;

        Map<HashableState, TransitionCountRewardTotalPair> resultingStateMap = getResultingStateMap(hs, a);
		TransitionCountRewardTotalPair pair = resultingStateMap.get(hsPrime);
        pair.transitionCount = n_sasp;
		pair.rewardTotal = r_sasp;
        resultingStateMap.put(hsPrime, pair);

		if (n_sa >= mThreshold) {

		    // update THIS transition
            PossibleOutcome outcome = getPossibleOutcome(hs, a, hsPrime);
            double newR = (1.0 * r_sasp) / (1.0 * n_sasp); // .... was n_sa;
            double newP = (1.0 * n_sasp) / (1.0 * n_sa);
            setApproximateReward(hs, a, hsPrime, newR);
            setApproximateTransitionProbability(hs, a, hsPrime, newP);

            // update the other known possible transitions
            for (HashableState otherHsPrime : resultingStateMap.keySet()) {
                if (otherHsPrime.equals(hsPrime)) {
                    // this is the transition we just updated, skip
                    continue;
                }
                int otherTransitionCount = getResultingStateCount(hs, a, otherHsPrime);
                double remainingP = (1.0 * otherTransitionCount) / (1.0 * n_sa);
                setApproximateTransitionProbability(hs, a, otherHsPrime, remainingP);
            }

        }
	}

	protected PossibleOutcome getPossibleOutcome(HashableState hs, Action a, HashableState hsPrime) {
        Map<HashableState, PossibleOutcome> sPrimeToOutcomes = getSPrimeToOutcomes(hs, a);
        PossibleOutcome outcome = sPrimeToOutcomes.get(hsPrime);
        if (outcome == null) {
            double initialReward = 0.0;
            double initialProbability = 0.0;
            EnvironmentOutcome eo = new EnvironmentOutcome(hs.s(), a, hsPrime.s(), initialReward, terminal(hsPrime.s()));
            outcome = new PossibleOutcome(hashingFactory, eo, initialProbability);
            sPrimeToOutcomes.put(hsPrime, outcome);
        }
        return outcome;
    }

	protected void setApproximateReward(HashableState hs, Action a, HashableState hsPrime, double reward) {
        PossibleOutcome outcome = getPossibleOutcome(hs, a, hsPrime);
        outcome.setReward(reward);
    }

    protected void setApproximateTransitionProbability(HashableState hs, Action a, HashableState hsPrime, double probability) {
        PossibleOutcome outcome = getPossibleOutcome(hs, a, hsPrime);
        outcome.setTransitionProbability(probability);
    }

    protected Map<HashableState, PossibleOutcome> getSPrimeToOutcomes(HashableState hs, Action a) {
        String actionName = StringFormat.parameterizedActionName(a);
        HashableStateActionPair pair = new HashableStateActionPair(hs, actionName);
        HashMap<HashableState, PossibleOutcome> sPrimeToOutcomes = approximateTransitions.get(pair);
        if (sPrimeToOutcomes == null) {
            sPrimeToOutcomes = new HashMap<HashableState, PossibleOutcome>();
            approximateTransitions.put(pair, sPrimeToOutcomes);
        }
        return sPrimeToOutcomes;
    }

    // the transitions come from the recorded rewards and probabilities in the maps
    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        HashableState hs = this.hashingFactory.hashState(s);
        Map<HashableState, PossibleOutcome> sPrimeToOutcomes = getSPrimeToOutcomes(hs, a);
        Collection<PossibleOutcome> outcomes = sPrimeToOutcomes.values();
        List<TransitionProb> tps = new ArrayList<TransitionProb>();
        if (outcomes.size() < 1) {
            if (hImaginedState == null) {
                hImaginedState = createImaginedState(hs);
            }
            EnvironmentOutcome imaginedOutcome = new EnvironmentOutcome(s, a, hImaginedState.s(), rmax, true);
            TransitionProb imaginedTransition = new TransitionProb(1.0, imaginedOutcome);
            tps.add(imaginedTransition);
            return tps;
        }
        for (PossibleOutcome outcome : outcomes) {
            TransitionProb probability = outcome.transitionProbability;
            if (outcome.getOutcome() != probability.eo) {
                throw new RuntimeException("ERROR: the EnvironmentOutcome stored in the TransitionProb was not identical to the one in its own PossibleOutcome");
            }
            tps.add(probability);
        }
        return tps;
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

}
