package hierarchy.framework;

import java.util.ArrayList; 
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;

public class GroundedTask {

	/**
	 * specific action in a task
	 */
	private Action action;
	
	/**
	 * the general task node which this grounded task is part of
	 */
	private Task t;
	
	/**
	 * each grounded task has an action and task
	 * @param a the parameterization of the task
	 * @param t the general task this is a part of
	 */
	public GroundedTask(Action a, Task t){
		this.action = a;
		this.t = t;
	}
	
	/**
	 * gets the action this task wraps around 
	 * @return the grounded task's action
	 */
	public Action getAction(){
		return action;
	}
	
	public OOSADomain getDomain(FullModel model){
		OOSADomain d = new OOSADomain();
		d.setModel(model);
		List<ActionType> acts = t.domain.getActionTypes();
		for(ActionType a : acts){
			d.addActionType(a);
		}
		return d;
	}
	/**
	 * gets a executable tasks that are children of the task 
	 * @param s the current task
	 * @return list of all groundings of child tasks
	 */
	public List<GroundedTask> getGroundedChildTasks(State s){
		Task[] children = t.getChildren();
		List<GroundedTask> gts = new ArrayList<GroundedTask>();
		for(Task t : children){
			gts.addAll(t.getAllGroundedTasks(s));
		}
		return gts;
	}

	/**
	 * Determines if this task is terminated
	 * @param s the current state
	 * @return if this task is terminal in s
	 */
	public boolean isTerminal(State s){
		return t.isTerminal(s, action);
	}
	
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof GroundedTask)) {
            return false;
        }

        GroundedTask o = (GroundedTask) other;
        if(!this.action.actionName().equals(o.action.actionName())){
            return false; 
        }
        
        return true;
    }
     
    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(31, 7);
        hashCodeBuilder.append(action.actionName());
        return hashCodeBuilder.toHashCode();
    }
	/**
	 * observe the result of executing an action ing the task's domain
	 * @param s the current state to perform the action in
	 * @param a the action to execute
	 * @return the envirnment opcome resulting from performing a in state s
	 */
	public EnvironmentOutcome executeAction(State s, Action a){
		SimulatedEnvironment env = new SimulatedEnvironment(t.getDomain(), s);
		return env.executeAction(a);
	}
	
	public State mapState(State s){
		return t.projectState(s);
	}
	
	public boolean isPrimitive(){
		return t.isPrimitive();
	}
	
	public void fixReward(EnvironmentOutcome eo){
		if(!t.isPrimitive()){
			NonprimitiveTask npt = (NonprimitiveTask) t;
			eo.r = npt.reward(eo.op, action);
		}
	}
	
	public double reward(State s){
		if(!t.isPrimitive()){
			NonprimitiveTask npt = (NonprimitiveTask) t;
			return npt.reward(s, action);
		}
		return 0;
	}
}
