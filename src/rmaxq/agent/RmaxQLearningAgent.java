package rmaxq.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import config.ExperimentConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import state.hashing.cached.CachedHashableStateFactory;
import utilities.ValueIteration;

import java.util.*;

public class RmaxQLearningAgent implements LearningAgent {

    private static final double DEFAULT_MAX_DELTA_IN_POLICY = 0.0001;
    private static final double DEFAULT_MAX_DELTA_IN_MODEL = 0.0001;
    private static final int DEFAULT_MAX_ITERATIONS_IN_MODEL = 10000;

    private HashSet<HashableState> allGroundStates;

    private double maxDeltaInPolicy = DEFAULT_MAX_DELTA_IN_POLICY;
    private double maxDeltaInModel = DEFAULT_MAX_DELTA_IN_MODEL;
    private int maxIterationsInModel = DEFAULT_MAX_ITERATIONS_IN_MODEL;
    private int threshold;
    private Task rootSolve;
    private HashableStateFactory hashingFactory;
    private double gamma;
    private double vMax;
    private Environment env;
    private int numberPrimitivesExecuted;
    private LinkedHashMap<GroundedTask, RMAXQTaskData> taskDataMap;
    private LinkedHashSet<RMAXQStateData> allStateData;

    public RmaxQLearningAgent(Task rootSolve, HashableStateFactory hsf, double gamma, int threshold, double vMax, double maxDeltaInPolicy, double maxDeltaInModel, int maxIterationsInModel) {
        this.rootSolve = rootSolve;
        this.hashingFactory = hsf;
        this.gamma = gamma;
        this.threshold = threshold;
        this.vMax = vMax;
        this.maxDeltaInPolicy = maxDeltaInPolicy;
        this.maxDeltaInModel = maxDeltaInModel;
        this.maxIterationsInModel = maxIterationsInModel;
        this.allGroundStates = new LinkedHashSet<>();
        this.allStateData = new LinkedHashSet<>();
        this.taskDataMap = new LinkedHashMap<>();
    }

    public RmaxQLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
        this(root, hsf, config.gamma, config.rmax.threshold, config.rmax.vmax, config.rmax.max_delta, config.rmax.max_delta_rmaxq, config.rmax.max_iterations_in_model);
    }

    @Override
    public Episode runLearningEpisode(Environment env) {
        return runLearningEpisode(env, -1);
    }

    private int debug = 0;
    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        this.env = env;
        debug += 1;
        if (debug >= 100) {
            System.out.println("debug");
        }
        State initialState = env.currentObservation();
        Episode e = new Episode(initialState);
        numberPrimitivesExecuted = 0;

        HashableState hs = hashingFactory.hashState(env.currentObservation());

        GroundedTask groundedRoot = rootSolve.getAllGroundedTasks(initialState).get(0);
        RMAXQStateData taskStatePair = getStateData(groundedRoot, hs);
        e = doRmaxq(taskStatePair, e, maxSteps);

        //to see the number of actions
        System.out.println("Number of actions in episode: " + e.numActions());


        return e;
    }

