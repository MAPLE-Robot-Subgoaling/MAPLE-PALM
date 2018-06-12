package rmaxq.agent;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.GroundedTask;
import state.hashing.simple.CachedHashableStateFactory;

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
    private GroundedTask rootSolve;
    private HashableStateFactory hashingFactory;
    private double gamma;
    private double discountPrimitive;
    private double vMax;
    private Environment env;
    private State initialState;
    //	private List<HashableState> reachableStates = new ArrayList<HashableState>();
    private long actualTimeElapsed = 0;
    private int numberPrimitivesExecuted;
    private HashMap<GroundedTask, RMAXQTaskData> taskDataMap;

    public RmaxQLearningAgent(GroundedTask rootSolve, HashableStateFactory hs, State initState, double vMax, double gamma, int threshold, double maxDeltaInPolicy, double maxDeltaInModel, int maxIterationsInModel) {
        this.rootSolve = rootSolve;
        this.hashingFactory = hs;
        this.initialState = initState;
        this.vMax = vMax;
        this.gamma = gamma;
        this.discountPrimitive = Math.pow(gamma, 1);
        this.threshold = threshold;
        this.maxDeltaInPolicy = maxDeltaInPolicy;
        this.maxDeltaInModel = maxDeltaInModel;
        this.maxIterationsInModel = maxIterationsInModel;
        this.allGroundStates = new HashSet<>();
        this.taskDataMap = new HashMap<>();
    }

    public long getActualTimeElapsed() {
        return actualTimeElapsed;
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
        RMAXQStateData taskStatePair = getStateData(rootSolve, hs);
        e = R_MaxQ(taskStatePair, e, maxSteps);

        //to see the number of actions
        System.out.println("Number of actions in episode: " + e.numActions());

        //for a chart of runtime
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Estimated RMAXQ episode nano time: " + estimatedTime);

        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
        System.out.println("Clock time elapsed: " + actualTimeElapsed);

        return e;
    }

    private String tabLevel = "";

    protected Episode R_MaxQ(RMAXQStateData taskStatePair, Episode e, int maxSteps) {

        RMAXQStateData currentTaskStatePair = taskStatePair;
        HashableState currentState = currentTaskStatePair.getHs();

        if (taskStatePair.getTask().isPrimitive()) {
            e = executePrimitive(e, taskStatePair);
            return e;
        } else {
            do {

                System.out.println(tabLevel+"doing " + currentTaskStatePair.toString());
                computeModel(null, currentTaskStatePair);
                computePolicy(currentTaskStatePair);

                GroundedTask childTask = currentTaskStatePair.getStoredPolicyAction();
                System.out.println(tabLevel+"selected " + childTask);

                int stepsBefore = numberPrimitivesExecuted;
                RMAXQStateData childTaskStatePair = getStateData(childTask, currentState);
                tabLevel += "\t";
                e = R_MaxQ(childTaskStatePair, e, maxSteps);
                tabLevel = tabLevel.substring(0,tabLevel.length()-1);

                int stepsAfter = numberPrimitivesExecuted;
                int k = stepsAfter - stepsBefore;
                updateNumberOfSteps(childTask, k);
                System.out.println(tabLevel+"took " + k + " steps");

                State s = e.stateSequence.get(e.stateSequence.size() - 1);
                currentState = hashingFactory.hashState(s);
                currentTaskStatePair = getStateData(currentTaskStatePair.getTask(), currentState);
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
        }
        if (attempts < 1) {
            System.err.println("Warning: ValueIteration exhausted attempts to converge");
        }
        taskStatePair.setStoredPolicy();
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

    private Episode executePrimitive(Episode e, RMAXQStateData taskStatePair) {
        Action a = taskStatePair.getTask().getAction();
        EnvironmentOutcome outcome = env.executeAction(a);
        e.transition(outcome);
        State sPrime = outcome.op;
        HashableState hsPrime = hashingFactory.hashState(sPrime);
        allGroundStates.add(taskStatePair.getHs());
        allGroundStates.add(hsPrime);
        double newReward = outcome.r;

        //r(s,a,s') += r
        updateTotalReward(taskStatePair, hsPrime, newReward);

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
            List<GroundedTask> childTasks = task.getGroundedChildTasks(taskStatePair.getMappedState());
            for (GroundedTask childTask : childTasks) {

                RMAXQStateData childTaskStatePair = getStateData(childTask, hs);
                computeModel(task, childTaskStatePair);

                Set<HashableState> hsPrimes = allGroundStates;
                for (HashableState hsPrime : hsPrimes) {
                    double transitionProbability = childTaskStatePair.getStoredExpectedTransitionProbabilityForState(hsPrime);
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
            for (HashableState hsPrime : allGroundStates) {
                setTransitionProbability_eq7(taskStatePair, hsPrime);
                setReward_eq6(taskStatePair, hsPrime);
            }
        }
    }

    // R^a(s,s') <- r(s,a,s') / n(s,a,s')
    // only primitives
    private void setReward_eq6(RMAXQStateData taskStatePair, HashableState hsPrime) {
//        int stateActionCount = taskStatePair.getStateActionCount();
        Map<HashableState,Integer> transitions = taskStatePair.getTotalTransitionCount();
        int stateActionSPrimeCount = transitions.containsKey(hsPrime) ? transitions.get(hsPrime) : 0;
        if (stateActionSPrimeCount == 0) {
            return;
        }
        double totalReward =  taskStatePair.getTotalReward(hsPrime);
        double approximateReward = totalReward / (1.0 * stateActionSPrimeCount);
        int k = 1;
        taskStatePair.setStoredRewardBySteps(hsPrime, approximateReward, k);
    }

    // P^a(s,sPrime) <- n(s,a,sPrime) / n(s,a)
    // only primitives
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

    private boolean isTerminal(RMAXQStateData taskStatePair) {
        State s = taskStatePair.getMappedState();
        GroundedTask task = taskStatePair.getTask();
        Boolean terminal = task.isComplete(s) || task.isFailure(s);// || rootSolve.isComplete(s);
        return terminal;
    }

//    private List<HashableState> getKnownTerminalStates(GroundedTask task) {
//        Set<HashableState> taskEnvelope = getEnvelope(task);
//        List<HashableState> taskTerminalStates = new ArrayList<>();
//        for (HashableState state : taskEnvelope) {
//            RMAXQStateData taskStatePair = getStateData(task, state);
//            if (isTerminal(taskStatePair)) {
//                taskTerminalStates.add(state);
//            }
//        }
//        return taskTerminalStates;
//    }

    private double doDynamicProgramming(GroundedTask task) {
        Set<HashableState> taskEnvelope = getEnvelope(task);
//        List<HashableState> taskTerminalStates = getKnownTerminalStates(task);
        double maxDelta = 0.0;
        for(HashableState hsPrime : taskEnvelope) {
            // update rewards
            RMAXQStateData taskHsPrimePair = getStateData(task, hsPrime);
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

    // compute the expected reward
    // non-primitive tasks
    private double setR_eq4(RMAXQStateData taskStatePair) {
        return 0.0;
    }

    private double setP_eq5(RMAXQStateData taskStatePair, HashableState hsX) {

        double oldP = taskStatePair.getStoredExpectedTransitionProbabilityForState(hsX);
        double newP = 0.0;

        GroundedTask childTask = taskStatePair.getStoredPolicyAction();
        Set<Integer> possibleNumbersOfSteps = taskStatePair.getTaskData().getPossibleK();
        for (Integer k : possibleNumbersOfSteps) {

            // get the action (child / subtask) that would be selected by policy
            RMAXQStateData childTaskStatePair = getStateData(childTask, taskStatePair.getHs());
            double childTerminalTransitionProbability = childTaskStatePair.getStoredTransitionProbabilityForSteps(hsX, k);

            Set<HashableState> hsPrimes = new HashSet<>(taskStatePair.getHSPrimes());
            double expectedTransitionProbability = 0.0;
            for (HashableState hsPrime : hsPrimes) {
                RMAXQStateData taskHsPrimePair = getStateData(taskStatePair.getTask(), hsPrime);
                if (isTerminal(taskHsPrimePair)) {
                    continue;
                }
                RMAXQStateData childTaskPair = getStateData(childTask, taskStatePair.getHs());
                double childTransitionProbability = childTaskPair.getStoredTransitionProbabilityForSteps(hsPrime, k);
                double parentTerminalTransitionProbability = taskHsPrimePair.getStoredTransitionProbabilityForSteps(hsX, k);
                expectedTransitionProbability += childTransitionProbability * parentTerminalTransitionProbability;
            }

            double probGivenK = childTerminalTransitionProbability + expectedTransitionProbability;
            double discount = Math.pow(gamma, k);
            double discountedProb = discount * probGivenK;
            double approxProb = discountedProb;
            taskStatePair.setStoredTransitionsBySteps(hsX, approxProb, k);
            newP += approxProb;
        }

        taskStatePair.setStoredExpectedTransitionProbabilityForState(hsX, newP);
//        System.out.println(taskStatePair.toString() + ", old is: " + oldP + ", new is: " + newP);
//        if (oldP == 0.5904900000000002) {
//            System.out.println("debug here");
//        }
        double delta = Math.abs(newP - oldP);
        return delta;
    }

    private void updateTotalReward(RMAXQStateData primitiveActionState, HashableState hsPrime, double reward) {
        double totalReward = primitiveActionState.getTotalReward(hsPrime);
        totalReward = totalReward + reward;
        primitiveActionState.setTotalReward(hsPrime, totalReward);
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
        if (taskEnvelope.size() < 1) { System.err.println("Warning: empty taskEnvelope"); return true; }
        double maxDelta = 0.0;
        for(HashableState hsPrime : taskEnvelope){
            RMAXQStateData taskHsPrimePair = getStateData(task, hsPrime);
            List<GroundedTask> childTasks = task.getGroundedChildTasks(taskHsPrimePair.getMappedState());
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

//        double newQ = 0.0;
//        if (isTerminal(taskStatePair)) { return newQ; }
//
//        GroundedTask task = taskStatePair.getTask();
//        HashableState hs = taskStatePair.getHs();
//        double oldQ = taskStatePair.getQValue(childTask);
//        RMAXQStateData childTaskStatePair = getStateData(childTask, hs);
//        for (HashableState hsPrime : childTaskStatePair.getHSPrimes()) {
//            double childTransitionProbability = computeExpectedProbability(childTaskStatePair, hsPrime);
//            if (childTransitionProbability == 0.0) { continue; }
//            double childReward = childTaskStatePair.getStoredReward(hsPrime);
//            RMAXQStateData taskHsPrimePair = getStateData(task, hsPrime);
//            double parentValueAtStatePrime = taskHsPrimePair.getStoredValue();
//            if (isTerminal(taskHsPrimePair)) {
//                double probability = computeExpectedProbability(taskStatePair, hsPrime);
//                if (probability != 0.0) {
//                    State abstractState = taskStatePair.getMappedState();
//                    HashableState hashedAbstractState = cachingHSF.hashState(abstractState);
//                    State abstractStatePrime = taskHsPrimePair.getMappedState();
//                    double pseudoReward = cachedGoalRewards.computeIfAbsent(task, i -> new HashMap<>()).computeIfAbsent(hashedAbstractState, i ->
//                            task.getReward(abstractState, task.getAction(), abstractStatePrime)
//                    );
//                    double probScaledReward = probability * pseudoReward;
//                    parentValueAtStatePrime = probScaledReward;
//                }
//            }
//
//            double innerTerm = childReward + parentValueAtStatePrime;
//            double probScaledReward = childTransitionProbability * innerTerm;
//            newQ += probScaledReward;
//            System.out.println("***");
//            System.out.println(taskStatePair + " " + childTask);
//            System.out.println(probScaledReward + " <- (" + childTransitionProbability + " * " + innerTerm +")");
//            if (probScaledReward > 1.0 || newQ > 1.0 || probScaledReward < -1.0 || newQ < -1.0) {
//                System.err.println("error: invalid reward/Q");
//            }
//        }
//
//        if (newQ > 1.0 || newQ < -1.0) {
//            System.err.println("error: invalid reward/Q");
//        }
//        taskStatePair.setQValue(childTask, newQ);
//
//        double delta = Math.abs(newQ - oldQ);
//        return delta;
        return 0.0;
    }

    private Map<GroundedTask, HashMap<HashableState,Double>> cachedGoalRewards = new HashMap<>();
    private HashableStateFactory cachingHSF = new CachedHashableStateFactory(false);
    private double setV_eq2(RMAXQStateData taskStatePair) {

        double oldV = taskStatePair.getStoredValue();

        double newV;
        if(isTerminal(taskStatePair)) {
            newV = 0.0;

        } else {
            GroundedTask task = taskStatePair.getTask();
            HashableState hs = taskStatePair.getHs();
            List<GroundedTask> childTasks = task.getGroundedChildTasks(taskStatePair.getMappedState());
            double maxQ = Double.NEGATIVE_INFINITY;
            GroundedTask maxAction = null;
            for (GroundedTask childTask : childTasks) {
                double qValue = taskStatePair.getQValue(childTask);
                if (qValue > maxQ) {
                    maxQ = qValue;
                    maxAction = childTask;
                }
            }
            newV = maxQ;
        }

        if (newV > 1.0 || newV < -1.0) {
            System.err.println("error: invalid reward/Q");
        }
        taskStatePair.setStoredValue(newV);

        double delta = Math.abs(newV - oldV);
        return delta;
    }


}