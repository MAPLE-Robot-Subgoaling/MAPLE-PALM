//package edu.umbc.cs.maple.hierarchy.framework;
//
//import burlap.mdp.core.action.Action;
//import burlap.mdp.core.state.State;
//import burlap.mdp.singleagent.model.RewardFunction;
//
//public class NonprimitiveRewardFunction implements RewardFunction{
//	//the default rewardTotal function for non primitive tasks
//
//	private Task t;
//
//	public NonprimitiveRewardFunction(Task t) {
//		this.t = t;
//	}
//
//	/**
//	 * return a rewardTotal assigned to the resulting s prime state
//	 * @param s the pprevious state
//	 * @param a the grounded action of the task currently being solved
//	 * @param sPrime the resulting state whick is associated with the rewardTotal
//	 * @return 1 if s prime completes the task, -1 if the task fails, 0 otherwise
//	 */
//	@Override
//	public double rewardTotal(State s, Action a, State sPrime) {
//		if(t.isComplete(sPrime, a))
//			return 1;
////		else if (t.isFailure(sPrime, a))
////			return -1;
//		return 0;
//	}
//
//}
