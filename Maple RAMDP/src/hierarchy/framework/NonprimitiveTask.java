package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

public class NonprimitiveTask extends Task{

	private RewardFunction rf;
	private PropositionalFunction terminal, complted;
	
	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map,
			PropositionalFunction term, PropositionalFunction compl) {
		super(children, aType, abstractDomain, map);
		this.rf = new NonprimitiveRewardFunction(this);
		this.terminal = term;
		this.complted = compl; 
	}

	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map,
			 RewardFunction taskrf, PropositionalFunction term, PropositionalFunction compl) {
		super(children, aType, abstractDomain, map);
		this.rf = taskrf;
		this.terminal = term;
		this.complted = compl;
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

	@Override
	public boolean isTerminal(State s, Action a) {
		return terminal.isTrue((OOState) s, a.actionName());
	}
	
	@Override
	public boolean isComplete(State s, Action a){
		return complted.isTrue((OOState) s, a.actionName()); 
	}
}
