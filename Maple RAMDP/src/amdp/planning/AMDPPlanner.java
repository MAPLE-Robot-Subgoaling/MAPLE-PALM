package amdp.planning;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;

public class AMDPPlanner {
	
	private Task root;
	
	private Map<String, Policy> taskPolicies;
	
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
		this.taskPolicies = new HashMap<String, Policy>();
	}
	
	
	public Episode planFromState(State s){
		GroundedTask solve = root.getAllGroundedTasks(s).get(0);
		Episode e = new Episode(s);
		return solveTask(solve, e );
	}

	public Episode solveTask(GroundedTask task, Episode e, Environment env){
		OOSADomain domain;
		if(task.isPrimitive()){
			Action a = task.getAction();
			EnvironmentOutcome result = env.executeAction(a);
			e.transition(result);
		}else{
			State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
			State currentState = task.mapState(baseState);
			
			Policy taskPolicy = getPolicy(task, currentState);
			while(!(task.isFailure(currentState) || task.isComplete(currentState))){
				Action a = taskPolicy.action(currentState);
				GroundedTask child =
			}
		}	
	}
	
	private Policy getPolicy(GroundedTask t, State s){
		Policy p = taskPolicies.get(t.toString());
		if(p == null){
			BoundedRTDP brtdp = new BoundedRTDP(t.getDomain(), gamma, hs, new ConstantValueFunction(0), new ConstantValueFunction(1),
					 maxDelta, maxRollouts);
			p = brtdp.planFromState(s);
			this.taskPolicies.put(t.toString(), p);
		}
		
		return p;
	}
	
	private Environment getBasenvirnment(Task t){
		if(t.isPrimitive()){
			return new SimulatedEnvironment(t.getDomain());
		}else{
			for(Task child : t.getChildren()){
				return getBasenvirnment(child);
			}
		}
		return null;
	}
	
	private GroundedTask getTask(Action a){
		String aMame = a.actionName();
		GroundedTask gt = this.actionMap.get(aMame);
		
	}
}
