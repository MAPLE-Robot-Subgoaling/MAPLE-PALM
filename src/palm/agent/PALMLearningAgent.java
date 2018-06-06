
package palm.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.StringFormat;
import hierarchy.framework.Task;
import palm.rmax.agent.ExpectedRmaxModel;
import palm.rmax.agent.HierarchicalRmaxModel;
import utilities.DiscountProvider;
import utilities.ValueIteration;
import utilities.ValueIterationMultiStep;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PALMLearningAgent implements LearningAgent{

	public static boolean debug = false;

	/**
	 * The root of the task hierarchy
	 */
	private GroundedTask root;
	
	/**
	 * collection of models for each task
	 */
	private Map<GroundedTask, PALMModel> models;
	
	/**
	 * Steps currently taken
	 */
	private int steps;
	
	/**
	 * lookup grounded tasks by name task
	 */
	private Map<String, GroundedTask> taskNames;

	/**
	 * provided state hashing factory
	 */
	private HashableStateFactory hashingFactory;
	
	/**
	 * the max error allowed for the planner
	 */
	private double maxDelta;

	private int maxIterationsInModelPlanner = -1;

	/**
	 * the current episode
	 */
	private Episode e;

	private PALMModelGenerator modelGenerator;

	private long actualTimeElapsed = 0;

	/**
	 * create a RAMDP agent on a given task
	 * @param root the root of the hierarchy to learn
	 * @param hs a state hashing factory
	 * @param delta the max error for the planner
	 */
	public PALMLearningAgent(GroundedTask root, PALMModelGenerator models,
                             HashableStateFactory hs, double delta, int maxIterationsInModelPlanner) {
		this.root = root;
		this.hashingFactory = hs;
		this.models = new HashMap<GroundedTask, PALMModel>();
		this.taskNames = new HashMap<String, GroundedTask>();
		this.maxDelta = delta;
		this.maxIterationsInModelPlanner = maxIterationsInModelPlanner;
		this.modelGenerator = models;
	}
	
	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {

		//runtime
		long start = System.nanoTime();
		System.out.println("PALM episode start time: " + start);
		actualTimeElapsed = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		Date resultdate = new Date(actualTimeElapsed);
		System.out.println(sdf.format(resultdate));

		steps = 0;
		e = new Episode(env.currentObservation());
		solveTask(root, env, maxSteps);
		System.out.println(e.actionSequence.size() + " " + e.actionSequence);

		///for a chart of runtime
		long estimatedTime = System.nanoTime() - start;
		System.out.println("Estimated PALM episode nano time: " + estimatedTime);

		actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
		System.out.println("Clock time elapsed: " + actualTimeElapsed);

		return e;
	}

//	public static String tabLevel = "";

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
        PALMModel model = getModel(task);
		int actionCount = 0;

//		tabLevel += "\t";
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
            return true;
        }

        List<String> subtasksExecuted = new ArrayList<String>();

        boolean allChildrenBeyondThreshold = true;

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

			Action a = nextAction(task, currentState);
			String actionName = StringFormat.parameterizedActionName(a);
			GroundedTask action = this.taskNames.get(actionName);
			if(action == null){
				addChildrenToMap(task, currentState);
				action = this.taskNames.get(actionName);
			}

			if (allChildrenBeyondThreshold && !action.isPrimitive()) {
				State s = currentState;
				PALMModel m = getModel(task);
				if (!m.isConvergedFor(s, a, null)) {
					allChildrenBeyondThreshold = false;
				}
			}

            // solve this task's next chosen subtask, recursively
            int stepsBefore = steps;
            subtaskCompleted = solveTask(action, baseEnv, maxSteps);
            int stepsAfter = steps;
            int stepsTaken = stepsAfter - stepsBefore;

            baseState = e.stateSequence.get(e.stateSequence.size() - 1);
            currentState = task.mapState(baseState);
            double taskReward = task.getReward(pastState, a, currentState);
            result = new EnvironmentOutcome(pastState, a, currentState, taskReward, false); //task.isFailure(currentState));

			// update task model if the subtask completed correctly
            // the case in which this is NOT updated is if the subtask failed or did not take at least one step
            // for example, the root "solve" task may not complete
			if(subtaskCompleted) {
				model.updateModel(result, stepsTaken);
				subtasksExecuted.add(action.toString()+"++");
			} else {
                subtasksExecuted.add(action.toString()+"--");
			}
		}

		if (task.toString().contains("solve")) {
            System.out.println(subtasksExecuted.size() + " " + subtasksExecuted);
        }

