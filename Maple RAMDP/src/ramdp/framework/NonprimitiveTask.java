package ramdp.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.oo.OOSADomain;

public abstract class NonprimitiveTask extends Task{

	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map) {
		super(children, aType, abstractDomain, map);
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

}
