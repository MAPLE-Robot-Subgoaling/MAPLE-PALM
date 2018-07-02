
package edu.umbc.cs.maple.palm.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.rmax.agent.PALMRmaxModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.RmaxModel;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.TaxiModel;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.IntegerParameterizedAction;
import edu.umbc.cs.maple.utilities.ValueIterationMultiStep;

import java.text.SimpleDateFormat;
import java.util.*;

import static edu.umbc.cs.maple.hierarchy.framework.GoalFailRF.PSEUDOREWARD_ON_FAIL;
import static edu.umbc.cs.maple.hierarchy.framework.GoalFailRF.PSEUDOREWARD_ON_GOAL;


public class PALMLearningAgent implements LearningAgent {

    /**
     * The root of the task hierarchy
     */
    private Task root;

    /**
     * The root task grounded to the initial state
     */
    private GroundedTask groundedRoot;

    /**
     * collection of models for each task
     */
    private Map<String, PALMModel> models;

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

    // if true, AMDP models will not update until all actions they took were also beyond RMAX threshold
    private boolean waitForChildren;

    // if true, uses model sharing, forcing the same model regardless of the parameterized object
    private boolean useModelSharing;

    /**
     * the current episode
     */
    private Episode e;

    private PALMModelGenerator modelGenerator;

    private long actualTimeElapsed = 0;

