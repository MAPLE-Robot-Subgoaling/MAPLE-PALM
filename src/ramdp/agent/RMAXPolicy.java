//package ramdp.agent;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import burlap.behavior.policy.Policy;
//import burlap.debugtools.RandomFactory;
//import burlap.mdp.core.action.Action;
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.core.state.State;
//import burlap.statehashing.HashableState;
//import burlap.statehashing.HashableStateFactory;
//
//public class RMAXPolicy implements Policy {
//
//	/**
//	 * the model this policy is planning for
//	 */
//	private RAMDPModel model;
//
//	/**
//	 * the provided state hashing factory
//	 */
//	private HashableStateFactory hashingFactory;
//
//	/**
//	 * a policy obtain from pure planning on the domain
//	 */
//	private Policy basePolicy;
//
//	/**
//	 * all possible action in the domain
//	 */
//	private List<ActionType> actionTypes;
//
//	/**
//	 * create a rmax policy
//	 * @param mod the model that is being planned for
//	 * @param base the policy from pure planning
//	 * @param actions the actions of the domain
//	 * @param hs provided state hashing factory
//	 */
//	public RMAXPolicy(RAMDPModel mod, Policy base, List<ActionType> actions, HashableStateFactory hs) {
//		model = mod;
//		basePolicy = base;
//		actionTypes = actions;
//		hashingFactory = hs;
//	}
//
//	@Override
//	public Action action(State s) {
//		//if there are action that have not reached the sample threshold, they get priority
//		List<Action> unmodeled = unmodeledActions(s);
//
//		if(unmodeled.size() > 0) {
////			System.out.print("*");
//			return unmodeled.get(RandomFactory.getMapped(0).nextInt(unmodeled.size()));
//		}
//
//		//if not, default to original policy
//		return basePolicy.action(s);
//	}
//
//	@Override
//	public double actionProb(State s, Action a) {
//		List<Action> unmodeled = unmodeledActions(s);
//
//		if(unmodeled.size() > 0)
//			return 1.0 / (double) unmodeled.size();
//		return basePolicy.actionProb(s, a);
//	}
//
//	@Override
//	public boolean definedFor(State s) {
//		return true;
//	}
//
//	/**
//	 * create a list of actions which do not have enough samples from s to have converged
//	 * @param s the current state
//	 * @return a list of the actions which are under sampled out of s
//	 */
//	protected List<Action> unmodeledActions(State s){
//		HashableState hs = hashingFactory.hashState(s);
//		List<Action> unmodeled = new ArrayList<Action>();
//
//		for(ActionType type : actionTypes){
//			List<Action> possible = type.allApplicableActions(s);
//			for(Action a : possible){
//				int n_sa = model.getStateActionCount(hs, a);
//				if(n_sa < model.getThreshold()){
//					unmodeled.add(a);
//				}
//			}
//		}
//		return unmodeled;
//	}
//}
