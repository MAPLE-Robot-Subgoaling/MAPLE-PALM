package ramdp.framework;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;

public class GroundedTask {

	/**
	 * specific action in a task
	 */
	Action action;
	
	/**
	 * the general task node which this grounded task is part of
	 */
	Task t;
	
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
	 * observe the result of executing an action ing the task's domain
	 * @param s the current state to perform the action in
	 * @param a the action to execute
	 * @return the envirnment opcome resulting from performing a in state s
	 */
	public EnvironmentOutcome executeAction(State s, Action a){
		SimulatedEnvironment env = new SimulatedEnvironment(t.getDomain(), s);
		return env.executeAction(a);
	}
}
