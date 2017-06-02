package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

public abstract class NonprimitiveTask extends Task{

	private RewardFunction rf;
	
	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
		this.rf = new NonprimitiveRewardFunction(this);
	}

	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map
			, RewardFunction taskrf) {
		super(children, aType, abstractDomain, map);
		this.rf = taskrf;
	}
	@Override
	public boolean isPrimitive() {
		return false;
	}
	
	public double reward(State s, Action a){
		return rf.reward(s, a, s);
	}
	
	public void setRF(RewardFunction rf){
		this.rf = rf;
	}
}
