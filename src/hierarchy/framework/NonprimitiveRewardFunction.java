package hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class NonprimitiveRewardFunction implements RewardFunction{
	//the default reward function for non primitive tasks
	
	private Task t;
	
	public NonprimitiveRewardFunction(Task t) {
		this.t = t;
	}
	
	/**
	 * the action is associated with the grounded task 
	 * the sprime is the state to assign award to
	 * this returns +1 for completion
	 * -1 for failure 
	 * and 0 otherwise
	 */
	@Override
	public double reward(State s, Action a, State sPrime) {
		if(t.isComplete(sPrime, a))
			return 1;
		else if (t.isFailure(sPrime, a))
			return -1;
		return 0;
	}

}
