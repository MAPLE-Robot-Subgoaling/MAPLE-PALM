package hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class NonprimitiveRewardFunction implements RewardFunction{

	private Task t;
	
	public NonprimitiveRewardFunction(Task t) {
		this.t = t;
	}
	@Override
	public double reward(State s, Action a, State sprime) {
		if(t.isComplete(s, a))
			return 1;
		else if (t.isTerminal(s, a))
			return -1;
		return 0;
	}

}
