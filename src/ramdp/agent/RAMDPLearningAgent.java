package ramdp.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import com.sun.javafx.binding.StringFormatter;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.StringFormat;
import state.hashing.simple.CachedHashableStateFactory;
import utilities.BoundedRTDP;
import utilities.ValueIteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RAMDPLearningAgent implements LearningAgent{

	public static boolean debug = false;

	/**
	 * The root of the task hierarchy
	 */
	private GroundedTask root;
	
	/**
	 * r-max "m" parameter
	 */
	private int rmaxThreshold;
	
	/**
	 * maximum rewardTotal
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

	private int maxIterationsInModelPlanner = -1;

	private boolean useMultitimeModel;
	
	/**
	 * the current episode
	 */
	private Episode e;

	/**
	 * create a RAMDP agent on a given task
	 * @param root the root of the hierarchy to learn
	 * @param threshold the rmax sample threshold
	 * @param discount the discount for the tasks' domains 
	 * @param rmax the max rewardTotal
	 * @param hs a state hashing factory
	 * @param delta the max error for the planner
	 */
	public RAMDPLearningAgent(GroundedTask root, int threshold, double discount, double rmax,
			HashableStateFactory hs, double delta, int maxIterationsInModelPlanner, boolean useMultitimeModel) {
		this.rmaxThreshold = threshold;
		this.root = root;
		this.gamma = discount;
		this.hashingFactory = hs;
		this.rmax = rmax;
		this.models = new HashMap<GroundedTask, RAMDPModel>();
		this.taskNames = new HashMap<String, GroundedTask>();
		this.maxDelta = delta;
		this.maxIterationsInModelPlanner = maxIterationsInModelPlanner;
		this.useMultitimeModel = useMultitimeModel;
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
		System.out.println(e.actionSequence.size() + " " + e.actionSequence);
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

        if(task.isPrimitive()) {
            EnvironmentOutcome result;
            Action a = task.getAction();
//                System.out.println(tabLevel + "    " + actionName);
//            subtaskCompleted = true;
            result = baseEnv.executeAction(a);
            e.transition(result);
            baseState = result.op;
            currentState = task.mapState(result.op);
            result.o = pastState;
            result.op = currentState;
            result.a = a;
            result.r = task.getReward(pastState, a, currentState);
            steps++;
        }

        List<String> subtasksExecuted = new ArrayList<String>();

//        int stepsBefore = steps;
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

//            System.out.println(tabLevel + task.getAction() + " " + actionCount);
			//System.out.println(tabLevel + "    Possible Actions: " + task.getGroundedChildTasks(currentState));
			Action a = nextAction(task, currentState);
			String actionName = StringFormat.parameterizedActionName(a);
			GroundedTask action = this.taskNames.get(actionName);
			if(action == null){
				addChildrenToMap(task, currentState);
				action = this.taskNames.get(actionName);
			}

            int stepsBefore = steps;

			// solve this task's next chosen subtask, recursively
            subtaskCompleted = solveTask(action, baseEnv, maxSteps);

            int stepsAfter = steps;
            int stepsTaken = stepsAfter - stepsBefore;
            //System.out.println(tabLevel + "+++ " + task.getAction() + " " + actionCount);
            baseState = e.stateSequence.get(e.stateSequence.size() - 1);
            currentState = task.mapState(baseState);

            double taskReward = task.getReward(pastState, a, currentState);
            result = new EnvironmentOutcome(pastState, a, currentState, taskReward, false); //task.isFailure(currentState));

			//update task model if the subtask completed correctly
            // the case in which this is NOT updated is if the subtask failed or did not take at least one step
            // for example, the root "solve" task may not complete
			if(subtaskCompleted){
				model.updateModel(result, gamma, stepsTaken);
				subtasksExecuted.add(action.toString()+"++");
			} else {
                subtasksExecuted.add(action.toString()+"--");
			}
		}

		if (task.toString().contains("solve")) {
            System.out.println(subtasksExecuted.size() + " " + subtasksExecuted);
        }

//		System.out.println(tabLevel + "<<< " + StringFormat.parameterizedActionName(task.getAction()) + " " + actionCount);
        tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));
		return task.isComplete(currentState) || actionCount == 0;
	}
	
	/**
	 * add the children of the given task to the action name lookup
	 * @param gt the current grounded task
	 * @param s the current state
	 */
	protected void addChildrenToMap(GroundedTask gt, State s){
		List<GroundedTask> children = gt.getGroundedChildTasks(s);
		for(GroundedTask child : children){
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
//		double discount = useMultitimeModel ? 1.0 : gamma;
		double discount = gamma;
//        ValueFunction lowerVInit = new ConstantValueFunction(-model.getRmax());
//        ValueFunction upperVInit = new ConstantValueFunction(model.getRmax());
//		BoundedRTDP planner = new BoundedRTDP(domain, discount, hashingFactory, lowerVInit, upperVInit, 0.001, 1000);
//		planner.setMaxRolloutDepth(1000);
        ValueIteration planner = new ValueIteration(domain, discount, hashingFactory, maxDelta, maxIterationsInModelPlanner);
        planner.toggleReachabiltiyTerminalStatePruning(true);
//		ValueFunction valueFunction = task.valueFunction;
//		if (valueFunction != null) {
//			planner.setValueFunctionInitialization(valueFunction);
//		}
		Policy policy = planner.planFromState(s);
		if (debug) {
			DynamicProgramming dp = planner.getCopyOfValueFunction();
			List<State> states = planner.getAllStates();
			for (State state : states) {
				System.out.println(state);
				for (QValue qv : dp.qValues(state)) {
					System.out.println(qv.a + " " + qv.q);
				}
			}
		}
        Action action = policy.action(s);
		if (debug) {
			try {
				if (task.toString().contains("solve")) {
					Episode e = PolicyUtils.rollout(policy, s, model, 10);
					System.out.println(tabLevel + "    Debug rollout: " + e.actionSequence);
					System.out.println(tabLevel + action + ", ");
				}
			} catch (Exception e) {
				//             ignore, temp debug to assess ramdp
				System.out.println(e);
				e.printStackTrace();
			}
		}
//		double defaultValue = 0.0;
//		valueFunction = planner.saveValueFunction(defaultValue, rmax);
//		task.valueFunction = valueFunction;
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
			model = new RAMDPModel(t, this.rmaxThreshold, this.rmax, this.hashingFactory, this.useMultitimeModel);
			this.models.put(t, model);
		}
		return model;
	}
}