//    private void printDebug() {
////        for (GroundedTask task : this.taskDataMap.keySet()) {
////            System.out.println(task);
////            RMAXQTaskData taskData = getTaskData(task);
////            for (HashableState hs : allGroundStates) {
////                RMAXQStateData stateData = getStateData(task, hs);
//        for (RMAXQStateData stateData : allStateData) {
//            HashableState hs = stateData.getHs();
//            System.out.println(hs.s());
//            Object rewardBySteps = stateData.getStoredRewardBySteps();
//            Object stateActionCount = stateData.getStateActionCount();
//            Object storedExpectedTransitionProbability = stateData.getStoredExpectedTransitionProbability();
//            Object storedTransitionsBySteps = stateData.getStoredTransitionsBySteps();
//            Object storedExpectedReward = stateData.getStoredExpectedReward();
//            Object storedQValues = stateData.getStoredQValues();
//            Object totalReward = stateData.getTotalReward();
//            Object cachedStateMapping = stateData.getCachedStateMapping();
//            System.out.println("debug");
//        }
//    }

    private String tabLevel = "";

    protected Episode doRmaxq(RMAXQStateData taskStatePair, Episode e, int maxSteps) {

        if (taskStatePair.getTask().isPrimitive()) {
            e = executePrimitive(e, (PrimitiveData) taskStatePair);
            return e;
        } else {
            AbstractData currentTaskStatePair = (AbstractData) taskStatePair;
            HashableState currentState = currentTaskStatePair.getHs();
            do {

                System.out.println(tabLevel+"doing " + currentTaskStatePair.toString());
                computeModel(currentTaskStatePair);
                computePolicy(currentTaskStatePair);

                GroundedTask childTask = currentTaskStatePair.selectNextAction();
                System.out.println(tabLevel+"selected " + childTask);

                int stepsBefore = numberPrimitivesExecuted;
                RMAXQStateData childTaskStatePair = getStateData(childTask, currentState);
                tabLevel += "\t";
                e = doRmaxq(childTaskStatePair, e, maxSteps);
                tabLevel = tabLevel.substring(0,tabLevel.length()-1);

                int stepsAfter = numberPrimitivesExecuted;
                int k = stepsAfter - stepsBefore;
//                updateNumberOfSteps(childTask, k);
                System.out.println(tabLevel+"took " + k + " steps");

                State s = e.stateSequence.get(e.stateSequence.size() - 1);
                currentState = hashingFactory.hashState(s);
                currentTaskStatePair = (AbstractData) getStateData(currentTaskStatePair.getTask(), currentState);
                addToEnvelope(currentTaskStatePair);
            } while (
                    !isTerminal(currentTaskStatePair)
                 && (numberPrimitivesExecuted < maxSteps || maxSteps == -1))
            ;
            return e;
        }
    }

    public RMAXQTaskData getTaskData(GroundedTask task) {
        RMAXQTaskData taskData = taskDataMap.computeIfAbsent(task, t ->
        {
            if (task.isPrimitive()) {
                return new RMAXQTaskData(this, task, gamma);
            } else {
                return new RMAXQTaskData(this, task, gamma);
            }
        });
        return taskData;
    }

    private RMAXQStateData getStateData(GroundedTask task, HashableState hs) {
        RMAXQTaskData taskData = getTaskData(task);
        RMAXQStateData stateData = taskData.getStateData(hs);
        if (!allStateData.contains(stateData)) { allStateData.add(stateData); }
        return stateData;
    }

    private void addToEnvelope(RMAXQStateData taskStatePair) {
        taskStatePair.getTaskData().addToEnvelope(taskStatePair.getHs());
    }

    private Set<HashableState> getEnvelope(GroundedTask task) {
        return getTaskData(task).getEnvelope();
    }

    public void computePolicy(RMAXQStateData taskStatePair) {

//        if(taskStatePair.getTaskData().isComputedPolicy()) {
//            return;
//        }
//
//        handleTimesteps(taskStatePair.getTask());

        prepareEnvelope((AbstractData)taskStatePair);

        Set<HashableState> envelope = getEnvelope(taskStatePair.getTask());
        boolean converged = false;
        int attempts = maxIterationsInModel;
//        while (!converged && attempts > 0) {
//            converged = doValueIteration(taskStatePair.getTask(), envelope);
//            attempts -= 1;
//        }
//        if (attempts < 1) {
//            System.err.println("Warning: ValueIteration exhausted attempts to converge");
//        }
    }

    private Episode executePrimitive(Episode e, PrimitiveData taskStatePair) {
        Action a = taskStatePair.getTask().getAction();
        EnvironmentOutcome outcome = env.executeAction(a);
        e.transition(outcome);
        State sPrime = outcome.op;
        HashableState hsPrime = hashingFactory.hashState(sPrime);
        allGroundStates.add(taskStatePair.getHs());
        allGroundStates.add(hsPrime);
        double newReward = outcome.r;

        //r(s,a,s') += r
        taskStatePair.updateTotalReward(hsPrime, newReward);

        //n(s,a) ++
        taskStatePair.incrementStateActionCount(1);

        //n(s,a,s')++
        taskStatePair.incrementTotalTransitionCount(hsPrime, 1);

        numberPrimitivesExecuted++;

        // clear all policies
//        clearAllPolicies();

        return e;
    }

