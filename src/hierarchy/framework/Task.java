package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import taxi.hierarchies.interfaces.ParameterizedStateMapping;

import java.util.ArrayList;
import java.util.List;

public abstract class Task {

	/**
	 * domain - the MDP representing the level of abstraction
	 */
	protected OOSADomain domain;
	
	/**
	 * actionType - the general action type acossiated with the task 
	 */
	private ActionType actionType;
	
	/**
	 * children - the subtasks in a hierarchy
	 */
	private Task[] children;

	/**
	 * the function to map state at one level lower up to current level
	 */
	private StateMapping mapper;
	
	/**
	 * Setup of variables
	 * @param children the task's subtasks
	 * @param aType the general class of actions used by this task
	 * @param abstractDomain the domain at the correct level of abstraction
	 * @param map state mapper to the abstract domain
	 */
	public Task(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map){
		this.children = children;
		this.actionType = aType;
		this.domain = abstractDomain;
		this.mapper = map;
	}
	
	/**
	 * get the domain
	 * @return the abstract domain for the task
	 */
	public OOSADomain getDomain(){
		return domain;
	}

	public ActionType getActionType(){
		return actionType;
	}
	
	/**
	 * Gets all parameterizations of the task availibe in s
	 * @param s the current state
	 * @return list of grounded tasks which gives all variations 
	 * of the task in the current state
	 */
	public List<GroundedTask> getAllGroundedTasks(State s){
		List<Action> acts = actionType.allApplicableActions(s);
		List<GroundedTask> gts = new ArrayList<GroundedTask>();
		for(Action a : acts){
			gts.add(new GroundedTask(a, this));
		}
		return gts;
	}
	
	/**
	 * Gets the subtasks of the current task
	 * @return array of subtasks of the task
	 */
	public Task[] getChildren(){
		return children;
	}
	
	/**
	 * Projects a state at level L - 1 to level L
	 * @param lowerState state at level just below current level
	 * @return the same state but projected up one level
	 */
	public State mapState(State lowerState, String...params){
		if(mapper instanceof ParameterizedStateMapping)
			return ((ParameterizedStateMapping)mapper).mapState(lowerState, params);
		else
			return mapper.mapState(lowerState);
	}
	
	/**
	 * a unique ID
	 * @return unique ID for the task in the hierarchy
	 */
	public String getName(){
		return actionType.typeName();
	}
	
	/**
	 * determines if the current task is terminated in state s which parameterization a 
	 * @param s the current state
	 * @param a the action from the specific grounding
	 * @return boolean indicating if the action a is terminated in state s
	 */
	public abstract boolean isFailure(State s, Action a);
	
	/**
	 * tells whether this task is in the base MDP
	 * @return boolean indicating whether the task is 
	 * primitive (true) or composite (false)
	 */
	public abstract boolean isPrimitive();

	/**
	 * tests a state to determine if task is complete
	 * @param s state to test
	 * @param a the grounded task
	 * @return wether a is complete in s
	 */
	public abstract boolean isComplete(State s, Action a);
}
