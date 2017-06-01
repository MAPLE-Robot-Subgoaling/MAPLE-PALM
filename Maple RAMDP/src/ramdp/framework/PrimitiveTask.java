package ramdp.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;

public class PrimitiveTask extends Task{

	public PrimitiveTask(ActionType aType, OOSADomain abstractDomain) {
		super(null, aType, abstractDomain, new IdentityMap());
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}	
}