//    public void clearAllPolicies() {
//        for (GroundedTask task : taskDataMap.keySet()) {
//            RMAXQTaskData taskData = taskDataMap.get(task);
//            taskData.setComputedPolicy(false);
//        }
//    }

    /**
     * calculates and stores the possible states that can be reached from the current state
     *
     */
    private void prepareEnvelope(AbstractData taskStatePair) {
        GroundedTask task = taskStatePair.getTask();
        HashableState hs = taskStatePair.getHs();
        Set<HashableState> envelope = getEnvelope(task);
        if (!envelope.contains(hs)) {
            envelope.add(hs);
            List<GroundedTask> childTasks = task.getGroundedChildTasks(taskStatePair.getMappedState());
            for (GroundedTask childTask : childTasks) {

                RMAXQStateData childTaskStatePair = getStateData(childTask, hs);
                computeModel(childTaskStatePair);

                Set<HashableState> hsPrimes = allGroundStates;
                for (HashableState hsPrime : hsPrimes) {
                    double transitionProbability = childTaskStatePair.getP(hsPrime);
                    if (transitionProbability > 0) {
                        AbstractData next = (AbstractData) getStateData(task, hsPrime);
                        prepareEnvelope(next);
                    }
                }
            }
        }
    }

    private void computeModel(RMAXQStateData childTaskStatePair) {
        GroundedTask childTask = childTaskStatePair.getTask();
        if (childTask.isPrimitive()) {
            computeModelPrimitive((PrimitiveData) childTaskStatePair);
        } else {
            computeModelAbstract((AbstractData) childTaskStatePair);
        }
    }

    private void computeModelPrimitive(PrimitiveData childTaskStatePair) {
        System.out.println("Skipping computeModelPrimitive, not needed?");
    }

    private void computeModelAbstract(AbstractData childTaskStatePair) {

        computePolicy(childTaskStatePair);

        GroundedTask childTask = childTaskStatePair.getTask();

        boolean converged = false;
        int attempts = maxIterationsInModel;
        double oldDelta = Double.NEGATIVE_INFINITY;
        while (!converged && attempts > 0) {
            double maxDelta = doDynamicProgramming(childTask);
            if (oldDelta == maxDelta || maxDelta < maxDeltaInModel) {
                converged = true;
            }
            oldDelta = maxDelta;
            attempts -= 1;
        }
        if (attempts < 1) {
            System.err.println("Warning: exhausted attempts in DynamicProgramming, did not converge");
            System.exit(-1);
        }
    }

    private boolean isTerminal(RMAXQStateData taskStatePair) {
        State s = taskStatePair.getMappedState();
        GroundedTask task = taskStatePair.getTask();
        Boolean terminal = task.isComplete(s) || task.isFailure(s);
        return terminal;
    }

    private double doDynamicProgramming(GroundedTask task) {
        Set<HashableState> taskEnvelope = getEnvelope(task);
        double maxDelta = 0.0;
        for(HashableState hsPrime : taskEnvelope) {
            // update rewards
            AbstractData taskHsPrimePair = (AbstractData) getStateData(task, hsPrime);
            double deltaR = setR_eq4(taskHsPrimePair);
            if (deltaR > maxDelta) {
                maxDelta = deltaR;
            }
            for (HashableState hsX : allGroundStates) {
                if (!isTerminal(getStateData(task,hsX))) {
                    continue;
                }
                // update transitions
                double deltaP = setP_eq5(taskHsPrimePair, hsX);
                if (deltaP > maxDelta) {
                    maxDelta = deltaP;
                }
            }
        }
        return maxDelta;
    }

    private double setR_eq4(AbstractData taskStatePair) {
        return 0.0;
    }

    private double setP_eq5(AbstractData taskStatePair, HashableState hsX) {
        return 0.0;
    }

    public int getThreshold() {
        return threshold;
    }

    public double getVMax() {
        return vMax;
    }
}