package rmaxq.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class QProviderRmaxQ implements QProvider, MDPSolverInterface{

	private Map<HashableState, List<QValue>> qvals;
	private HashableStateFactory hashingFactory;
	private GroundedTask task;
	
	public QProviderRmaxQ(HashableStateFactory hsf, GroundedTask t){
		this.hashingFactory = hsf;
		this.task = t;
		this.qvals = new HashMap<HashableState, List<QValue>>();
	}
	
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

	public double value(State s) {
		if(!task.t.isTaskPrimitive() && task.t.terminal(s, task.getAction())){
			NonPrimitiveTaskNode npt = (NonPrimitiveTaskNode)task.t;
			return npt.pseudoRewardFunction(s, task.getAction());
		}
				
		return QProvider.Helper.maxQ(this, s);
	}

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
	
	public List<QValue> qValues(State s) {
		HashableState hs = hashingFactory.hashState(s);
		if(!qvals.containsKey(hs)){
			List<QValue> qs = new ArrayList<QValue>();
			if(task.t.isTaskPrimitive()){
				qs.add(new QValue(s, task.action, 0));
			}else{
			
				TaskNode[] children = ((NonPrimitiveTaskNode)task.t).getChildren();
				for(TaskNode child: children){
					List<GroundedTask> tasks = child.getApplicableGroundedTasks(s);
					for(GroundedTask a : tasks){
						qs.add(new QValue(s, a.action, 0));
					}
				}
			}
			qvals.put(hs, qs);
		}
		return qvals.get(hs);
	}
	
	public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {
		
	}

	public void resetSolver() {
		
	}

	public void setDomain(SADomain domain) {
		
	}

	public void setModel(SampleModel model) {
		
	}

	public SampleModel getModel() {
		return null;
	}

	public Domain getDomain() {
		return null;
	}

	public void addActionType(ActionType a) {
	
	}

	public void setActionTypes(List<ActionType> actionTypes) {
		
	}

	public List<ActionType> getActionTypes() {
		return null;
	}

	public void setHashingFactory(HashableStateFactory hashingFactory) {
		
	}

	public HashableStateFactory getHashingFactory() {
		return null;
	}

	public double getGamma() {
		return 0;
	}

	public void setGamma(double gamma) {
		
	}

	public void setDebugCode(int code) {
		
	}

	public int getDebugCode() {
		return 0;
	}

	public void toggleDebugPrinting(boolean toggle) {
		
	}
}