//        tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));

		boolean parentShouldUpdateModel = task.isComplete(currentState) || actionCount == 0;
		parentShouldUpdateModel = parentShouldUpdateModel && allChildrenBeyondThreshold;
		return parentShouldUpdateModel;
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

	public static boolean PRINT = false;
	/**
	 * plan over the given task's model and pick the best action to do next favoring unmodeled actions
	 * @param task the current task
	 * @param s the current state
	 * @return the best action to take
	 */
	protected Action nextAction(GroundedTask task, State s){
        PALMModel model = getModel(task);
		OOSADomain domain = task.getDomain(model);
//		double discount = model.gamma();
		DiscountProvider discountProvider = model.getDiscountProvider();
		ValueIterationMultiStep planner = new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterationsInModelPlanner, discountProvider);
		planner.toggleReachabiltiyTerminalStatePruning(true);
//		planner.toggleReachabiltiyTerminalStatePruning(false);
		ValueFunction knownValueFunction = task.valueFunction;
		if (knownValueFunction != null) {
			planner.setValueFunctionInitialization(knownValueFunction);
		}
		Policy policy = planner.planFromState(s);
        Action action = policy.action(s);
//        if (PRINT) {
//        	PRINT = false;
//			Map<HashableState,Double> valueFunction = planner.getValueFunction();
//
//			valueFunction.entrySet().stream()
//					.sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
//					.forEach(k -> {
//						State state = k.getKey().s();
//						System.out.println("\n" + state + ": " + k.getValue());
//						List<QValue> qValues = planner.qValues(state);
//						Collections.sort(qValues, new Comparator<QValue>() {
//							@Override
//							public int compare(QValue o1, QValue o2) {
//								if (o1.q > o2.q) return -1;
//								if (o1.q < o2.q) return  1;
//								return 0;
//							}
//						});
//						for (QValue qValue : qValues) {
//							System.out.println(qValue.a + "\t" + qValue.q);
//						}
////						Action tempAction = policy.action(state);
////						double discounted = model.getDiscountProvider().yield(state, tempAction, null);
////						((HierarchicalRmaxModel)model).getPossibleOutcome(state, action);
////						double expectedSteps = ((ExpectedRmaxModel)model).getExpectedNumberOfSteps(state, tempAction, null);
////						System.out.println(tempAction + " " + discounted);// + " " + expectedSteps);
//					});
//
//
//			Episode e = PolicyUtils.rollout(policy, s, model, 10);
//			System.out.println("Debug rollout: " + e.actionSequence);
//			System.out.println(action + ", ");
//
//        	nextAction(task, s);
//		}
		if (debug) {
			try {
				if (task.toString().contains("solve")) {
					Episode e = PolicyUtils.rollout(policy, s, model, 10);
//					System.out.println(tabLevel + "    Debug rollout: " + e.actionSequence);
//					System.out.println(tabLevel + action + ", ");
				}
			} catch (Exception e) {
				//             ignore, temp debug to assess ramdp
				System.out.println(e);
				e.printStackTrace();
			}
		}
		double defaultValue = 0.0;
//		valueFunction = planner.saveValueFunction(defaultValue, rmax);
		task.valueFunction = knownValueFunction;
    	return action;
	}

	/**
	 * get the rmax model of the given task
	 * @param t the current task
	 * @return the learned rmax model of the task
	 */
	protected PALMModel getModel(GroundedTask t){
        PALMModel model = models.get(t);
		if(model == null){
			model = modelGenerator.getModelForTask(t);
			this.models.put(t, model);
		}
		return model;
	}
}

