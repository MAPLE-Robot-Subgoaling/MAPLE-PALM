package edu.umbc.cs.maple.rmaxq.agent;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.state.hashing.cached.CachedHashableStateFactory;

import java.text.SimpleDateFormat;
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
    private double discountPrimitive;
    private double vMax;
    private Environment env;
    //	private List<HashableState> reachableStates = new ArrayList<HashableState>();
    private long actualTimeElapsed = 0;
    private int numberPrimitivesExecuted;
    private HashMap<GroundedTask, RMAXQTaskData> taskDataMap;

    public RmaxQLearningAgent(Task rootSolve, HashableStateFactory hsf, double gamma, int threshold, double vMax, double maxDeltaInPolicy, double maxDeltaInModel, int maxIterationsInModel) {
        this.rootSolve = rootSolve;
        this.hashingFactory = hsf;
        this.gamma = gamma;
        this.discountPrimitive = Math.pow(gamma, 1);
        this.threshold = threshold;
        this.vMax = vMax;
        this.maxDeltaInPolicy = maxDeltaInPolicy;
        this.maxDeltaInModel = maxDeltaInModel;
        this.maxIterationsInModel = maxIterationsInModel;
        this.allGroundStates = new HashSet<>();
        this.taskDataMap = new HashMap<>();
    }

    public RmaxQLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
        this(root, hsf, config.gamma, config.rmax.threshold, config.rmax.vmax, config.rmax.max_delta, config.rmax.max_delta_rmaxq, config.rmax.max_iterations_in_model);
    }

    public long getActualTimeElapsed() {
        return actualTimeElapsed;
    }

    @Override
    public Episode runLearningEpisode(Environment env) {
        return runLearningEpisode(env, -1);
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        this.env = env;
        State initialState = env.currentObservation();
        Episode e = new Episode(initialState);
        numberPrimitivesExecuted = 0;
        for (GroundedTask task : taskDataMap.keySet()) {
            taskDataMap.get(task).clearTimesteps();
        }

        //for a chart of runtime
        long startTime = System.nanoTime();

        actualTimeElapsed = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(actualTimeElapsed);
        System.out.println(sdf.format(resultdate));
        HashableState hs = hashingFactory.hashState(env.currentObservation());

        GroundedTask groundedRoot = rootSolve.getAllGroundedTasks(initialState).get(0);
        RMAXQStateData taskStatePair = getStateData(groundedRoot, hs);
        e = R_MaxQ(null, taskStatePair, e, maxSteps);

        //to see the number of actions
        System.out.println("Number of actions in episode: " + e.numActions());

        //for a chart of runtime
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Estimated RMAXQ episode nano time: " + estimatedTime);

        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
        System.out.println("Clock time elapsed: " + actualTimeElapsed);

        return e;
    }

    public static String tabLevel = "";

    protected Episode R_MaxQ(RMAXQStateData parent, RMAXQStateData taskStatePair, Episode e, int maxSteps) {

//		System.out.println(tabLevel + ">>> " + taskStatePair.getTask().getAction());

        RMAXQStateData currentTaskStatePair = taskStatePair;
        HashableState currentState = currentTaskStatePair.getHs();

        if (taskStatePair.getTask().isPrimitive()) {
            e = executePrimitive(e, parent, taskStatePair);
            return e;
        } else {
            do {

                computePolicy(currentTaskStatePair);

                GroundedTask childTask = getStoredPolicy(currentTaskStatePair);

                tabLevel += "\t";

                int stepsBefore = numberPrimitivesExecuted;
                RMAXQStateData childTaskStatePair = getStateData(childTask, currentState);
                e = R_MaxQ(taskStatePair, childTaskStatePair, e, maxSteps);

                int stepsAfter = numberPrimitivesExecuted;
                int k = stepsAfter - stepsBefore;
                updateNumberOfSteps(childTask, k);

                tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));

                State s = e.stateSequence.get(e.stateSequence.size() - 1);
                currentState = hashingFactory.hashState(s);
                currentTaskStatePair = getStateData(currentTaskStatePair.getTask(), currentState);
                addToEnvelope(currentTaskStatePair);
            } while (
                    !isTerminal(currentTaskStatePair)
                 && (numberPrimitivesExecuted < maxSteps || maxSteps == -1))
            ;
