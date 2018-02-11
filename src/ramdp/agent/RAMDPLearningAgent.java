package ramdp.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import com.sun.javafx.binding.StringFormatter;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.StringFormat;
import utilities.ValueIteration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/**
	 * the discount factor
	 */
	private double gamma;
	
	/**
	 * provided state hashing factory
	 */
	private HashableStateFactory hashingFactory;
	
	/**
	 * the max error allowed for the planner
	 */
	private double maxDelta;
	
	/**
	 * the current episode
	 */
	private Episode e;

	/**
	 * create a RAMDP agent on a given task
	 * @param root the root of the hierarchy to learn
	 * @param threshold the rmax sample threshold
	 * @param discount the discount for the tasks' domains 
	 * @param rmax the max reward
	 * @param hs a state hashing factory
	 * @param delta the max error for the planner
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
		System.out.println(e.actionSequence);
		return e;
	}

	public static String tabLevel = "";

	/**
	 * tries to solve a grounded task while creating a model of it
	 * @param task the grounded task to solve
	 * @param baseEnv a environment defined by the base domain and at the current base state
	 * @param maxSteps the max number of primitive actions that can be taken
	 * @return whether the task was completed 
	 */
	protected boolean solveTask(GroundedTask task, Environment baseEnv, int maxSteps){
		State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
		State currentState = task.mapState(baseState);
		State pastState = currentState;
		RAMDPModel model = getModel(task);
		int actionCount = 0;

		tabLevel += "\t";
//        System.out.println(tabLevel + ">>> " + task.getAction() + " " + actionCount);

		while(
			// while task still valid
		        !(task.isFailure(currentState) || task.isComplete(currentState))
			// and still have steps it can take
                && (steps < maxSteps || maxSteps == -1)
			// and it hasn't solved the root goal, keep planning
                && !(root.isComplete(root.mapState(baseState)))
        ){
			actionCount++;
			boolean subtaskCompleted = false;
			pastState = currentState;
			EnvironmentOutcome result;

            System.out.println(tabLevel + task.getAction() + " " + actionCount);
			//System.out.println(tabLevel + "    Possible Actions: " + task.getGroundedChildTasks(currentState));
			Action a = nextAction(task, currentState);
			String actionName = StringFormat.parameterizedActionName(a);
			GroundedTask action = this.taskNames.get(actionName);
			if(action == null){
				addChildrenToMap(task, currentState);
				action = this.taskNames.get(actionName);
			}


			if(action.isPrimitive()){
                System.out.println(tabLevel + "    " + actionName);
				subtaskCompleted = true;
				result = baseEnv.executeAction(a);
				e.transition(result);
				baseState = result.op;
				currentState = task.mapState(result.op);
				result.o = pastState;
				result.op = currentState;
				result.a = a;
				result.r = task.getReward(pastState, a, currentState);
				steps++;
			}else{
			    int stepsBefore = steps;
				subtaskCompleted = solveTask(action, baseEnv, maxSteps);
				int stepsAfter = steps;
				int stepsTaken = stepsAfter - stepsBefore;
				//System.out.println(tabLevel + "+++ " + task.getAction() + " " + actionCount);
				baseState = e.stateSequence.get(e.stateSequence.size() - 1);
				currentState = task.mapState(baseState);

                // use multi-time model discounting ((gamma^k)*reward) for k steps taken by multi-time model)
				double discount = Math.pow(gamma, stepsTaken);
				double discountedReward = discount * task.getReward(pastState, a, currentState);
				System.out.println(StringFormat.parameterizedActionName(action.getAction()) + ": " + e.actionSequence.get(e.actionSequence.size()-1) + " " + stepsBefore + " " + stepsAfter + " " + stepsTaken + " " + discountedReward);
                System.out.println(e.actionSequence.subList(stepsBefore,e.actionSequence.size()));
				result = new EnvironmentOutcome(pastState, a, currentState,
						discountedReward, task.isFailure
						(currentState));
			}
            //System.out.println(tabLevel + "\treward: " + result.r);

			//update task model if the subtask completed correctly
			if(subtaskCompleted){
				model.updateModel(result);
			}
		}

		System.out.println(tabLevel + "<<< " + StringFormat.parameterizedActionName(task.getAction()) + " " + actionCount);
        tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));
		return task.isComplete(currentState) || actionCount == 0;
	}
	
	/**
	 * add the children of the given task to the action name lookup
	 * @param gt the current grounded task
	 * @param s the current state
	 */
	protected void addChildrenToMap(GroundedTask gt, State s){
		List<GroundedTask> chilkdren = gt.getGroundedChildTasks(s);
		for(GroundedTask child : chilkdren){
			taskNames.put(child.toString(), child);
		}
	}
	
	/**
	 * plan over the given task's model and pick the best action to do next favoring unmodeled actions
	 * @param task the current task
	 * @param s the current state
	 * @return the best action to take
	 */
	protected Action nextAction(GroundedTask task, State s){
		RAMDPModel model = getModel(task);
		OOSADomain domain = task.getDomain(model);
		ValueIteration plan = new ValueIteration(domain, gamma, hashingFactory, maxDelta, 1000);
		Policy viPolicy = plan.planFromState(s);
		Policy rmaxPolicy = new RMAXPolicy(model, viPolicy, domain.getActionTypes(), hashingFactory);
		Action action = rmaxPolicy.action(s);
		try {
            Episode e = PolicyUtils.rollout(rmaxPolicy, s, model, 100);
            System.out.println(tabLevel + "    Debug rollout: " + e.actionSequence);
        } catch (Exception e) {
		    // ignore, temp debug to assess ramdp
//            System.err.println(e);
        }
		return action;
	}

	/**
	 * get the rmax model of the given task
	 * @param t the current task
	 * @return the learned rmax model of the task
	 */
	protected RAMDPModel getModel(GroundedTask t){
		RAMDPModel model = models.get(t);
		if(model == null){
			model = new RAMDPModel(t, this.rmaxThreshold, this.rmax, this.hashingFactory);
			this.models.put(t, model);
		}
		return model;
	}
}
