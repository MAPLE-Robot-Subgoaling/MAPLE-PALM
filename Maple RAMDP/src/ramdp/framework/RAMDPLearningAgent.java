package ramdp.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

public class RAMDPLearningAgent implements LearningAgent{

	/**
	 * The root of the task hierarchy
	 */
	private GroundedTask root;
	
	/**
	 * r-max "m" parameter
	 */
	private int rmaxThreshold;
	
	/**
	 * collection of models for each task
	 */
	private Map<GroundedTask, RAMDPModel> models;
	
	/**
	 * Steps currently taken
	 */
	private int steps;
	
	/**
	 * lookup grounded tasks by name task
	 */
	private Map<String, GroundedTask> taskNames;
	/**
	 * 
	 * @param root
	 * @param threshold
	 */
	public RAMDPLearningAgent(GroundedTask root, int threshold) {
		this.rmaxThreshold = threshold;
		this.root = root;
	}
	
	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		steps = 0;
		Episode e = new Episode(env.currentObservation());
		return solveTask(root, e, maxSteps);
	}

	protected Episode solveTask(GroundedTask task, Episode e, int maxSteps){
		State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
		State currentState = task.mapState(baseState);
		
		while(!task.isTerminal(currentState) && (steps < maxSteps || maxSteps == -1)){
			Action a;
			EnvironmentOutcome result = task.executeAction(currentState, a);

			if(task.isPrimitive()){
				e.transition(result);
			}else{
				//get child task
				GroundedTask action = this.taskNames.get(a.actionName());
				if(action == null){
					addChildrenToMap(task, currentState);
					action = this.taskNames.get(a.actionName());
				}
				e = solveTask(action, e, maxSteps);
			}
			baseState = e.stateSequence.get(e.stateSequence.size() - 1);
			currentState = task.mapState(baseState);
			result.op = currentState;
			//update task model
		}
		
		return e;
	}
	
	protected void addChildrenToMap(GroundedTask gt, State s){
		List<GroundedTask> chilkdren = gt.getGroundedChildTasks(s);
		for(GroundedTask child : chilkdren){
			taskNames.put(child.getAction().actionName(), child);
		}
	}
}
