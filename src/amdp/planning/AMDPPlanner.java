package amdp.planning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import utilities.BoundedRTDP;

public class AMDPPlanner {
	
	private Task root;
	
	private Map<String, Map<HashableState, Policy>> taskPolicies;
	
	private double gamma;
	
	private HashableStateFactory hs;
	
	private double maxDelta;
	
	private int maxRollouts;
	
	private Map<String, GroundedTask> actionMap;
	
	public AMDPPlanner(Task root, double gamma, HashableStateFactory hs, double maxDelta, int maxRollouts) {
		this.root = root;
		this.gamma = gamma;
		this.hs = hs;
		this.maxDelta = maxDelta;
		this.maxRollouts = maxRollouts; 
		this.actionMap = new HashMap<String, GroundedTask>();
		this.taskPolicies = new HashMap<String, Map<HashableState,Policy>>();
	}
	
	
	public Episode planFromState(State baseState){
		State rootState = root.mapState(baseState);
		GroundedTask solve = root.getAllGroundedTasks(rootState).get(0);
		Episode e = new Episode(baseState);
		SimulatedEnvironment env = getBaseEnvirnment(root, baseState);
		return solveTask(solve, e, env);
	}

	public Episode solveTask(GroundedTask task, Episode e, Environment env){
		if(task.isPrimitive()){
			Action a = task.getAction();
			EnvironmentOutcome result = env.executeAction(a);
			e.transition(result);
		}else{
			State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
			State currentState = task.mapState(baseState);

			int actCout = 0;
			Policy taskPolicy = getPolicy(task, currentState);
			while(!(task.isFailure(currentState) || task.isComplete(currentState))){
				actCout++;
				Action a = taskPolicy.action(currentState);
				GroundedTask child = getChildGT(task, a, currentState);
				solveTask(child, e, env);
				baseState = e.stateSequence.get(e.stateSequence.size() - 1);
				currentState = task.mapState(baseState);
			}
			System.out.println(task + " " + actCout);
		}	
		return e;
	}
	
	private Policy getPolicy(GroundedTask t, State s){
		HashableState hscurrwnt = hs.hashState(s);
		
		Map<HashableState, Policy> taskPolicies = this.taskPolicies.get(t.toString());
		if(taskPolicies == null){
			taskPolicies = new HashMap<HashableState, Policy>();
			this.taskPolicies.put(t.toString(), taskPolicies);
		}
		
		Policy p = taskPolicies.get(hscurrwnt);
		if(p == null){
			OOSADomain domain = t.getDomain();
			OOSADomain copy = new OOSADomain();
			List<ActionType> acts = domain.getActionTypes();
			for(ActionType a : acts){
				copy.addActionType(a);
			}
			
			FullModel generalModel = (FullModel) domain.getModel();
			FullModel newModel = new AMDPModel(t, generalModel);
			copy.setModel(newModel);
			BoundedRTDP brtdp = new BoundedRTDP(copy, gamma, hs, new ConstantValueFunction(0), new ConstantValueFunction(1),
					 maxDelta, maxRollouts);
			p = brtdp.planFromState(s);
			taskPolicies.put(hscurrwnt, p);
		}
		return p;
	}
	
	private SimulatedEnvironment getBaseEnvirnment(Task t, State s){
		if(t.isPrimitive()){
			return new SimulatedEnvironment(t.getDomain(), s);
		}else{
			for(Task child : t.getChildren()){
				return getBaseEnvirnment(child, s);
			}
		}
		return null;
	}
	
	private GroundedTask getChildGT(GroundedTask t, Action a, State s){
		String aMame = a.actionName();
		GroundedTask gt = this.actionMap.get(aMame);
		if(gt == null){
			List<GroundedTask> children = t.getGroundedChildTasks(s);
			for(GroundedTask child : children){
				this.actionMap.put(child.toString(), child);
			}
			gt = this.actionMap.get(a.actionName());
		}
		return gt;
	}
}
