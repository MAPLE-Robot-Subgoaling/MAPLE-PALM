package ramdp.agent;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;

public class RAMDPModel implements FullModel{

	private int mThreshold;
	
	private HashableStateFactory hashingFactory;
	
	/**
	 * reward function, S x ActionName
	 */
	private Map<HashableState, Map<String, Double>> rewards;
	
	/**
	 * transition function, S x Action name x S
	 */
	private Map<HashableState, Map<String, Map<HashableState, Double>>> transitions;
	
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
	
	private GroundedTask node;
	
	private double rmax;
	
	/**
	 * creats a RAMDP model 
	 * @param tasks
	 * @param threshold
	 * @param hs
	 */
	public RAMDPModel( GroundedTask node, int threshold, double rmax, HashableStateFactory hs) {
		this.hashingFactory = hs;
		this.mThreshold = threshold;
		this.rewards = new HashMap<HashableState, Map<String, Double>>();
		this.transitions = new HashMap<HashableState, Map<String, Map<HashableState,Double>>>();
		this.stateActionCount = new HashMap<HashableState, Map<String,Integer>>();
		this.resultingStateCount = new HashMap<HashableState, Map<String, Map<HashableState, Integer>>>();
		this.totalReward = new HashMap<HashableState, Map<String,Double>>();
		this.node = node;
		this.rmax = rmax;
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		List<TransitionProb> tps = transitions(s, a);
		double sample = Math.random();
		double sum = 0;
		for(TransitionProb tp : tps){
			sum += tp.p;
			if(sample < sum){
				return tp.eo;
			}
		}
		
		throw new RuntimeException("Probabilities don't sum to 1.0: " + sum);
	}

	@Override
	public boolean terminal(State s) {
		return node.isTerminal(s);
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		HashableState hs = this.hashingFactory.hashState(s);
		Map<HashableState, Double> resultingStates = getResultingStates(hs, a);
		List<TransitionProb> tps = new ArrayList<TransitionProb>();
		double reward = getReward(hs, a);
		
		for(HashableState hsprime : resultingStates.keySet()){
			EnvironmentOutcome eo = new EnvironmentOutcome(s, a, hsprime.s(),
					reward, node.isTerminal(hsprime.s()));
			double p = resultingStates.get(hsprime);
			tps.add( new TransitionProb(p, eo));
		}
		return tps; 
	}
	
	public void updateModel(EnvironmentOutcome result){
		HashableState hs = this.hashingFactory.hashState(result.o);
		double reward = result.r;
		Action a = result.a;
		HashableState hsp = this.hashingFactory.hashState(result.op);
		
		int n_sa = getStateActionCount(hs, a) + 1;
		double r_sa = getTotalReward(hs, a) + reward;
		int n_sasp = getResultingStateCount(hs, a, hsp) + 1;
		
		this.stateActionCount.get(hs).put(a.actionName(), n_sa);
		this.totalReward.get(hs).put(a.actionName(), r_sa);
		this.resultingStateCount.get(hs).get(a.actionName()).put(hsp, n_sasp);
		
		if(n_sa >= mThreshold){
			double newR = r_sa / n_sa;
			setReward(hs, a, newR);
			
			Map<HashableState, Integer> resultStates = this.resultingStateCount.get(hs).get(a.actionName());
			for(HashableState hsprime : resultStates.keySet()){
				n_sasp = resultStates.get(hsprime);
				double newP = (double) n_sasp / n_sa;
				setTransition(hs, a, hsprime, newP);
			}
		}
	}
	
	protected Map<HashableState, Double> getResultingStates(HashableState hs, Action a){
		Map<String, Map<HashableState, Double>> SResults = this.transitions.get(hs);
		if(SResults == null){                                         
			SResults = new HashMap<String, Map<HashableState,Double>>();
			this.transitions.put(hs, SResults);
		}
		
		Map<HashableState, Double> SAResults = SResults.get(a.actionName());
		if(SAResults == null){
			SAResults = new HashMap<HashableState, Double>();
			SResults.put(a.actionName(), SAResults);
		}
		return SAResults;
	}
	
	protected double getReward(HashableState hs, Action a){
		Map<String, Double> rewards = this.rewards.get(hs);
		if(rewards == null){
			rewards = new HashMap<String, Double>();
			this.rewards.put(hs, rewards);
		}
		
		Double reward = rewards.get(a.actionName());
		if(reward == null){
			reward = this.rmax;
			rewards.put(a.actionName(), reward);
		}
		return reward;
	}
	
	protected int getStateActionCount(HashableState hs, Action a){
		Map<String, Integer> stateCount = this.stateActionCount.get(hs);
		if(stateCount == null){
			stateCount = new HashMap<String, Integer>();
			this.stateActionCount.put(hs, stateCount);
		}
		
		Integer SAcount = stateCount.get(a.actionName());
		if(SAcount == null){
			SAcount = 0;
			stateCount.put(a.actionName(), SAcount);
		}
		return SAcount;
	}
	
	protected int getResultingStateCount(HashableState hs, Action a, HashableState hsp){
		Map<String, Map<HashableState, Integer>> stateCount = this.resultingStateCount.get(hs);
		if(stateCount == null){
			stateCount = new HashMap<String, Map<HashableState, Integer>>();
			this.resultingStateCount.put(hs, stateCount);
		}
		
		Map<HashableState, Integer> SACount = stateCount.get(a.actionName());
		if(SACount == null){
			SACount = new HashMap<HashableState, Integer>();
			stateCount.put(a.actionName(), SACount);
		}
		
		Integer SASPCount = SACount.get(hsp);
		if(SASPCount == null){
			SASPCount = 0;
			SACount.put(hsp, SASPCount);
		}
		
		return SASPCount;
	}
	
	protected double getTotalReward(HashableState hs, Action a){
		Map<String, Double> Sreward = this.totalReward.get(hs);
		if(Sreward == null){
			Sreward = new HashMap<String, Double>();
			this.totalReward.put(hs, Sreward);
		}
		
		Double rewards = Sreward.get(a.actionName());
		if(rewards == null){
			rewards = 0.;
			Sreward.put(a.actionName(), rewards);
		}
		
		return rewards;
	}
	
	protected void setReward(HashableState hs, Action a, double reward){
		getReward(hs, a);
		this.rewards.get(hs).put(a.actionName(), reward);
	}
	
	protected void setTransition(HashableState hs, Action a, HashableState hsp, double probability){
		getResultingStates(hs, a);
		this.transitions.get(hs).get(a.actionName()).put(hsp, probability);
	}
}
