package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;

public class RootTask extends NonprimitiveTask {
	//the default top of the hierarchy
	
	/**
	 * create a root of a task hierarchy
	 * @param children the subtasks
	 * @param abstractDomain the domain which the actions are executed in
	 * @param map the state abstraction function into the given domain
	 */
	public RootTask(Task[] children, OOSADomain abstractDomain, StateMapping map) {
		super(children, new SolveActionType(), abstractDomain, map, null, null);
	}

	//a hierarchy is assumed to complete the the terminal goal of the provided abstract domain
	//failure is undefined - the hierarchy should be able to complete the goal
	@Override
	public boolean isFailure(State s, Action a) {
		return false;
	}
	
	@Override
	public boolean isComplete(State s, Action a){
		return this.domain.getModel().terminal(s);
	}
}
