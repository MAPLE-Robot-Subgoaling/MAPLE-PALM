//package rmaxq.agent;
//
//import burlap.behavior.singleagent.MDPSolverInterface;
//import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
//import burlap.behavior.valuefunction.QProvider;
//import burlap.behavior.valuefunction.QValue;
//import burlap.behavior.valuefunction.ValueFunction;
//import burlap.mdp.core.Domain;
//import burlap.mdp.core.action.Action;
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.core.state.State;
//import burlap.mdp.singleagent.SADomain;
//import burlap.mdp.singleagent.model.SampleModel;
//import burlap.statehashing.HashableState;
//import burlap.statehashing.HashableStateFactory;
//import hierarchy.framework.GroundedTask;
//import hierarchy.framework.StringFormat;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class QProviderRmaxQ implements QProvider, MDPSolverInterface{
//
//	private static final double INITIAL_Q_VALUE = 0.0;
//
//	private double qInit = INITIAL_Q_VALUE;
//
//	/**
//	 * a list of action values for each state
//	 */
//	private Map<HashableState, List<QValue>> stateToQValues;
//
//	/**
//	 * a provided state hashing factory
//	 */
//	private HashableStateFactory hashingFactory;
//
//	/**
//	 * the task
//	 */
//	private GroundedTask task;
//
//	/**
//	 * create a q provider for the task
//	 * @param hsf a state hashing factory
//	 * @param t the task
//	 */
//	public QProviderRmaxQ(HashableStateFactory hsf, GroundedTask t){
//		this.hashingFactory = hsf;
//		this.task = t;
//		this.stateToQValues = new HashMap<HashableState, List<QValue>>();
//	}
//
//	@Override
//	public List<QValue> qValues(State s) {
//		HashableState hs = hashingFactory.hashState(s);
//		if(!stateToQValues.containsKey(hs)){
//			List<QValue> qs = new ArrayList<QValue>();
//			List<GroundedTask> gts = task.getGroundedChildTasks(s);
//			if (gts.size() < 1) {
//				task.getGroundedChildTasks(s);
//				throw new RuntimeException("error/debug: no grounded tasks were found!");
//			}
//			for(GroundedTask a : gts){
//				qs.add(new QValue(s, a.getAction(), 0));
//			}
//			stateToQValues.put(hs, qs);
//		}
//		return stateToQValues.get(hs);
//	}
//
//	@Override
//	public double qValue(State s, Action a) {
//		HashableState hs = hashingFactory.hashState(s);
//		if(!stateToQValues.containsKey(hs)) {
//			stateToQValues.put(hs, new ArrayList<QValue>());
//		}
//		List<QValue> qValues = stateToQValues.get(hs);
//
//		String taskNameNew = StringFormat.parameterizedActionName(a);
//		for(QValue q : qValues){
//			String taskNameThis = StringFormat.parameterizedActionName(q.a);
//			if(taskNameNew.equals(taskNameThis)) {
//				return q.q;
//			}
//		}
//		return qInit;
//	}
//
//	//RmaxQ eqn 2
//	@Override
//	public double value(State s) {
//		if(!task.isPrimitive() && (task.isComplete(s) || task.isFailure(s))){
//			return task.getReward(s, task.getAction(), s);
//		}
//		return QProvider.Helper.maxQ(this, s);
//	}
//
//	/**
//	 * update Q(s, a) to the given value
//	 * @param s the state
//	 * @param a the action
//	 * @param val the new q value
//	 */
//	public void update(State s, Action a, double val){
//		List<QValue> qvalsins = stateToQValues.get(hashingFactory.hashState(s));
//		String taskNameNew = StringFormat.parameterizedActionName(a);
//		for(QValue q : qvalsins){
//			String taskNameThis = StringFormat.parameterizedActionName(q.a);
//			if(taskNameNew.equals(taskNameThis)) {
//				q.q = val;
//				return;
//			}
//		}
//		qvalsins.add(new QValue(s, a, qInit));
//	}
//
//
//
//	//the rest of this class is only needed because of mdpsolver but they are unused
//	@Override
//	public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void resetSolver() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void setDomain(SADomain domain) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void setModel(SampleModel model) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public SampleModel getModel() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public Domain getDomain() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void addActionType(ActionType a) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void setActionTypes(List<ActionType> actionTypes) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public List<ActionType> getActionTypes() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void setHashingFactory(HashableStateFactory hashingFactory) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public HashableStateFactory getHashingFactory() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public double getGamma() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void setGamma(double gamma) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void setDebugCode(int code) {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public int getDebugCode() {
//		throw new RuntimeException("not implemented");
//	}
//
//	@Override
//	public void toggleDebugPrinting(boolean toggle) {
//		throw new RuntimeException("not implemented");
//	}
//}