//			System.out.println(tabLevel + "<<< " + currentTaskStatePair.getTask().getAction());
            return e;
        }
    }

    public RMAXQTaskData getTaskData(GroundedTask task) {
        RMAXQTaskData taskData = taskDataMap.computeIfAbsent(task, t ->
        {
                if (task.isPrimitive()) {
                    return new RMAXQTaskData(task, vMax);
                } else {
                    return new RMAXQTaskData(task, 0.0);
                }
        });
        return taskData;
    }

    private RMAXQStateData getStateData(GroundedTask task, HashableState hs) {
        RMAXQTaskData taskData = getTaskData(task);
        RMAXQStateData stateData = taskData.getStateData(hs);
        return stateData;
    }

    private void updateNumberOfSteps(GroundedTask childTask, int k) {
        getTaskData(childTask).addPossibleK(k);
    }

    private void addToEnvelope(RMAXQStateData taskStatePair) {
        taskStatePair.getTaskData().addToEnvelope(taskStatePair.getHs());
    }

    private Set<HashableState> getEnvelope(GroundedTask task) {
        return getTaskData(task).getEnvelope();
    }

    public void computePolicy(RMAXQStateData taskStatePair) {

        if(taskStatePair.getTaskData().isComputedPolicy()) {
            return;
        }

        handleTimesteps(taskStatePair.getTask());

        prepareEnvelope(taskStatePair);

        Set<HashableState> envelope = getEnvelope(taskStatePair.getTask());
        boolean converged = false;
        int attempts = maxIterationsInModel;
        while (!converged && attempts > 0) {
            converged = doValueIteration(taskStatePair.getTask(), envelope);
            attempts -= 1;
//			System.out.println("VI: " + taskStatePair.getTask().toString() + " " + attempts);
        }
        if (attempts < 1) {
            System.err.println("Warning: ValueIteration exhausted attempts to converge");
        }
        setStoredPolicy(taskStatePair);
        taskStatePair.getTaskData().setComputedPolicy(true);
    }

    private void handleTimesteps(GroundedTask task) {

        RMAXQTaskData taskData = getTaskData(task);

        // initialize timesteps
        Integer timesteps = taskData.getTaskTimesteps();

        // initialize taskEnvelope
        Set<HashableState> taskEnvelope = taskData.getEnvelope();

        // clear task envelope if timestampForTask < total actual timesteps
        if (timesteps < numberPrimitivesExecuted) {
            taskData.setTaskTimesteps(numberPrimitivesExecuted);
            taskEnvelope.clear();
        }
    }

    private Episode executePrimitive(Episode e,RMAXQStateData parent, RMAXQStateData taskStatePair) {
        GroundedTask parentTask = parent.getTask();
        Action a = taskStatePair.getTask().getAction();
        Action unMaskedAction = a;
      if (parentTask.isMasked()) {
                unMaskedAction = a.copy();
                //for now, reliant on parent of masked task to be unmasked. This may not be a safe assumption
                //there may be a need to traverse arbitrarily far up the task hierarchy to find an unmasked ancestor
                //in order to recover the true parameters.
                String trueParameters = ((ObjectParameterizedAction)parentTask.getAction()).getObjectParameters()[0];
                ((ObjectParameterizedAction) unMaskedAction).getObjectParameters()[0] = trueParameters;
            }
        EnvironmentOutcome outcome = env.executeAction(unMaskedAction);
        e.transition(outcome);
        State sPrime = outcome.op;
        HashableState hsPrime = hashingFactory.hashState(sPrime);
        allGroundStates.add(taskStatePair.getHs());
        allGroundStates.add(hsPrime);
        double newReward = outcome.r;

        //r(s,a) += r
        updateTotalReward(taskStatePair, newReward);

        //n(s,a) ++
        incrementStateActionCount(taskStatePair);

        //n(s,a,s')++
        incrementTotalTransitionCount(taskStatePair, hsPrime);

        numberPrimitivesExecuted++;

        // clear all policies
        clearAllPolicies();

        return e;
    }

    public void clearAllPolicies() {
        for (GroundedTask task : taskDataMap.keySet()) {
            RMAXQTaskData taskData = taskDataMap.get(task);
            taskData.setComputedPolicy(false);
        }
    }

    /**
     * calculates and stores the possible states that can be reached from the current state
     *
     */
    private void prepareEnvelope(RMAXQStateData taskStatePair) {
        GroundedTask task = taskStatePair.getTask();
        HashableState hs = taskStatePair.getHs();
        Set<HashableState> envelope = getEnvelope(task);
        if (!envelope.contains(hs)) {
            envelope.add(hs);
            List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(taskStatePair));
            for (GroundedTask childTask : childTasks) {

                RMAXQStateData childTaskStatePair = getStateData(childTask, hs);
                computeModel(task, childTaskStatePair);

                Set<HashableState> hsPrimes = allGroundStates;
                for (HashableState hsPrime : hsPrimes) {
                    double transitionProbability = getStoredExpectedProbability(childTaskStatePair, hsPrime);
                    if (transitionProbability > 0) {
                        RMAXQStateData next = getStateData(task, hsPrime);
                        prepareEnvelope(next);
                    }
                }
            }
        }
    }

    private void computeModel(GroundedTask parent, RMAXQStateData childTaskStatePair) {
        GroundedTask childTask = childTaskStatePair.getTask();
        HashableState hs = childTaskStatePair.getHs();
        if (childTask.isPrimitive()) {
            computeModelPrimitive(childTaskStatePair);
        } else {

            computePolicy(childTaskStatePair);
            Set<HashableState> childTaskEnvelope = getEnvelope(childTask);
            for (HashableState oneOfAllHS : childTaskEnvelope) {
                if (hs.equals(oneOfAllHS)) {
                    continue;
                }
                RMAXQStateData childTaskOneOfAllHSPair = getStateData(childTask, oneOfAllHS);
                if (childTaskOneOfAllHSPair.getStoredPolicyAction() == null) {
                    clearAllPolicies();
                }
                computePolicy(childTaskOneOfAllHSPair);
            }

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
//				System.out.println("dynamic programming " + childTask.toString() +" "+ attempts);
            }
            if (attempts < 1) {
                System.err.println("Warning: exhausted attempts in DynamicProgramming, did not converge");
                System.exit(-1);
            }
        }
    }

    private void computeModelPrimitive(RMAXQStateData taskStatePair) {
        int stateActionCount = taskStatePair.getStateActionCount();
        if (stateActionCount >= threshold) {
            setReward_eq6(taskStatePair);
            for (HashableState hsPrime : allGroundStates) {
                setTransitionProbability_eq7(taskStatePair, hsPrime);
            }
        }
    }

    // R^a(s) <- r(s,a) / n(s,a)
    private void setReward_eq6(RMAXQStateData taskStatePair) {
        int stateActionCount = taskStatePair.getStateActionCount();
        double totalReward =  taskStatePair.getTotalReward();
        double approximateReward = totalReward / (1.0 * stateActionCount);
        taskStatePair.setStoredReward(approximateReward);
    }

    // P^a(s,sPrime) <- n(s,a,sPrime) / n(s,a)
    private void setTransitionProbability_eq7(RMAXQStateData taskStatePair, HashableState hsPrime) {
        int stateActionCount = taskStatePair.getStateActionCount();
        int countForThisTransition = taskStatePair.getTotalTransitionCount(hsPrime);
        if (countForThisTransition <= 0) {
            return; // sparse -- do not store zero probs
        }
        double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
        int k = 1;
        double probability = approximateTransitionProbability;
        taskStatePair.setStoredTransitionsBySteps(hsPrime, probability, k);
    }

    private GroundedTask getStoredPolicy(RMAXQStateData taskStatePair) {
        GroundedTask policyAction = taskStatePair.getStoredPolicyAction();
        return policyAction;
    }

    private double getStoredQ(RMAXQStateData taskStatePair, GroundedTask childTask) {
        double qValue = taskStatePair.getQValue(childTask);
        return qValue;
    }

    private double getStoredValue(RMAXQStateData taskStatePair) {
        double value = taskStatePair.getStoredValue();
        return value;
    }

    private double getStoredReward(RMAXQStateData taskStatePair) {
        double reward = taskStatePair.getStoredReward();
        return reward;
    }

    private Map<HashableState, HashMap<Integer, Double>> getStoredTransitions(GroundedTask task, HashableState hs) {
        RMAXQStateData stateData = getStateData(task, hs);
        return stateData.getStoredTransitionsBySteps();
    }

    private List<HashableState> getHSPrimes(GroundedTask task, HashableState hs) {
        return new ArrayList<>(getStoredTransitions(task, hs).keySet());
    }

    private double getStoredTransitionProbabilityForSteps(RMAXQStateData taskStatePair, HashableState hsPrime, int steps) {
        double probability = taskStatePair.getStoredTransitionProbabilityForSteps(hsPrime, steps);
        return probability;
    }

    private double getStoredExpectedProbability(RMAXQStateData taskStatePair, HashableState hsPrime) {
        double transitionProbability = 0.0;
        HashMap<HashableState,HashMap<Integer,Double>> map = taskStatePair.getStoredTransitionsBySteps();
        Map<Integer,Double> stepsToProbability = map.get(hsPrime);
        if (stepsToProbability == null || stepsToProbability.size() == 0) {
            return transitionProbability; // return 0.0, do not store it
        }

        // if we know it is primitive then we know it is one step, just return discounted multitime model step
        if (taskStatePair.getTask().isPrimitive()) return discountPrimitive;

        for (Integer kSteps : stepsToProbability.keySet()) {
            double discount = Math.pow(gamma, kSteps);
            double probability = stepsToProbability.get(kSteps);
            double product = discount * probability;
            transitionProbability += product;
        }
        return transitionProbability;
    }

    private HashMap<GroundedTask, HashMap<HashableState, State>> cachedStateMapping = new HashMap<>();
    private State getMappedState(RMAXQStateData taskStatePair) {
        GroundedTask task = taskStatePair.getTask();
        HashableState hs = taskStatePair.getHs();
        Map<HashableState, State> stateMapping = cachedStateMapping.computeIfAbsent(task, i -> new HashMap<>());
        State s = stateMapping.computeIfAbsent(hs, i -> task.mapState(hs.s()));
        return s;
//		return task.mapState(hs.s());
    }

    private boolean isTerminal(RMAXQStateData taskStatePair) {
        State s = getMappedState(taskStatePair);//task.mapState(hs.s());
        GroundedTask task = taskStatePair.getTask();
        Boolean terminal = task.isComplete(s) || task.isFailure(s);// || rootSolve.isComplete(s);
        return terminal;
    }

    private List<HashableState> getKnownTerminalStates(GroundedTask task) {
        Set<HashableState> taskEnvelope = getEnvelope(task);
        List<HashableState> taskTerminalStates = new ArrayList<>();
        for (HashableState state : taskEnvelope) {
            RMAXQStateData taskStatePair = getStateData(task, state);
            if (isTerminal(taskStatePair)) {
                taskTerminalStates.add(state);
            }
        }
        return taskTerminalStates;
    }

    private double doDynamicProgramming(GroundedTask task) {
        Set<HashableState> taskEnvelope = getEnvelope(task);
        List<HashableState> taskTerminalStates = getKnownTerminalStates(task);
        double maxDelta = 0.0;
        for(HashableState hsPrime : taskEnvelope) {
            // update rewards
            RMAXQStateData taskHsPrimePair = getStateData(task, hsPrime);
            double deltaR = setR_eq4(taskHsPrimePair);
            if (deltaR > maxDelta) {
                maxDelta = deltaR;
            }
            for (HashableState taskTerminalState : taskTerminalStates) {
                // update transitions
                double deltaP = setP_eq5(taskHsPrimePair, taskTerminalState);
                if (deltaP > maxDelta) {
                    maxDelta = deltaP;
                }
            }
        }
        return maxDelta;
    }

    private double setR_eq4(RMAXQStateData taskStatePair) {

        // get the action (child / subtask) that would be selected by policy
        GroundedTask childTask = getStoredPolicy(taskStatePair);
        HashableState hs = taskStatePair.getHs();
        RMAXQStateData childTaskStatePair = getStateData(childTask, hs);
        double childReward = getStoredReward(childTaskStatePair);

        //now compute the expected reward
        Set<HashableState> hsPrimes = new HashSet<>(taskStatePair.getStoredTransitionsBySteps().keySet());//allGroundStates;//getHSPrimes(task, hs);
        double expectedReward = 0.0;
        for (HashableState hsPrime : hsPrimes) {
            RMAXQStateData taskHsPrime = getStateData(taskStatePair.getTask(), hsPrime);
            if (isTerminal(taskHsPrime)) {
                continue;
            }
            double childTransitionProbability = getStoredExpectedProbability(childTaskStatePair, hsPrime);
            double parentReward = getStoredReward(taskHsPrime);
            expectedReward += childTransitionProbability * parentReward;
        }

        // get the old reward, store the new reward, compute the delta
        double oldRewardForTaskInState = getStoredReward(taskStatePair);
        double rewardForTaskInState = childReward + expectedReward;
        taskStatePair.setStoredReward(rewardForTaskInState);
        double delta = Math.abs(rewardForTaskInState - oldRewardForTaskInState);
        return delta;
    }

    private double setP_eq5(RMAXQStateData taskStatePair, HashableState hsX) {
        double oldP = getStoredExpectedProbability(taskStatePair, hsX);
        double newP = 0.0;

        GroundedTask childTask = getStoredPolicy(taskStatePair);
//		List<Integer> steps = stepsByTask.computeIfAbsent(task, k->new ArrayList<>());
        Set<Integer> possibleNumbersOfSteps = taskStatePair.getTaskData().getPossibleK();
        for (Integer k : possibleNumbersOfSteps) {
            // get the action (child / subtask) that would be selected by policy
            RMAXQStateData childTaskStatePair = getStateData(childTask, taskStatePair.getHs());
            double childTerminalTransitionProbability = getStoredTransitionProbabilityForSteps(childTaskStatePair, hsX, k);

            Set<HashableState> hsPrimes = new HashSet<>(taskStatePair.getStoredTransitionsBySteps().keySet());//allGroundStates;//getHSPrimes(task, hs);
            double expectedTransitionProbability = 0.0;
            for (HashableState hsPrime : hsPrimes) {
                RMAXQStateData taskHsPrimePair = getStateData(taskStatePair.getTask(), hsPrime);
                if (isTerminal(taskHsPrimePair)) {
                    continue;
                }
                RMAXQStateData childTaskPair = getStateData(childTask, taskStatePair.getHs());
                double childTransitionProbability = getStoredTransitionProbabilityForSteps(childTaskPair, hsPrime, k);
                double parentTerminalTransitionProbability = getStoredTransitionProbabilityForSteps(taskHsPrimePair, hsX, k);
                expectedTransitionProbability += childTransitionProbability * parentTerminalTransitionProbability;
            }

            double probGivenK = childTerminalTransitionProbability + expectedTransitionProbability;
            double discount = Math.pow(gamma, k);
            double discountedProb = discount * probGivenK;
            double approxProb = discountedProb;
//			double approxProb = probGivenK;
            taskStatePair.setStoredTransitionsBySteps(hsX, approxProb, k);
            newP += approxProb;
        }
        double delta = Math.abs(newP - oldP);
        return delta;
    }

    private void updateTotalReward(RMAXQStateData primitiveActionState, double reward) {
        double totalReward = primitiveActionState.getTotalReward();
        totalReward = totalReward + reward;
        primitiveActionState.setTotalReward(totalReward);
    }

    private void incrementStateActionCount(RMAXQStateData primitiveActionState) {
        int stateActionCount = primitiveActionState.getStateActionCount();
        stateActionCount = stateActionCount + 1;
        primitiveActionState.setStateActionCount(stateActionCount);
    }

    private void incrementTotalTransitionCount(RMAXQStateData primitiveActionState, HashableState hsPrime) {
        int transitionCount = primitiveActionState.getTotalTransitionCount(hsPrime);
        transitionCount = transitionCount + 1;
        primitiveActionState.setTotalTransitionCount(hsPrime, transitionCount);
    }

    private boolean doValueIteration(GroundedTask task, Set<HashableState> taskEnvelope) {
        if (taskEnvelope.size() < 1) {
            System.err.println("Warning: empty taskEnvelope");
            return true;
        }
        double maxDelta = 0.0;
        for(HashableState hsPrime : taskEnvelope){
            RMAXQStateData taskHsPrimePair = getStateData(task, hsPrime);
            List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(taskHsPrimePair));//task.mapState(hsPrime.s()));
            for(GroundedTask childTask : childTasks){
                setQ_eq1(taskHsPrimePair, childTask);
            }
            double deltaV = setV_eq2(taskHsPrimePair);
            if(deltaV > maxDelta) {
                maxDelta = deltaV;
            }
        }
        boolean converged = maxDelta < maxDeltaInPolicy;
        return converged;
    }

    private double setQ_eq1(RMAXQStateData taskStatePair, GroundedTask childTask) {

        GroundedTask task = taskStatePair.getTask();
        HashableState hs = taskStatePair.getHs();
        double oldQ = getStoredQ(taskStatePair, childTask);

        RMAXQStateData childTaskStatePair = getStateData(childTask, hs);
        double childReward = getStoredReward(childTaskStatePair);

        Map<HashableState, HashMap<Integer,Double>> childTransitions = getStoredTransitions(childTask, hs);

        double expectedValue = 0.0;
        for (HashableState hsPrime : childTransitions.keySet()) {
            double childTransitionProbability = getStoredExpectedProbability(childTaskStatePair, hsPrime);
            RMAXQStateData taskHsPrimePair = getStateData(task, hsPrime);
            double parentValueAtStatePrime = getStoredValue(taskHsPrimePair);
            expectedValue += childTransitionProbability * parentValueAtStatePrime;
//			if (childReward + expectedValue > 1.0) {
//				System.err.println("invalid value computed");
//			}
        }

        double newQ = childReward + expectedValue;

        taskStatePair.setStoredQValues(childTask, newQ);

        double delta = Math.abs(newQ - oldQ);
        return delta;
    }

    private Map<GroundedTask, HashMap<HashableState,Double>> cachedGoalRewards = new HashMap<>();
    private HashableStateFactory cachingHSF = new CachedHashableStateFactory(false);
    private double setV_eq2(RMAXQStateData taskStatePair) {

        double oldV = getStoredValue(taskStatePair);

        double newV;
//		if(!task.isPrimitive() && isTerminal(task, hs)) {
        if(isTerminal(taskStatePair)) {

//			newV = 0.0;
            // unsure which this should be, either this line
//			newV = stateData.getStoredReward();
            // or this line
//			newV = task.getReward(null, task.getAction(), getMappedState(task, hs));
            State abstractState = getMappedState(taskStatePair);
            HashableState hashedAbstractState = cachingHSF.hashState(abstractState);
            GroundedTask task = taskStatePair.getTask();
            newV = cachedGoalRewards.computeIfAbsent(task, i -> new HashMap<>()).computeIfAbsent(hashedAbstractState, i -> task.getReward(null, task.getAction(), abstractState));

        } else {
            GroundedTask task = taskStatePair.getTask();
            HashableState hs = taskStatePair.getHs();
            List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(taskStatePair));
            double maxQ = Double.NEGATIVE_INFINITY;
            GroundedTask maxAction = null;
            for (GroundedTask childTask : childTasks) {
                double qValue = getStoredQ(taskStatePair, childTask);
                if (qValue > maxQ) {
                    maxQ = qValue;
                    maxAction = childTask;
                }
            }
            newV = maxQ;
        }

        taskStatePair.setStoredValue(newV);

        double delta = Math.abs(newV - oldV);
        return delta;
    }

    private void setStoredPolicy(RMAXQStateData taskStatePair) {
        List<GroundedTask> childTasks = taskStatePair.getTask().getGroundedChildTasks(getMappedState(taskStatePair));
        double maxQ = Double.NEGATIVE_INFINITY;
        List<GroundedTask> maxChildTasks = new ArrayList<>();
        for (GroundedTask childTask : childTasks) {
            double qValue = getStoredQ(taskStatePair, childTask);
            if (qValue > maxQ) {
                maxQ = qValue;
                maxChildTasks.clear();
                maxChildTasks.add(childTask);
            } else if (qValue == maxQ) {
                maxChildTasks.add(childTask);
            }
        }
        int size = maxChildTasks.size();
        int bound = RandomFactory.getMapped(0).nextInt(size);
        // get a random child task among the equally good actions
        GroundedTask maxChildTask = maxChildTasks.get(bound);
        taskStatePair.setStoredPolicyAction(maxChildTask);
    }


}