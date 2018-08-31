
package edu.umbc.cs.maple.palm.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.config.solver.FittedVIConfig;
import edu.umbc.cs.maple.config.solver.SarsaLambdaConfig;
import edu.umbc.cs.maple.config.solver.SolverConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.DynamicProgrammingMultiStep;
import edu.umbc.cs.maple.utilities.IntegerParameterizedAction;
import edu.umbc.cs.maple.utilities.ValueIterationMultiStep;

import java.text.SimpleDateFormat;
import java.util.*;


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
    protected Map<String, GroundedTask> taskNames;

    /**
     * provided state hashing factory
     */
    private HashableStateFactory hashingFactory;

    /**
     * the max error allowed for the planner
     */
    private double maxDelta;

    private int maxIterationsInModelPlanner = -1;

    // if true, uses model sharing, forcing the same model regardless of the parameterized object
    private boolean useModelSharing;

    /**
     * the current episode
     */
    private Episode e;

    private PALMModelGenerator modelGenerator;

    /**
     * create a PALM agent on a given task
     *
     * @param root     the root of the hierarchy to learn
     * @param hsf      a state hashing factory
     * @param maxDelta the max error for the planner
     */
    public PALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, double maxDelta, int maxIterationsInModelPlanner, boolean useModelSharing) {
        this.root = root;
        this.hashingFactory = hsf;
        this.models = new HashMap<>();
        this.taskNames = new HashMap<>();
        this.maxDelta = maxDelta;
        this.maxIterationsInModelPlanner = maxIterationsInModelPlanner;
        this.modelGenerator = modelGenerator;
        this.useModelSharing = useModelSharing;
    }

    public PALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, ExperimentConfig config) {
        this(root, modelGenerator, hsf, config.rmax.max_delta, config.rmax.max_iterations_in_model, config.rmax.use_model_sharing);
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
        long actualTimeElapsed = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(actualTimeElapsed);
        System.out.println(sdf.format(resultdate));

        steps = 0;
        State initialState = env.currentObservation();

        e = new Episode(initialState);
        groundedRoot = root.getAllGroundedTasks(initialState).get(0);
        tabLevel = "";
        solveTask(null, groundedRoot, env, maxSteps);
        System.out.println(e.actionSequence.size() + " " + e.actionSequence);
        System.out.println("Discounted return: " + e.discountedReturn(getModel(groundedRoot).getDiscountProvider().getGamma()));

        System.out.println(models.size() + " models, " + taskNames.size() + " named tasks");

        // run a rollout on the models resulting from the episode just computed,
        // to check if the root AMDP has reached a solution yet
//        Action action = runDebugRollout(groundedRoot, initialState, maxSteps);
//        System.out.println(e.rewardSequence.get(e.rewardSequence.size()-1));
//        runDebugRollout(taskNames.get(action.toString()), initialState, maxSteps);

        long estimatedTime = System.nanoTime() - start;
        System.out.println("Nano time elapsed:  " + estimatedTime);

        return e;
    }

    private Action runDebugRollout(GroundedTask groundedTask, State fromState, int maxSteps) {

        State abstractInitialState = groundedTask.mapState(fromState);
        PALMModel model = getModel(groundedTask);
        String[] params = getParams(groundedTask);
        OOSADomain domain = groundedTask.getDomain(model, params);
        DiscountProvider discountProvider = model.getDiscountProvider();
        ValueIterationMultiStep planner = new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterationsInModelPlanner, discountProvider);
        planner.toggleReachabiltiyTerminalStatePruning(true);
        ValueFunction knownValueFunction = groundedTask.valueFunction;
        if (knownValueFunction != null) {
            planner.setValueFunctionInitialization(knownValueFunction);
        }
        Policy policy = planner.planFromState(abstractInitialState);
        Episode ep = PolicyUtils.rollout(policy, abstractInitialState, model, maxSteps);
        OOState last = (OOState) ep.stateSequence.get(ep.stateSequence.size() - 1);
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
     *
     * @param task     the grounded task to solve
     * @param baseEnv  a environment defined by the base domain and at the current base state
     * @param maxSteps the max number of primitive actions that can be taken
     * @return whether the task was completed
     */
    protected boolean[] solveTask(GroundedTask parent, GroundedTask task, Environment baseEnv, int maxSteps) {

        tabLevel += "\t";

        State currentStateGrounded = e.stateSequence.get(e.stateSequence.size() - 1);
        State currentStateAbstract = task.mapState(currentStateGrounded);
        State pastStateAbstract = currentStateAbstract;

        int actionCount = 0;

        if (task.isPrimitive()) {
            EnvironmentOutcome result;
            Action maskedAction = task.getAction();
            //somewhat generalized unmasking:
            //copy the action, and unmask the copy, execute the unmasked action
            //this allows the task to always store the masked version for model/planning purposes
            Action action;
            if (parent.isMasked() && maskedAction instanceof ObjectParameterizedAction) {
                action = maskedAction.copy();
                //for now, reliant on parent of masked task to be unmasked. This may not be a safe assumption
                //there may be a need to traverse arbitrarily far up the task hierarchy to find an unmasked ancestor
                //in order to recover the true parameters.
                String trueParameters = ((ObjectParameterizedAction) parent.getAction()).getObjectParameters()[0];
                ((ObjectParameterizedAction) action).getObjectParameters()[0] = trueParameters;
            } else {
                // action was not masked
                action = maskedAction;
            }
            result = baseEnv.executeAction(action);

            SimulatedEnvironment simEnv = (SimulatedEnvironment) ((EnvironmentServer) baseEnv).getEnvironmentDelegate();
            simEnv.setAllowActionFromTerminalStates(true);

            e.transition(result);
            currentStateAbstract = task.mapState(result.op);
            result.o = pastStateAbstract;
            result.a = action;
            result.op = currentStateAbstract;
            String[] params = getParams(task);
            result.r = task.getReward(pastStateAbstract, action, currentStateAbstract, params);
            steps++;
            boolean taskComplete = task.isComplete(currentStateAbstract);
            boolean taskFailed = task.isFailure(currentStateAbstract);
            boolean parentShouldUpdate = true;
            boolean[] results = new boolean[]{taskComplete, taskFailed, parentShouldUpdate};
            return results;
        }

        List<String> subtasksExecuted = new ArrayList<>();
        boolean allChildrenAtOrBeyondThreshold = true;

        while (
            // while task still valid
                !(task.isFailure(currentStateAbstract) || task.isComplete(currentStateAbstract))
            // and still have steps it can take
            && (steps < maxSteps || maxSteps == -1)
            // and it hasn't solved the root goal, keep planning
            && !(groundedRoot.isComplete(groundedRoot.mapState(currentStateGrounded)))
                ) {

            actionCount++;

            State pastStateGrounded = currentStateGrounded;
            pastStateAbstract = currentStateAbstract;

            Action action = nextAction(task, currentStateAbstract);
            GroundedTask childTask = nextSubtask(task, action, currentStateAbstract);

//            System.out.println(tabLevel + ">>>>> " + task.toString() + " >>>>> " + action);

            int stepsBefore = steps;
            boolean[] results = solveTask(task, childTask, baseEnv, maxSteps);
            boolean childComplete = results[0];
            boolean childFailed = results[1];
            boolean shouldUpdateModel = results[2];

            int stepsAfter = steps;
            int stepsTaken = stepsAfter - stepsBefore;
            if (stepsTaken == 0) {
                System.err.println("took a 0 step action " + action + " for " + task);
            }

            tabLevel = tabLevel.substring(0, tabLevel.length() - 1);

            currentStateGrounded = e.stateSequence.get(e.stateSequence.size() - 1);
            currentStateAbstract = task.mapState(currentStateGrounded);

            StringBuilder resultString = new StringBuilder(action.toString());
            resultString.append(childComplete ? "+" : "-");
            if (shouldUpdateModel) {
                boolean atOrBeyondThreshold = updateModel(task, pastStateGrounded, pastStateAbstract, action, currentStateGrounded, currentStateAbstract, stepsTaken);
                resultString.append(atOrBeyondThreshold ? "+" : "-");
                if (!atOrBeyondThreshold) {
                    allChildrenAtOrBeyondThreshold = false;
                }
            } else {
                resultString.append("-");
            }
            subtasksExecuted.add(resultString.toString());
        }

        if (task.toString().contains("solve")) {
            System.out.println(subtasksExecuted.size() + " " + subtasksExecuted);
        }

        boolean taskCompleted = task.isComplete(currentStateAbstract);
        boolean taskFailed = task.isFailure(currentStateAbstract);
        boolean parentShouldUpdateModel = taskCompleted || taskFailed || actionCount == 0;
        parentShouldUpdateModel = parentShouldUpdateModel && allChildrenAtOrBeyondThreshold;
        boolean[] results = new boolean[]{taskCompleted, taskFailed, parentShouldUpdateModel};
        return results;
    }

    protected boolean updateModel(GroundedTask task, State state, State abstractState, Action action, State statePrime, State abstractStatePrime, int stepsTaken) {
        String[] params = getParams(task);
        double taskReward = getTaskReward(task, abstractState, action, abstractStatePrime, params);
        PALMModel model = getModel(task);
        EnvironmentOutcome result = new EnvironmentOutcome(abstractState, action, abstractStatePrime, taskReward, false);
        boolean atOrBeyondThreshold = model.updateModel(result, stepsTaken, params);
        return atOrBeyondThreshold;
    }

    protected double getTaskReward(GroundedTask task, State abstractState, Action action, State abstractStatePrime, String[] params) {
        // reward rollup scheme
//            double sumChildRewards = e.rewardSequence.subList(stepsBefore, stepsAfter).stream().mapToDouble(Double::doubleValue).sum();
//            double discount = 1.0;// getModel(task).getDiscountProvider().yield(pastState, a, currentState);
//            double discountOverTime = Math.pow(discount, stepsTaken);
//            double discountedReward = sumChildRewards * discountOverTime;
//            System.out.println(sumChildRewards + " " + discount + " " + discountOverTime + " " + discountedReward);
//            double taskReward = discountedReward + task.getReward(pastStateAbstract, a, currentStateAbstract, params);
//            double taskReward = discountedReward;
        double pseudoReward = task.getReward(abstractState, action, abstractStatePrime, params);
//            double taskReward = pseudoReward <= PSEUDOREWARD_ON_FAIL || pseudoReward >= PSEUDOREWARD_ON_GOAL ? pseudoReward : discountedReward;
        //task.getReward(pastState, a, currentState);
        return pseudoReward;
    }

    protected void addChildrenToMap(GroundedTask gt, State s) {
        String taskName = gt.toString();
        if (!taskNames.containsKey(taskName)) {
            taskNames.put(taskName, gt);
            System.out.println("Adding taskName: " + taskName);
        }
        List<GroundedTask> children = gt.getGroundedChildTasks(s);
        for (GroundedTask child : children) {
            String childName = child.toString();
            if (!taskNames.containsKey(childName)) {
                taskNames.put(childName, child);
                System.out.println("Adding child taskName: " + childName);
            }
        }
    }

    protected GroundedTask nextSubtask(GroundedTask task, Action action, State currentStateAbstract) {
        String actionName = StringFormat.parameterizedActionName(action);
        addChildrenToMap(task, currentStateAbstract);
        GroundedTask subtask = this.taskNames.get(actionName);
        return subtask;
    }

    private ConstantValueFunction initializer = new ConstantValueFunction(0.0);

    /**
     * plan over the given task's model and pick the best action to do next favoring unmodeled actions
     *
     * @param task the current task
     * @param s    the current state
     * @return the best action to take
     */
    protected Action nextAction(GroundedTask task, State s) {
        PALMModel model = getModel(task);
        String[] params = getParams(task);
        OOSADomain domain = task.getDomain(model, params);
        // must use discountProvider instead of gamma
        DiscountProvider discountProvider = model.getDiscountProvider();
        SolverConfig solverConfig = ((NonprimitiveTask)task.getTask()).getSolverConfig();
        Policy policy;
        if(solverConfig.getType().equals("ValueIterationMultiStep")){
            solverConfig.setDomain(domain);
            solverConfig.setDiscountProvider(discountProvider);
            solverConfig.setHashingFactory(hashingFactory);
            solverConfig.setMaxDelta(maxDelta);
            solverConfig.setMaxIterations(maxIterationsInModelPlanner);
        } else if(solverConfig.getType().equals("FittedVI")){
            solverConfig.setDomain(domain);
            solverConfig.setMaxDelta(maxDelta);
            solverConfig.setMaxIterations(maxIterationsInModelPlanner);
            ((FittedVIConfig)solverConfig).setGamma(discountProvider.getGamma());
        } else if(solverConfig.getType().equals("SarsaLambda")){
            solverConfig.setDomain(domain);
            ((SarsaLambdaConfig)solverConfig).setGamma(discountProvider.getGamma());
        }

        ValueFunction knownValueFunction = task.valueFunction;
        Planner planner = solverConfig.generateSolver(knownValueFunction);

        policy = planner.planFromState(s);

        Action action = policy.action(s);
        boolean debug = false;
        if (debug) {
            try {
//				if (task.toString().contains("solve")) {
                System.out.println("*****\n" + tabLevel + "    Task:    " + task.toString() + " " + s.toString());
                Episode e = PolicyUtils.rollout(policy, s, model, 100);
                OOState last = (OOState) e.stateSequence.get(e.stateSequence.size() - 1);
                if (last.numObjects() > 0) {
                    System.out.println(tabLevel + "    Rollout: " + e.actionSequence);
                    System.out.println(tabLevel + "    Chose:   " + action);
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
        ((DynamicProgrammingMultiStep)planner).setValueFunctionInitialization(initializer);
        task.valueFunction = (ValueFunction) planner;


        // clear the model parameters (sanity check)
        model.setParams(null);
        return action;
    }

    protected PALMModel getModel(GroundedTask t) {
        String modelName;
        //if we are using model sharing, we want to collapse all masked parameters
        //but retain the unmasked parameters
        if(t.isMasked() && useModelSharing){
            StringBuilder mName = new StringBuilder(t.getAction().actionName());
            String[] params = getParams(t);
            String[] maskedParamTypes = t.getMaskedParameters();
            String[] actionParamTypes = getParamClasses(t);
            for(int i = 0; i < actionParamTypes.length; i++){
                //if a parameter is of a type which is masked, don't include it
                if (!Arrays.asList(maskedParamTypes).contains(actionParamTypes[i])){
                    mName.append(" ");
                    mName.append(params[i]);
                }
            }
            modelName = mName.toString();
        //if we aren't using model sharing, we want to retain all parameters, even potentially masked ones
        } else modelName = t.toString();
        PALMModel model = models.get(modelName);
        if (model == null) {
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

    protected String[] getParamClasses(GroundedTask task){
        Action taskAction = task.getAction();
        String[] params = null;
        if (taskAction instanceof ObjectParameterizedAction) {
            params = ((ObjectParameterizedActionType)task.getTask().getActionType()).getParameterClasses();
        } else if (taskAction instanceof IntegerParameterizedAction) {
            //TODO (will be some number of Integers based on task)
        }
        return params;
    }



}