    /**
     * create a PALM agent on a given task
     * @param root the root of the hierarchy to learn
     * @param hsf a state hashing factory
     * @param maxDelta the max error for the planner
     */
    public PALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, double maxDelta, int maxIterationsInModelPlanner, boolean waitForChildren, boolean useModelSharing) {
        this.root = root;
        this.hashingFactory = hsf;
        this.models = new HashMap<>();
        this.taskNames = new HashMap<>();
        this.maxDelta = maxDelta;
        this.maxIterationsInModelPlanner = maxIterationsInModelPlanner;
        this.modelGenerator = modelGenerator;
        this.waitForChildren = waitForChildren;
        this.useModelSharing = useModelSharing;
    }

    public PALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, ExperimentConfig config) {
        this(root, modelGenerator, hsf, config.rmax.max_delta, config.rmax.max_iterations_in_model, config.rmax.wait_for_children, config.rmax.use_model_sharing);
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
        State initialState = env.currentObservation();

//        Taxi taxi = new Taxi();
//        taxi.generateDomain();
//        OOSADomain temp = taxi.generateDomain();
//        List<State> state = StateReachability.getReachableStates(initialState, temp, hashingFactory);
//        System.out.println(state.size());

        e = new Episode(initialState);
        groundedRoot = root.getAllGroundedTasks(initialState).get(0);
        tabLevel = "";
        solveTask(null, groundedRoot, env, maxSteps);
        System.out.println(e.actionSequence.size() + " " + e.actionSequence);
        System.out.println("Discounted return: " + e.discountedReturn(getModel(groundedRoot).getDiscountProvider().getGamma()));

        // run a rollout on the models resulting from the episode just computed,
        // to check if the root AMDP has reached a solution yet
        Action action = runDebugRollout(groundedRoot, initialState, maxSteps);
        runDebugRollout(taskNames.get(action.toString()), initialState, maxSteps);

        ///for a chart of runtime
        long estimatedTime = System.nanoTime() - start;
        System.out.println("Nano time elapsed:  " + estimatedTime);

        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
        System.out.println("Clock time elapsed: " + actualTimeElapsed);

        return e;
    }

    private Action runDebugRollout(GroundedTask groundedTask, State fromState, int maxSteps) {

        State abstractInitialState = groundedTask.mapState(fromState);
        PALMModel model = getModel(groundedTask);
        String[] params = getParams(groundedTask);
        OOSADomain domain = groundedTask.getDomain(model, params);
//		double discount = model.gamma();
        DiscountProvider discountProvider = model.getDiscountProvider();
        ValueIterationMultiStep planner = new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterationsInModelPlanner, discountProvider);
        planner.toggleReachabiltiyTerminalStatePruning(true);
        ValueFunction knownValueFunction = groundedTask.valueFunction;
        if (knownValueFunction != null) {
            planner.setValueFunctionInitialization(knownValueFunction);
        }
        Policy policy = planner.planFromState(abstractInitialState);
        Episode ep = PolicyUtils.rollout(policy, abstractInitialState, model, maxSteps);
        OOState last = (OOState) ep.stateSequence.get(ep.stateSequence.size()-1);
        if (last.numObjects() > 0) {
            System.out.println("    Policy converged, rollout: " + ep.actionSequence);
        } else {
            System.out.println("    unconverged...");
        }
        return ep.actionSequence.get(0);
    }

    private String tabLevel = "";

    /**
     * tries to solve a grounded task while creating a model of it
     * @param task the grounded task to solve
     * @param baseEnv a environment defined by the base domain and at the current base state
     * @param maxSteps the max number of primitive actions that can be taken
     * @return whether the task was completed
     */
    protected boolean solveTask(GroundedTask parent, GroundedTask task, Environment baseEnv, int maxSteps){

        tabLevel += "\t";

        State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
        State currentState = task.mapState(baseState);
        State pastState = currentState;
        PALMModel model = getModel(task);
        int actionCount = 0;

        if(task.isPrimitive()) {
            EnvironmentOutcome result;
            Action a = task.getAction();
            Action unMaskedAction = a;
            //somewhat generalized unmasking:
            //copy the action, and unmask the copy, execute the unmasked action
            //this allows the task to always store the masked version for model/planning purposes
            if (parent.isMasked()) {
                unMaskedAction = a.copy();
                //for now, reliant on parent of masked task to be unmasked. This may not be a safe assumption
                //there may be a need to traverse arbitrarily far up the task hierarchy to find an unmasked ancestor
                //in order to recover the true parameters.
                String trueParameters = ((ObjectParameterizedAction)parent.getAction()).getObjectParameters()[0];
                ((ObjectParameterizedAction) unMaskedAction).getObjectParameters()[0] = trueParameters;
            }
//            System.out.println(tabLevel + "    " + a.toString());
            result = baseEnv.executeAction(unMaskedAction);

            SimulatedEnvironment simEnv = (SimulatedEnvironment)((EnvironmentServer)baseEnv).getEnvironmentDelegate();
            simEnv.setAllowActionFromTerminalStates(false);

            e.transition(result);
            baseState = result.op;
            currentState = task.mapState(result.op);
            result.o = pastState;
            result.op = currentState;
            result.a = unMaskedAction;
            String[] params = getParams(task);
            result.r = task.getReward(pastState, unMaskedAction, currentState, params);
            steps++;
            return true;
        }

        List<String> subtasksExecuted = new ArrayList<>();

        boolean allChildrenBeyondThreshold = true;

		while(
			// while task still valid
		        !(task.isFailure(currentState) || task.isComplete(currentState))
			// and still have steps it can take
                && (steps < maxSteps || maxSteps == -1)
            // and it hasn't solved the root goal, keep planning
            //    && !(groundedRoot.isComplete(groundedRoot.mapState(baseState)))
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

			if (waitForChildren && allChildrenBeyondThreshold && !action.isPrimitive()) {
				State s = currentState;
				PALMModel m = getModel(task);
				if (!m.isConvergedFor(s, a, null)) {
					allChildrenBeyondThreshold = false;
				}
			}
            // solve this task's next chosen subtask, recursively
            int stepsBefore = steps;
//            System.out.println(tabLevel + ">>>>> " + task.toString() + " >>>>> " + action);
            subtaskCompleted = solveTask(task, action, baseEnv, maxSteps);
            int stepsAfter = steps;
            int stepsTaken = stepsAfter - stepsBefore;
            if (stepsTaken == 0) {
                System.err.println("took a 0 step action");
            }

            tabLevel = tabLevel.substring(0,tabLevel.length()-1);

            baseState = e.stateSequence.get(e.stateSequence.size() - 1);
            currentState = task.mapState(baseState);
//            double sumChildRewards = e.rewardSequence.subList(stepsBefore, stepsAfter).stream().mapToDouble(Double::doubleValue).sum();
//            double discount = 1.0;// getModel(task).getDiscountProvider().yield(pastState, a, currentState);
//            double discountOverTime = Math.pow(discount, stepsTaken);
//            double discountedReward = sumChildRewards * discountOverTime;
            String[] params = getParams(task);
            double pseudoReward = task.getReward(pastState, a, currentState, params);
//            System.out.println(sumChildRewards + " " + discount + " " + discountOverTime + " " + discountedReward);
//            double taskReward = discountedReward + task.getReward(pastState, a, currentState);
            double taskReward = pseudoReward;//task.getReward(pastState, a, currentState);
//            double taskReward = pseudoReward <= PSEUDOREWARD_ON_FAIL || pseudoReward >= PSEUDOREWARD_ON_GOAL ? pseudoReward : discountedReward;
//            double taskReward = discountedReward;
            result = new EnvironmentOutcome(pastState, a, currentState, taskReward, false);

            // update task model if the subtask completed correctly
            // the case in which this is NOT updated is if the subtask failed or did not take at least one step
            // for example, the root "solve" task may not complete
			if(subtaskCompleted) {
				model.updateModel(result, stepsTaken, params);
				subtasksExecuted.add(action.toString()+"++");
			} else {
                subtasksExecuted.add(action.toString()+"--");
            }
        }

//        if (task.toString().contains("solve")) {
            System.out.println(subtasksExecuted.size() + " " + subtasksExecuted);
//        }

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
        String[] params = getParams(task);
		OOSADomain domain = task.getDomain(model, params);
		// must use discountProvider instead of gamma
		DiscountProvider discountProvider = model.getDiscountProvider();
		ValueIterationMultiStep planner = new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterationsInModelPlanner, discountProvider);
		planner.toggleReachabiltiyTerminalStatePruning(true);
		ValueFunction knownValueFunction = task.valueFunction;
		if (knownValueFunction != null) {
			planner.setValueFunctionInitialization(knownValueFunction);
		}
		Policy policy = planner.planFromState(s);
        Action action = policy.action(s);
        boolean debug = false;
		if (debug) {
			try {
//				if (task.toString().contains("solve")) {
                    System.out.println(tabLevel + "    Task:    " + task.toString() + " " + s.toString());
					Episode e = PolicyUtils.rollout(policy, s, model, 100);
					OOState last = (OOState) e.stateSequence.get(e.stateSequence.size()-1);
					if (last.numObjects() > 0) {
                        System.out.println(tabLevel + "    Rollout: " + e.actionSequence);
                        System.out.println(tabLevel + "    Chose:   "+action);
                        System.out.println(tabLevel + "    Done?: " + task.isComplete(last));
                    }
//				}
			} catch (Exception e) {
				//             ignore, temp debug to assess palm
				System.out.println(e);
				e.printStackTrace();
			}
		}

		// allows the planner to start from where it left off
		task.valueFunction = planner;

		// clear the model parameters (sanity check)
        model.setParams(null);
    	return action;
	}

    protected PALMModel getModel(GroundedTask t){
//        PALMModel model = models.get(t);
        // idea: try to do lookup such that models are shared across certain AMDP classes
        // for example, instead of 4 put passenger AMDPs, one for each passenger
        // we mask the name of the passenger and share models, treating every passenger the same
        String modelName = t.isMasked() && useModelSharing ? t.getAction().actionName() : t.toString();
        PALMModel model = models.get(modelName);
        if(model == null) {
            model = modelGenerator.getModelForTask(t);
            this.models.put(modelName, model);
        }
        return model;
    }

    protected String[] getParams(GroundedTask task) {
        Action taskAction = task.getAction();
        return getParams(taskAction);
    }

    protected String[] getParams(Action taskAction) {
        String[] params = null;
        if (taskAction instanceof ObjectParameterizedAction) {
            params = Task.parseParams(taskAction);
        } else if (taskAction instanceof IntegerParameterizedAction) {
            params = Task.parseParams(taskAction);
        }
        return params;
    }

}

