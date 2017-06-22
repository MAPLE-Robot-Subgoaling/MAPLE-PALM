package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;

public class RootTask extends NonprimitiveTask {

	public RootTask(Task[] children, OOSADomain abstractDomain, StateMapping map) {
		super(children, new SolveActionType(), abstractDomain, map, null, null);
	}

	@Override
	public boolean isFailure(State s, Action a) {
		return this.domain.getModel().terminal(s);
	}
	
	@Override
	public boolean isComplete(State s, Action a){
		return isFailure(s, a);
	}
}
