package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import ramdp.agent.RAMDPModel;

public class NonprimitiveTask extends Task{
	//tasks which are not at the base of the hierarchy
	
	/**
	 * the function which assigns rewards for transitions in the task 
	 */
	private RewardFunction rf;
	
	/**
	 * the functions that test states for completion and failure
	 */
	private PropositionalFunction failure, completed;
	
	//used for hierarchies with abstractions
	/**
	 * create a nunprimitive taks
	 * @param children the subtasks
	 * @param aType the set of actions this task represents in its parent task's domain
	 * @param abstractDomain the domain this task executes actions in
	 * @param map the state abstraction function into the domain
	 * @param fail the failure PF 
	 * @param compl the completion PF
	 */
	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map,
			PropositionalFunction fail, PropositionalFunction compl) {
		super(children, aType, abstractDomain, map);
		this.rf = new NonprimitiveRewardFunction(this);
		this.failure = fail;
		this.completed = compl; 
	}

	//used for hierarchies with no abstraction
	/**
	 * create a nunprimitive taks
	 * @param children the subtasks
	 * @param aType the set of actions this task represents in its parent task's domain
	 * @param term the failure PF
	 * @param compl the completion PF
	 */
	public NonprimitiveTask(Task[] children, ActionType aType,
			PropositionalFunction term, PropositionalFunction compl) {
		super(children, aType,  null, null);
		this.rf = new NonprimitiveRewardFunction(this);
		this.failure = term;
		this.completed = compl;
	}
	
	/**
	 * 
	 * create a nunprimitive taks
	 * @param children the subtasks
	 * @param aType the set of actions this task represents in its parent task's domain
	 * @param abstractDomain the domain this task executes actions in
	 * @param map the state abstraction function into the domain
	 * @param taskrf the custom reward function for the task
	 * @param term the failure PF
	 * @param compl the completion PF
	 */
	public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map,
			 RewardFunction taskrf, PropositionalFunction term, PropositionalFunction compl) {
		super(children, aType, abstractDomain, map);
		this.rf = taskrf;
		this.failure = term;
		this.completed = compl;
	}
	
	@Override
	public boolean isPrimitive() {
		return false;
	}
	
	/**
	 * uses the defined reward function to assign reward to states
	 * @param s the original state that is being transitioned from
	 * @param a the action associated with the grounded version of this task
	 * @param sPrime the next state that is being transitioned into
	 * @return the reward assigned to s by the reward function
	 */
	public double reward(State s, Action a, State sPrime){
		return rf.reward(s, a, sPrime);
	}
	
	/**
	 * customise the reward function
	 * @param rf the reward function which should take in a state and
	 * grounded action
	 */
	public void setRF(RewardFunction rf){
		this.rf = rf;
	}

	//these functions use the two propositional function provided
	//to test states for completion and failure of the grounded 
	//task's action give by a
	@Override
	public boolean isFailure(State s, Action a) {
		return failure.isTrue((OOState) s, RAMDPModel.getActionNameSafe(a));
	}
	
	@Override
	public boolean isComplete(State s, Action a){
//        return completed.isTrue((OOState) s, a.actionName());
		if (a instanceof ObjectParameterizedAction) {
			return completed.isTrue((OOState) s, ((ObjectParameterizedAction) a).getObjectParameters());
		} else {
			return completed.isTrue((OOState) s, RAMDPModel.getActionNameSafe(a));
		}
	}
}
