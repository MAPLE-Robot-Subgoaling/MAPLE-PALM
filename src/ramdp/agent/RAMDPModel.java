package ramdp.agent;

import java.util.*;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOState;
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
	 * the rmax sample parameter
	 */
	private int mThreshold;
	
	/**
	 * the provided hashing factory
	 */
	private HashableStateFactory hashingFactory;

	/**
	 * reward function, S x ActionName
	 */
    private Map<HashableState, Map<String, Double>> rewards;

    /**
     * transition probabilities function, S x ActionName x S' -> [0, 1]
     */
    private Map<HashableState, Map<String, Map<HashableState, Double>>> transitionProbabilities;
	
	/**
	 * n(s, a) - count of executing a in s
	 */
	private Map<HashableState, Map<String, Integer>> stateActionCount;
	
	/**
	 * n(s, a, s') - count of executing a in s resulted in s'
	 */
	private Map<HashableState, Map<String, Map<HashableState, Integer>>> resultingStateCount;
	
	/**
	 * r(s, a) - total sum of reward received after taking a in s 
	 */
	private Map<HashableState, Map<String, Double>> totalReward;
	
	/**
	 * the grounded task that is being modeled
	 */
	private GroundedTask task;
	
	/**
	 * the max reward for the domain
	 */
	private double rmax;
	
	/**
	 * creates a rmax model
	 * @param task the grounded task to model
	 * @param threshold rmax sample threshold
	 * @param rmax max reward in domain
	 * @param hs provided hashing factory
	 */
	public RAMDPModel( GroundedTask task, int threshold, double rmax, HashableStateFactory hs) {
		this.hashingFactory = hs;
		this.mThreshold = threshold;
		this.rewards = new HashMap<HashableState, Map<String, Double>>();
		this.transitionProbabilities = new HashMap<HashableState, Map<String, Map<HashableState,Double>>>();
		this.stateActionCount = new HashMap<HashableState, Map<String,Integer>>();
		this.resultingStateCount = new HashMap<HashableState, Map<String, Map<HashableState, Integer>>>();
		this.totalReward = new HashMap<HashableState, Map<String,Double>>();
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

	// the transitions come from the recorded rewards and probabilities in the maps
	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		HashableState hs = this.hashingFactory.hashState(s);
		Map<HashableState, Double> resultingStates = getTransitionProbabilities(hs, a);
		List<TransitionProb> tps = new ArrayList<TransitionProb>();
		double reward = getReward(hs, a);
		
		for(HashableState hsPrime : resultingStates.keySet()){
			EnvironmentOutcome eo = new EnvironmentOutcome(s, a, hsPrime.s(),
					reward, terminal(hsPrime.s()));
			double p = resultingStates.get(hsPrime);
			tps.add( new TransitionProb(p, eo));
		}
		if (tps.size() < 1) {
			// System.err.println("no resulting states");
		}
		return tps;
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
		String actionName = StringFormat.parameterizedActionName(a);
		HashableState hsp = this.hashingFactory.hashState(result.op);
		
		//add to the count the information in the outcome and restore
		int n_sa = getStateActionCount(hs, a) + 1;
		double r_sa = getTotalReward(hs, a) + reward;
		int n_sasp = getResultingStateCount(hs, a, hsp) + 1;
		
		this.stateActionCount.get(hs).put(actionName, n_sa);
		this.totalReward.get(hs).put(actionName, r_sa);
		this.resultingStateCount.get(hs).get(actionName).put(hsp, n_sasp);

		if (n_sa >= mThreshold) {
            double newR = r_sa / n_sa;
		    setReward(hs, a, newR);
            double newP = (double) n_sasp / (1.0 * n_sa);
		    setTransitionProbability(hs, a, hsp, newP);
        }

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
	 * get a map of state that could result from taking action a in state s
	 * along with their probabilities
	 * @param hs the current hashed state
	 * @param a the action to take
	 * @return a map of hashed resulting states with their probability
	 */
	protected Map<HashableState, Double> getTransitionProbabilities(HashableState hs, Action a){
        String actionName = StringFormat.parameterizedActionName(a);
        int n_sa = getStateActionCount(hs, a);
        if (n_sa < mThreshold) {
            if (hImaginedState == null) {
                hImaginedState = createImaginedState(hs);
            }
            Map<HashableState, Double> tempTransition = new HashMap<HashableState, Double>();
            tempTransition.put(hImaginedState, 1.0);
            return tempTransition;
        }
	    return transitionProbabilities.get(hs).get(actionName);
	}

	protected double getReward(HashableState hs, Action a) {
        String actionName = StringFormat.parameterizedActionName(a);
        if (rewards.get(hs) == null || rewards.get(hs).get(actionName) == null) {
            return rmax;
        }
        return rewards.get(hs).get(actionName);
    }

	/**
	 * get the number of times a was executed in s
	 * @param hs current state
	 * @param a the action
	 * @return n(s, a)
	 */
	public int getStateActionCount(HashableState hs, Action a){
	    String actionName = StringFormat.parameterizedActionName(a);
        Map<String, Integer> actionToCount = stateActionCount.get(hs);
        if (actionToCount == null) {
            actionToCount = new HashMap<String,Integer>();
	        stateActionCount.put(hs, actionToCount);
        }
        Integer count = actionToCount.get(actionName);
        if (count == null) {
            count = 0;
	        actionToCount.put(actionName, count);
        }
		return count;
	}
	
	/**
	 * get the number of times sprime was reached after executing a in s
	 * @param hs the start state
	 * @param a the action
	 * @param hsp the resulting state
	 * @return n(s, a, s')
	 */
	protected int getResultingStateCount(HashableState hs, Action a, HashableState hsp){
	    String actionName = StringFormat.parameterizedActionName(a);
        Map<String, Map<HashableState, Integer>> actionToResultingStateToCount = resultingStateCount.get(hs);
        if (actionToResultingStateToCount == null){
            actionToResultingStateToCount = new HashMap<String, Map<HashableState, Integer>>();
			resultingStateCount.put(hs, actionToResultingStateToCount);
		}
        Map<HashableState, Integer> resultingStateToCount = actionToResultingStateToCount.get(actionName);
        if (resultingStateToCount == null){
            resultingStateToCount = new HashMap<HashableState, Integer>();
            actionToResultingStateToCount.put(actionName, resultingStateToCount);
		}
		Integer count = resultingStateToCount.get(hsp);
		if (count == null) {
		    count = 0;
		    resultingStateToCount.put(hsp, count);
        }
		return count;
	}
	
	/**
	 * gets the cumulative reward over all attempts of action a in state s
	 * @param hs the current state
	 * @param a the action
	 * @return r(s, a)
	 */
	protected double getTotalReward(HashableState hs, Action a){
	    String actionName = StringFormat.parameterizedActionName(a);
        Map<String, Double> actionToReward = totalReward.get(hs);
		if (actionToReward == null) {
		    actionToReward = new HashMap<String,Double>();
		    totalReward.put(hs, actionToReward);
        }
        Double reward = actionToReward.get(actionName);
        if (reward == null) {
            reward = 0.0;
            actionToReward.put(actionName, reward);
        }
        return reward;
	}
	
	/**
	 * get the number of action attempts needed for a transition to be known
	 * @return the rmax m coefficient
	 */
	public int getThreshold(){
		return mThreshold;
	}


    /**
     * sets R(s,a) to the given value
     * @param hs the current state
     * @param a the action to take
     * @param reward the new reward
     */
    protected void setReward(HashableState hs, Action a, double reward){
        String actionName = StringFormat.parameterizedActionName(a);
        Map<String, Double> actionToReward = rewards.get(hs);
        if (actionToReward == null) {
            actionToReward = new HashMap<String, Double>();
            rewards.put(hs, actionToReward);
        }
        actionToReward.put(actionName, reward);
    }

    /**
     * sets the probability of going to s' from s after executing a
     * @param hs the current state
     * @param a the action
     * @param hsp the resulting state
     * @param probability the new probability
     */
    protected void setTransitionProbability(HashableState hs, Action a, HashableState hsp, double probability){
        String actionName = StringFormat.parameterizedActionName(a);
        Map<String, Map<HashableState, Double>> actionToSPrimeToDouble = transitionProbabilities.get(hs);
        if (actionToSPrimeToDouble == null) {
            actionToSPrimeToDouble = new HashMap<String, Map<HashableState, Double>>();
            transitionProbabilities.put(hs, actionToSPrimeToDouble);
        }
        Map<HashableState, Double> sPrimeToDouble = actionToSPrimeToDouble.get(actionName);
        if (sPrimeToDouble == null) {
            sPrimeToDouble = new HashMap<HashableState, Double>();
            actionToSPrimeToDouble.put(actionName, sPrimeToDouble);
        }
        sPrimeToDouble.put(hsp, probability);
    }

}
