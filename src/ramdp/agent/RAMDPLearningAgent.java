package ramdp.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
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
	
	private double maxDelta;
	
	private Episode e;
	
	/**
	 * 
	 * @param root
	 * @param threshold
	 */
	public RAMDPLearningAgent(GroundedTask root, int threshold, double discount, double rmax,
			HashableStateFactory hs, double delta) {
		this.rmaxThreshold = threshold;
		this.root = root;
		this.gamma = discount;
		this.hashingFactory = hs;
		this.rmax = rmax;
		this.models = new HashMap<GroundedTask, RAMDPModel>();
		this.taskNames = new HashMap<String, GroundedTask>();
		this.maxDelta = delta;
	}
	
	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		steps = 0;
		e = new Episode(env.currentObservation());
		solveTask(root, env, maxSteps);
		return e;
	}

	protected boolean solveTask(GroundedTask task, Environment baseEnv, int maxSteps){
		State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
		State currentState = task.mapState(baseState);
		State pastState = currentState;
		RAMDPModel model = getModel(task);
		int actionCount = 0;
		
		while(!(task.isFailure(currentState) || task.isComplete(currentState)) && (steps < maxSteps || maxSteps == -1)){
			actionCount++;
			boolean subtaskCompleted = false;
			Action a = nextAction(task, currentState);
			pastState = currentState;
			EnvironmentOutcome result;

			GroundedTask action = this.taskNames.get(a.actionName());
			if(action == null){
				addChildrenToMap(task, currentState);
				action = this.taskNames.get(a.actionName());
			}

			if(action.isPrimitive()){
				subtaskCompleted = true;
				result = baseEnv.executeAction(a);
				e.transition(result);
				baseState = result.op;
				currentState = task.mapState(result.op);
				result.r = task.getReward(currentState);
				steps++;
			}else{
				subtaskCompleted = solveTask(action, baseEnv, maxSteps);
				baseState = e.stateSequence.get(e.stateSequence.size() - 1);
				currentState = task.mapState(baseState);

				result = new EnvironmentOutcome(pastState, a, currentState,
						task.getReward(currentState), task.isFailure
						(currentState));
			}
			
			//update task model
			if(subtaskCompleted){
				model.updateModel(result);
			}
		}
		return task.isComplete(currentState) || actionCount == 0;
	}
	
	protected void addChildrenToMap(GroundedTask gt, State s){
		List<GroundedTask> chilkdren = gt.getGroundedChildTasks(s);
		for(GroundedTask child : chilkdren){
			taskNames.put(child.toString(), child);
		}
	}
	
	protected Action nextAction(GroundedTask task, State s){
		RAMDPModel model = getModel(task);
		OOSADomain domain = task.getDomain(model);
		ValueIteration plan = new ValueIteration(domain, gamma, hashingFactory, maxDelta, 1000);
		Policy viPolicy = plan.planFromState(s);
		Policy rmaxPolicy = new RMAXPolicy(model, viPolicy, domain.getActionTypes(), hashingFactory);
		
		return rmaxPolicy.action(s);
	}
	
	protected RAMDPModel getModel(GroundedTask t){
		RAMDPModel model = models.get(t);
				
		if(model == null){
			model = new RAMDPModel(t, this.rmaxThreshold, this.rmax, this.hashingFactory);
			this.models.put(t, model);
		}

		return model;
	}
}
