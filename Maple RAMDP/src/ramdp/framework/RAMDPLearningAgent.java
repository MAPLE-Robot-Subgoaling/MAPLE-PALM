package ramdp.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.planning.Planner;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import utilities.ValueIteration;

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
	 * maximum reward
	 */
	private double rmax;
	
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
	
	private double gamma;
	
	private HashableStateFactory hashingFactory;
	
	/**
	 * 
	 * @param root
	 * @param threshold
	 */
	public RAMDPLearningAgent(GroundedTask root, int threshold, double discount, double rmax, HashableStateFactory hs) {
		this.rmaxThreshold = threshold;
		this.root = root;
		this.gamma = discount;
		this.hashingFactory = hs;
		this.rmax = rmax;
		this.models = new HashMap<GroundedTask, RAMDPModel>();
		this.taskNames = new HashMap<String, GroundedTask>();
	}
	
	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		steps = 0;
		Episode e = new Episode(env.currentObservation());
		return solveTask(root, e, env, maxSteps);
	}

	protected Episode solveTask(GroundedTask task, Episode e, Environment baseEnv, int maxSteps){
		State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
		State currentState = task.mapState(baseState);
		
		while(!task.isTerminal(currentState) && (steps < maxSteps || maxSteps == -1)){
			Action a = nextAction(task, currentState);
			EnvironmentOutcome result;

			GroundedTask action = this.taskNames.get(a.actionName());
			if(action == null){
				addChildrenToMap(task, currentState);
				action = this.taskNames.get(a.actionName());
			}
			if(action.isPrimitive()){
				result = baseEnv.executeAction(a);
				e.transition(result);
				steps++;
			}else{
				//get child task
				result = task.executeAction(currentState, a);
				e = solveTask(action, e, baseEnv, maxSteps);
			}
			
			baseState = e.stateSequence.get(e.stateSequence.size() - 1);
			currentState = task.mapState(baseState);
			result.op = currentState;
			
			if(task.isTerminal(currentState) && !task.isPrimitive()){
				System.out.println(result.r);
			}
			//update task model
			RAMDPModel model = getModel(task, currentState);
			model.updateModel(result);
		}
		
		return e;
	}
	
	protected void addChildrenToMap(GroundedTask gt, State s){
		List<GroundedTask> chilkdren = gt.getGroundedChildTasks(s);
		for(GroundedTask child : chilkdren){
			taskNames.put(child.getAction().actionName(), child);
		}
	}
	
	protected Action nextAction(GroundedTask task, State s){
		OOSADomain domain = task.getDomain(getModel(task, s));
		Planner plan = new ValueIteration(domain, gamma, hashingFactory, 0.01, 1);
		Policy p = plan.planFromState(s);
		return p.action(s);
	}
	
	protected RAMDPModel getModel(GroundedTask t, State s){
		RAMDPModel model = models.get(t);
		if(model == null){
			model = new RAMDPModel(t, this.rmaxThreshold, this.rmax, this.hashingFactory);
			this.models.put(t, model);
		}
		return model;
	}
}
