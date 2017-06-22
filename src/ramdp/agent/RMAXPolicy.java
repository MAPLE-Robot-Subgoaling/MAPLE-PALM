package ramdp.agent;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

public class RMAXPolicy implements Policy {

	
	private RAMDPModel model;
	
	private HashableStateFactory hashingFactory;
	
	private Policy basePolicy;
	
	private List<ActionType> actionTypes;
	
	public RMAXPolicy(RAMDPModel mod, Policy base, List<ActionType> actions, HashableStateFactory hs) {
		model = mod;
		basePolicy = base;
		actionTypes = actions;
		hashingFactory = hs;
	}
	
	@Override
	public Action action(State s) {
		List<Action> unmodeled = unmodeledActions(s);
		
		if(unmodeled.size() > 0)
			return unmodeled.get((int) (Math.random() * unmodeled.size()));
		
		return basePolicy.action(s);
	}

	@Override
	public double actionProb(State s, Action a) {
		List<Action> unmodeled = unmodeledActions(s);
		
		if(unmodeled.size() > 0)
			return 1 / (double) unmodeled.size();
		return basePolicy.actionProb(s, a);
	}

	@Override
	public boolean definedFor(State s) {
		return true;
	}

	protected List<Action> unmodeledActions(State s){
		HashableState hs = hashingFactory.hashState(s);
		List<Action> unmodeled = new ArrayList<Action>();
		
		for(ActionType type : actionTypes){
			List<Action> possible = type.allApplicableActions(s);
			for(Action a : possible){
				int n_sa = model.getStateActionCount(hs, a);
				if(n_sa < model.getThreshold()){
					unmodeled.add(a);
				}
			}
		}
		return unmodeled;
	}
}
