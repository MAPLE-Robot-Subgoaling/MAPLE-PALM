package rmaxq.agent;

import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QProviderRmaxQ implements QProvider, MDPSolverInterface{

	/**
	 * a list of action values for each state
	 */
	private Map<HashableState, List<QValue>> qvals;
	
	/**
	 * a provided state hashing factory
	 */
	private HashableStateFactory hashingFactory;
	
	/**
	 * the task
	 */
	private GroundedTask task;
	
	/**
	 * create a q provider for the task
	 * @param hsf a state hashing factory
	 * @param t the task
	 */
	public QProviderRmaxQ(HashableStateFactory hsf, GroundedTask t){
		this.hashingFactory = hsf;
		this.task = t;
		this.qvals = new HashMap<HashableState, List<QValue>>();
	}
	
	@Override
	public double qValue(State s, Action a) {
		HashableState hs = hashingFactory.hashState(s);
		if(!qvals.containsKey(hs))
			qvals.put(hs, new ArrayList<QValue>());
		List<QValue> qval = qvals.get(hs);
		
		for(QValue q : qval){
			if(q.a.equals(a))
				return q.q;
		}
		return 0;
	}

	//RmaxQ eqn 2
	@Override
	public double value(State s) {
		if(!task.isPrimitive() && (task.isComplete(s) || task.isFailure(s))){
			throw new RuntimeException("not implemented");
//			return task.getReward(s);
		}
				
		return QProvider.Helper.maxQ(this, s);
	}

	/**
	 * update Q(s, a) to the given value
	 * @param s the state
	 * @param a the action
	 * @param val the new q value
	 */
	public void update(State s, Action a, double val){
		List<QValue> qvalsins = qvals.get(hashingFactory.hashState(s));
		for(QValue q : qvalsins){
			if(q.a.equals(a)){
				q.q = val;
				return;
			}
		}
		qvalsins.add(new QValue(s, a, 0));
	}
	
	@Override
	public List<QValue> qValues(State s) {
		HashableState hs = hashingFactory.hashState(s);
		if(!qvals.containsKey(hs)){
			List<QValue> qs = new ArrayList<QValue>();
			List<GroundedTask> gts = task.getGroundedChildTasks(s);
			for(GroundedTask a : gts){
				qs.add(new QValue(s, a.getAction(), 0));
			}
			qvals.put(hs, qs);
		}
		return qvals.get(hs);
	}
	
	//the rest of this class is only needed because of mdpsolver but they are unused
	@Override
	public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {
		
	}

	@Override
	public void resetSolver() {
		
	}

	@Override
	public void setDomain(SADomain domain) {
		
	}

	@Override
	public void setModel(SampleModel model) {
		
	}

	@Override
	public SampleModel getModel() {
		return null;
	}

	@Override
	public Domain getDomain() {
		return null;
	}

	@Override
	public void addActionType(ActionType a) {
	
	}

	@Override
	public void setActionTypes(List<ActionType> actionTypes) {
		
	}

	@Override
	public List<ActionType> getActionTypes() {
		return null;
	}

	@Override
	public void setHashingFactory(HashableStateFactory hashingFactory) {
		
	}

	@Override
	public HashableStateFactory getHashingFactory() {
		return null;
	}

	@Override
	public double getGamma() {
		return 0;
	}

	@Override
	public void setGamma(double gamma) {
		
	}

	@Override
	public void setDebugCode(int code) {
		
	}

	@Override
	public int getDebugCode() {
		return 0;
	}

	@Override
	public void toggleDebugPrinting(boolean toggle) {
		
	}
}
