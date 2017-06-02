package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

public class RootTask extends NonprimitiveTask {

	public RootTask(Task[] children, OOSADomain abstractDomain, StateMapping map, RewardFunction abstractRf) {
		super(children, new SolveActionType(), abstractDomain, map, abstractRf);
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		return this.domain.getModel().terminal(s);
	}
}
