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

	/**
	 * the root task of the hierarchy this agent plans on
	 */
	private Task root;
	
	/**
	 * The cashed policies which tell the agent what to do
	 * they is one for each task and start state which the agent encounters
	 */
	private Map<String, Map<HashableState, Policy>> taskPolicies;
	
	/**
	 * the discount factor for the domains
	 */
	private double gamma;
	
	/**
	 * the hashing factory
	 */
	private HashableStateFactory hs;
	
	/**
	 * the maximum error allowed in the planning
	 */
	private double maxDelta;
	
	/**
	 * the maximum number of rollouts allowed 
	 */
	private int maxRollouts;
	
	/**
	 * a lookup table from action name to the grounded task that matches
	 */
	private Map<String, GroundedTask> actionMap;
	
	/**
	 * setup the planner with a specific hierarchy
	 * @param root the root of the AMDP hierarchy to plan over
	 * @param gamma discouny
	 * @param hs hashing factory
	 * @param maxDelta max error for the planner
	 * @param maxRollouts max number of rollouts for BRTDP
	 */
	public AMDPPlanner(Task root, double gamma, HashableStateFactory hs, double maxDelta, int maxRollouts) {
		this.root = root;
		this.gamma = gamma;
		this.hs = hs;
		this.maxDelta = maxDelta;
		this.maxRollouts = maxRollouts; 
		this.actionMap = new HashMap<String, GroundedTask>();
		this.taskPolicies = new HashMap<String, Map<HashableState,Policy>>();
	}
	
	/**
	 * generate a sequence of actions starting at the given state and 
	 * continuing till termination of the root goal
	 * @param baseState state to start planning from
	 * @return a episode start at given state containing all actions
	 * taken to complete goal
	 */
	public Episode planFromState(State baseState){
		State rootState = root.mapState(baseState);
		GroundedTask solve = root.getAllGroundedTasks(rootState).get(0);
		Episode e = new Episode(baseState);
		SimulatedEnvironment env = getBaseEnvirnment(root, baseState);
		return solveTask(solve, e, env);
	}

	/**
	 * the recursive function which solve a given task by executing 
	 * actions at each level of the hierarchy
	 * @param task the current task to solve
	 * @param e the current episode being created
	 * @param env a environment of the base domain to execute primitive actions in 
	 * @return the episode completed to the current task
	 */
	public Episode solveTask(GroundedTask task, Episode e, Environment env){
		if(task.isPrimitive()){
			Action a = task.getAction();
			EnvironmentOutcome result = env.executeAction(a);
			e.transition(result);
		}else{
			State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
			State currentState = task.mapState(baseState);

			//get the policy for the current task and start state and execute
			//it till task is completed or it fails
			Policy taskPolicy = getPolicy(task, currentState);
			while(!(task.isFailure(currentState) || task.isComplete(currentState))){
				Action a = taskPolicy.action(currentState);
				GroundedTask child = getChildGT(task, a, currentState);
				
				//recurse to solve the chosen subtask
				solveTask(child, e, env);
				
				//project the current base state into the current task's state space
				baseState = e.stateSequence.get(e.stateSequence.size() - 1);
				currentState = task.mapState(baseState);
			}
		}	
		return e;
	}
	
	/**
	 * get a valid policy for the given task and state
	 * @param t the current task
	 * @param s the current projected state
	 * @return a policy to solve the task
	 */
	private Policy getPolicy(GroundedTask t, State s){
		HashableState hscurrwnt = hs.hashState(s);
		
		//try to get the policy for task and state
		Map<HashableState, Policy> taskPolicies = this.taskPolicies.get(t.toString());
		if(taskPolicies == null){
			taskPolicies = new HashMap<HashableState, Policy>();
			this.taskPolicies.put(t.toString(), taskPolicies);
		}
		
		Policy p = taskPolicies.get(hscurrwnt);
		if(p == null){
			//generate a new policy using BRTDP planning to solve the task
			//create a copy of the task's domain with the same action the terminates and defines reward specific
			//to the task
			OOSADomain domain = t.getDomain();
			OOSADomain copy = new OOSADomain();
			List<ActionType> acts = domain.getActionTypes();
			for(ActionType a : acts){
				copy.addActionType(a);
			}
			
			FullModel generalModel = (FullModel) domain.getModel();
			FullModel newModel = new AMDPModel(t, generalModel);
			copy.setModel(newModel);
			
			//plan over the modified domain to solve the task
			BoundedRTDP brtdp = new BoundedRTDP(copy, gamma, hs, new ConstantValueFunction(0), new ConstantValueFunction(1),
					 maxDelta, maxRollouts);
			p = brtdp.planFromState(s);
			taskPolicies.put(hscurrwnt, p);
		}
		return p;
	}
	
	/**
	 * setup a environment to execute base actions with 
	 * @param t some task in the hierarchy
	 * @param s the current state 
	 * @return a base environment
	 */
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
	
	/**
	 * get the grounded task matching the given action
	 * @param t the task whose domain the action is defined in
	 * @param a the action to link to a subtask
	 * @param s the current subtask
	 * @return the grounded task that wraps around a
	 */
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
