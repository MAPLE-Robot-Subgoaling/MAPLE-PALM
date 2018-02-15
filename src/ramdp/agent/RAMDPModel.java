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
	private int mThreshold;
	
	/**
	 * the provided hashing factory
	 */
	private HashableStateFactory hashingFactory;
	

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
        this.approximateTransitions = new HashMap<HashableStateActionPair, Map<HashableState, PossibleOutcome>>();
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

    public double getRmax() { return rmax; }

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
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
		
		//add to the transitionCount the information in the outcome and restore
		int n_sa = getStateActionCount(hs, a) + 1;
        PossibleOutcome outcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
		double r_sasp = outcome.getRewardTotal() + reward;
		int n_sasp = outcome.getTransitionCount() + 1;

        // update the totals for this transition
        outcome.setTransitionCount(n_sasp);
        outcome.setRewardTotal(r_sasp);

		if (n_sa >= mThreshold) {

		    // update the approximate model for THIS transition
            double newR = (1.0 * r_sasp) / (1.0 * n_sasp); // n_sa, n_sasp
            double newP = (1.0 * n_sasp) / (1.0 * n_sa);
            outcome.setReward(newR);
            outcome.setTransitionProbability(newP);

            // update the approximate model for the other known possible transitions
            for(HashableState otherHsPrime : hsPrimeToOutcomes.keySet()) {
                if (otherHsPrime.equals(hsPrime)) {
                    // this is the transition we just updated, so skip it
                    continue;
                }
                PossibleOutcome otherOutcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, otherHsPrime);
                double otherRewardTotal = otherOutcome.getRewardTotal();
                int otherTransitionCount = otherOutcome.getTransitionCount();
                double otherReward = (1.0 * otherRewardTotal) / (1.0 * otherTransitionCount);
                double otherP = (1.0 * otherTransitionCount) / (1.0 * n_sa);
                otherOutcome.setReward(otherReward);
                otherOutcome.setTransitionProbability(otherP);
            }

        } else {
            hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
		    double imaginedR = 0.0;
		    double equalP = 1.0 / hsPrimeToOutcomes.size();
		    outcome.setReward(imaginedR);
		    outcome.setTransitionProbability(equalP);
            for(HashableState otherHsPrime : hsPrimeToOutcomes.keySet()) {
                if (otherHsPrime.equals(hsPrime)) {
                    // this is the transition we just updated, so skip it
                    continue;
                }
                PossibleOutcome otherOutcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, otherHsPrime);
                otherOutcome.setReward(imaginedR);
                otherOutcome.setTransitionProbability(equalP);
            }
        }
	}


    /**
     * get the number of times a was executed in s
     * @param hs current state
     * @param a the action
     * @return n(s, a)
     */
    public int getStateActionCount(HashableState hs, Action a){
        int totalCount = 0;
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
        if (hsPrimeToOutcomes == null) { return totalCount; }
        for(HashableState hsPrime : hsPrimeToOutcomes.keySet()) {
            PossibleOutcome outcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
            int transitionCount = outcome.getTransitionCount();
            totalCount += transitionCount;
        }
        return totalCount;
    }

//	protected PossibleOutcome getPossibleOutcome(HashableState hs, Action a, HashableState hsPrime) {
//        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
//        return getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
//    }

    protected PossibleOutcome getPossibleOutcome(Map<HashableState, PossibleOutcome> hsPrimeToOutcomes, HashableState hs, Action a, HashableState hsPrime) {
        if (hsPrimeToOutcomes == null) {
            String actionName = StringFormat.parameterizedActionName(a);
            HashableStateActionPair pair = new HashableStateActionPair(hs, actionName);
            hsPrimeToOutcomes = new HashMap<HashableState, PossibleOutcome>();
            approximateTransitions.put(pair, hsPrimeToOutcomes);
        } else {
            if (hsPrimeToOutcomes.size() < 1) {
                System.err.println("empty");
            }
        }
        PossibleOutcome outcome = hsPrimeToOutcomes.get(hsPrime);
        if (outcome == null) {
            double initialReward = 0.0;
            double initialProbability = 0.0;
            int initalCount = 0;
            double initalRewardTotal = 0.0;
            EnvironmentOutcome eo = new EnvironmentOutcome(hs.s(), a, hsPrime.s(), initialReward, terminal(hsPrime.s()));
            outcome = new PossibleOutcome(hashingFactory, eo, initialProbability, initalCount, initalRewardTotal);
            hsPrimeToOutcomes.put(hsPrime, outcome);
        }
        return outcome;
    }

    protected Map<HashableState, PossibleOutcome> getHsPrimeToOutcomes(HashableState hs, Action a) {
        String actionName = StringFormat.parameterizedActionName(a);
        HashableStateActionPair pair = new HashableStateActionPair(hs, actionName);
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = approximateTransitions.get(pair);

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

    // the transitions come from the recorded rewards and probabilities in the maps
    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        HashableState hs = this.hashingFactory.hashState(s);
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, a);
        List<TransitionProb> tps = new ArrayList<TransitionProb>();
        if (getStateActionCount(hs, a) < mThreshold || hsPrimeToOutcomes.size() < 1) {
            TransitionProb imaginedTransition = makeImaginedTransition(hs, a);
            tps.add(imaginedTransition);
            return tps;
        }
        double totalProbability = 0.0;
        for (HashableState hsPrime : hsPrimeToOutcomes.keySet()) {
            PossibleOutcome outcome = getPossibleOutcome(hsPrimeToOutcomes, hs, a, hsPrime);
            TransitionProb probability = outcome.transitionProb;
            if (outcome.getOutcome() != probability.eo) {
                throw new RuntimeException("ERROR: the EnvironmentOutcome stored in the TransitionProb was not identical to the one in its own PossibleOutcome");
            }
            tps.add(probability);
            totalProbability += probability.p;
        }
        if (totalProbability < 0.99999999999 || totalProbability > 1.00000000001) {
            System.err.println("total probability does not sum to 1.0");
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

    public void printDebugInfo() {
        System.out.println("\n\n\n******************************************************************************************************\n\n\n");
        for(HashableStateActionPair pair : approximateTransitions.keySet()) {
            System.out.println(pair.actionName);
            System.out.println(pair.hs.s().toString());
            Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = approximateTransitions.get(pair);
            for (HashableState hsPrime : hsPrimeToOutcomes.keySet()) {
                PossibleOutcome outcome = hsPrimeToOutcomes.get(hsPrime);
                System.out.println(outcome.toString());
            }
            System.out.println("\n*****************\n");
        }
    }
}
