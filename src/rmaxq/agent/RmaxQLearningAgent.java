package rmaxq.agent;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;

import java.text.SimpleDateFormat;
import java.util.*;

public class RmaxQLearningAgent implements LearningAgent {

	private static final double DEFAULT_MAX_DELTA_IN_POLICY = 0.0001;
	private static final double DEFAULT_MAX_DELTA_IN_MODEL = 0.0001;
	private static final int DEFAULT_MAX_ITERATIONS_IN_MODEL = 10000;

//	private HashMap<GroundedTask, HashMap< HashableState, HashMap<HashableState, Integer>>> kStepsForTransitionByTask;

	//R^a(s) for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, Double>> storedRewardsByTask;

	//P^a(s, s') for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, HashMap< HashableState, HashMap<Integer, Double>>>> storedTransitionsByTask;

    private Map<GroundedTask, HashMap<HashableState, Boolean>> cachedTerminalCheck = new HashMap<>();

	private HashMap<GroundedTask, HashMap< HashableState, Double>> storedValueByTask;

	private HashMap<GroundedTask, HashMap< HashableState, HashMap <GroundedTask, Double>>> storedQValuesByTask;

	private HashMap<GroundedTask, HashMap< HashableState, GroundedTask>> storedPoliciesByTask;

	//n(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Integer>> stateActionCountsByTask;

	//n(s,a,s')
	private HashMap<GroundedTask, HashMap< HashableState, HashMap<HashableState, Integer>>> totalTransitionsByTask;

	//r(s,a)
	private HashMap<GroundedTask, Map< HashableState, Double>> totalRewardsByTask;

	//envelopesByTask(a)
	private HashMap<GroundedTask, Set<HashableState>> envelopesByTask;

	private HashSet<HashableState> allGroundStates;


//	//ta
//	private HashMap<GroundedTask, List<HashableState>> terminalStatesByTask;

	private HashMap<GroundedTask, Integer> timestepsByTask;

	private double maxDeltaInPolicy = DEFAULT_MAX_DELTA_IN_POLICY;
	private double maxDeltaInModel = DEFAULT_MAX_DELTA_IN_MODEL;
	private int maxIterationsInModel = DEFAULT_MAX_ITERATIONS_IN_MODEL;
	private int threshold;
	private GroundedTask rootSolve;
	private HashableStateFactory hashingFactory;
	private double gamma;
	private double Vmax;
	private Environment env;
	private State initialState;	
//	private List<HashableState> reachableStates = new ArrayList<HashableState>();
	private long actualTimeElapsed = 0;
	private int numberPrimitivesExecuted;
    private Map<GroundedTask,Integer> stepsByTask;

    public RmaxQLearningAgent(GroundedTask rootSolve, HashableStateFactory hs, State initState, double vmax, double gamma, int threshold, double maxDeltaInPolicy, double maxDeltaInModel, int maxIterationsInModel){
		this.rootSolve = rootSolve;
		this.hashingFactory = hs;
		this.initialState = initState;
		this.Vmax = vmax;
		this.gamma = gamma;
		this.threshold = threshold;
		this.maxDeltaInPolicy = maxDeltaInPolicy;
		this.maxDeltaInModel = maxDeltaInModel;
		this.maxIterationsInModel = maxIterationsInModel;
		this.totalRewardsByTask = new HashMap<>();
		this.storedRewardsByTask = new HashMap<>();
		this.storedValueByTask = new HashMap<>();
		this.storedQValuesByTask = new HashMap<>();
		this.storedPoliciesByTask = new HashMap<>();
		this.storedTransitionsByTask = new HashMap<>();
		this.stateActionCountsByTask = new HashMap<>();
		this.envelopesByTask = new HashMap<>();
		this.totalTransitionsByTask = new HashMap<>();
//		this.terminalStatesByTask = new HashMap<>();
		this.timestepsByTask = new HashMap<>();
		this.allGroundStates = new HashSet<>();
		this.stepsByTask = new HashMap<>();
	}

	public long getActualTimeElapsed(){
		return actualTimeElapsed;
	}

	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		this.env = env;
		Episode e = new Episode(initialState);
		numberPrimitivesExecuted = 0;
		timestepsByTask.clear();

		actualTimeElapsed = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		Date resultdate = new Date(actualTimeElapsed);
		System.out.println(sdf.format(resultdate));
		HashableState hs = hashingFactory.hashState(env.currentObservation());
		e = R_MaxQ(rootSolve, hs, e, maxSteps);
		actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;

		return e;
	}

	public static String tabLevel = "";
	/**
	 * main loop of the algorithm that recurses down to primitive actions then updates counts
	 * @param task the task to solve
	 * @param hs the current state
	 * @param e current episode
	 * @param maxSteps the max number of primitive actions allowed
	 * @return the episode
	 */
	protected Episode R_MaxQ(GroundedTask task, HashableState hs, Episode e, int maxSteps){

		System.out.println(tabLevel + ">>> " + task.getAction());

		int k = 1;
		if(task.isPrimitive()){
			e = executePrimitive(e, task, hs);
			return e;
		} else {
			do {

				computePolicy(task, hs);

				GroundedTask childTask = pi(task, hs);

				tabLevel += "\t";

				int stepsBefore = numberPrimitivesExecuted;
				e = R_MaxQ(childTask, hs, e, maxSteps);
				int stepsAfter = numberPrimitivesExecuted;
                k = stepsAfter - stepsBefore;

				tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));

				State s = e.stateSequence.get(e.stateSequence.size() - 1);
				hs = hashingFactory.hashState(s);
				System.out.println(tabLevel + task.toString() + " just did " + childTask.toString() + ", " + ((OOState)hs.s()).object("Taxi")+ ", " + ((OOState)hs.s()).object("Passenger0")+ ", " + ((OOState)hs.s()).object("Passenger1"));
				envelopesByTask.get(task).add(hs);
			} while(!isTerminal(task, hs) && (numberPrimitivesExecuted < maxSteps || maxSteps == -1));
			System.out.println(tabLevel + "<<< " + task.getAction());
			updateNumberOfSteps(task, k);
			return e;
		}
	}

	private void updateNumberOfSteps(GroundedTask task, int k) {
	    int oldK = stepsByTask.computeIfAbsent(task, i -> 1);
	    stepsByTask.put(task, Math.min(oldK, k));
    }

	/**
	 * computes and updates action values for the state in the task
	 * @param task the current task
	 * @param hs current state
	 */
	public void computePolicy(GroundedTask task, HashableState hs){

		handleTimesteps(task);

		prepareEnvelope(task, hs);

		Set<HashableState> envelope = envelopesByTask.get(task);
		boolean converged = false;
		int attempts = maxIterationsInModel;
		while(!converged && attempts > 0){
			converged = doValueIteration(task, envelope);
			attempts -= 1;
		}
		if (attempts < 1) {
			System.err.println("Warning: ValueIteration exhausted attempts to converge");
		}
		setPi(task, hs);
	}

	private void handleTimesteps(GroundedTask task) {
		// initialize timesteps
		Integer timesteps = timestepsByTask.computeIfAbsent(task, k -> 0);

		// initialize taskEnvelope
		Set<HashableState> taskEnvelope = envelopesByTask.computeIfAbsent(task, k -> new HashSet<>());

		// clear task envelope if timestampForTask < total actual timesteps
		if (timesteps < numberPrimitivesExecuted) {
			timestepsByTask.put(task, numberPrimitivesExecuted);
			taskEnvelope.clear();
		}
	}

	private Episode executePrimitive(Episode e, GroundedTask task, HashableState hs) {
		Action a = task.getAction();
		EnvironmentOutcome outcome = env.executeAction(a);
		e.transition(outcome);
		State sPrime = outcome.op;
		HashableState hsPrime = hashingFactory.hashState(sPrime);
        allGroundStates.add(hs);
        allGroundStates.add(hsPrime);
		double newReward = outcome.r;

		//r(s,a) += r
		updateTotalReward(task, hs, newReward);

		//n(s,a) ++
		incrementStateActionCount(task, hs);

		//n(s,a,s')++
		incrementTotalTransitionCount(task, hs, hsPrime);

		numberPrimitivesExecuted++;
		return e;
	}

	/**
	 * calculates and stores the possible states that can be reached from the current state
	 * @param task the task
	 * @param hs the state
	 */
	private void prepareEnvelope(GroundedTask task, HashableState hs){
		Set<HashableState> envelope = envelopesByTask.get(task);
		if(!envelope.contains(hs)){
			envelope.add(hs);
			List<GroundedTask> childTasks = task.getGroundedChildTasks(task.mapState(hs.s()));
			for(GroundedTask childTask : childTasks){

				computeModel(task, childTask, hs);

                Set<HashableState> hsPrimes = allGroundStates;//getHSPrimes(task, hs);
				for (HashableState hsPrime : hsPrimes) {
					double transitionProbability = P(childTask, hs, hsPrime, false);
					if (transitionProbability > 0) {
						prepareEnvelope(task, hsPrime);
					}
				}
			}
		}
	}

	/**
	 * computes and stores totalRewardsByTask and transitionsByTask values for the task
	 * @param childTask a childTask of the current task being considered when this method was called
	 * @param hs the current state
	 */
	private void computeModel(GroundedTask parent, GroundedTask childTask, HashableState hs){
		if(childTask.isPrimitive()){
			computeModelPrimitive(childTask, hs);
		}else{

            computePolicy(childTask, hs);
            Set<HashableState> childTaskEnvelope = envelopesByTask.get(childTask);
		    for (HashableState oneOfAllHS : childTaskEnvelope) {
		        if (hs.equals(oneOfAllHS)) {
		            continue;
                }
                computePolicy(childTask, oneOfAllHS);
            }

			boolean converged = false;
			int attempts = maxIterationsInModel;
			double oldDelta = Double.NEGATIVE_INFINITY;
            while(!converged && attempts > 0){
				double maxDelta = doDynamicProgramming(childTask);
                if (oldDelta == maxDelta || maxDelta < maxDeltaInModel) {
                    converged = true;
                }
                oldDelta = maxDelta;
				attempts -= 1;
			}
			if (attempts < 1) {
				System.err.println("Warning: exhausted attempts in DynamicProgramming, did not converge");
			}
		}
	}

	private void computeModelPrimitive(GroundedTask primitiveAction, HashableState hs) {
		int stateActionCount = n(hs, primitiveAction);
		if (stateActionCount >= threshold) {
			setReward_eq6(hs, primitiveAction);
			for (HashableState hsPrime : allGroundStates) {
				setTransitionProbability_eq7(primitiveAction, hs, hsPrime);
			}
		}
	}

	// R^a(s) <- r(s,a) / n(s,a)
	private void setReward_eq6(HashableState hs, GroundedTask primitiveAction) {
		double stateActionCount = n(hs, primitiveAction);
		Map<HashableState, Double> taskRewards = totalRewardsByTask.get(primitiveAction);
		Double totalReward = taskRewards.get(hs);
		if (totalReward == null) { totalReward = 0.0; }
		// computes r(s,a) / n(s,a)
		double approximateReward = totalReward / (1.0 * stateActionCount);
		storeReward(primitiveAction, hs, approximateReward);
	}

	// P^a(s,sPrime) <- n(s,a,sPrime) / n(s,a)
	private void setTransitionProbability_eq7(GroundedTask primitiveAction, HashableState hs, HashableState hsPrime) {
		double stateActionCount = n(hs, primitiveAction);
		HashMap<HashableState, HashMap<HashableState, Integer>> totalTransitions = totalTransitionsByTask.get(primitiveAction);
		HashMap<HashableState, Integer> totalTransitionsFromState = totalTransitions.get(hs);
		Integer countForThisTransition = totalTransitionsFromState.get(hsPrime);
		if (countForThisTransition == null) { countForThisTransition = 0; }
		// computes n(s,a,sPrime) / n(s,a)
		double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
		int numberOfSteps = 1;
		storeTransitionProbability(primitiveAction, hs, hsPrime, approximateTransitionProbability, numberOfSteps);
	}

	// stores computed reward for both primitive and abstract tasks
	private void storeReward(GroundedTask task, HashableState hs, double newReward) {
		Map<HashableState, Double> storedRewards = storedRewardsByTask.computeIfAbsent(task, k -> new HashMap<>());
		storedRewards.put(hs, newReward);
	}

	// stores computed transition probability for both primitive and abstract tasks
	private void storeTransitionProbability(GroundedTask primitiveAction, HashableState hs, HashableState hsPrime, double transitionProbability, int numberOfSteps) {
        Map<HashableState, HashMap<HashableState, HashMap<Integer,Double>>> storedTransitions = storedTransitionsByTask.computeIfAbsent(primitiveAction, k -> new HashMap<>());
        Map<HashableState, HashMap<Integer,Double>> transitionsFromState = storedTransitions.computeIfAbsent(hs, k -> new HashMap<>());
        Map<Integer, Double> kStepsToProbability = transitionsFromState.computeIfAbsent(hsPrime, k -> new HashMap<>());
        kStepsToProbability.put(numberOfSteps, transitionProbability);
	}

	private int n(HashableState hs, GroundedTask primitiveAction) {
		Map<HashableState, Integer> stateCounts = stateActionCountsByTask.computeIfAbsent(primitiveAction, k -> new HashMap<>());
		Integer count = stateCounts.computeIfAbsent(hs, k -> 0);
		return count;
	}

	private GroundedTask pi(GroundedTask task, HashableState hs) {
		return getStoredPolicy(task, hs);
	}

	private double Q(GroundedTask task, HashableState hs, GroundedTask childTask) {
		return getStoredQ(task, hs, childTask);
	}

	private double V(GroundedTask task, HashableState hs) {
		return getStoredValue(task, hs);
	}

	private double R(GroundedTask task, HashableState hs) {
		return getStoredReward(task, hs);
	}

	private double P(GroundedTask task, HashableState hs, HashableState hsPrime, boolean initialize) {
		return getStoredTransitionProbability(task, hs, hsPrime, initialize);
	}

	private GroundedTask getStoredPolicy(GroundedTask task, HashableState hs) {
		Map<HashableState, GroundedTask> storedPolicies = storedPoliciesByTask.computeIfAbsent(task, k -> new HashMap<>());
		GroundedTask childTask = storedPolicies.get(hs);
		if (childTask == null) {
		    System.err.println("Warning: null childtask");
        }
		return childTask;
	}

	private double getStoredQ(GroundedTask task, HashableState hs, GroundedTask childTask) {
		Map<HashableState, HashMap<GroundedTask, Double>> storedQs = storedQValuesByTask.computeIfAbsent(task, k -> new HashMap<>());
		Map<GroundedTask, Double> childTaskToQValue = storedQs.computeIfAbsent(hs, k -> new HashMap<>());
		Double qValue = childTaskToQValue.computeIfAbsent(childTask, k -> 0.0);
		return qValue;
	}

	private double getStoredValue(GroundedTask task, HashableState hs) {
		Map<HashableState, Double> storedValue = storedValueByTask.computeIfAbsent(task, k -> new HashMap<>());
		Double value = storedValue.computeIfAbsent(hs, k -> 0.0);
		return value;
	}

	private double getStoredReward(GroundedTask task, HashableState hs) {
		Map<HashableState, Double> storedRewards = storedRewardsByTask.computeIfAbsent(task, k -> new HashMap<>());
		Double reward = storedRewards.computeIfAbsent(hs, k -> task.isPrimitive() ? Vmax : 0);
		//0.0;
		return reward;
	}

	private Map<HashableState, HashMap<Integer,Double>> getStoredTransitions(GroundedTask task, HashableState hs) {
		Map<HashableState, HashMap<HashableState, HashMap<Integer,Double>>> storedTransitions = storedTransitionsByTask.computeIfAbsent(task, k -> new HashMap<>());
		Map<HashableState, HashMap<Integer,Double>> transitionsFromState = storedTransitions.computeIfAbsent(hs, k -> new HashMap<>());
		return transitionsFromState;
	}

	private List<HashableState> getHSPrimes(GroundedTask task, HashableState hs) {
	    return new ArrayList<>(getStoredTransitions(task, hs).keySet());
    }

	private double getStoredTransitionProbability(GroundedTask task, HashableState hs, HashableState hsPrime, boolean initialize) {
		Map<HashableState, HashMap<Integer,Double>> transitionsFromState = getStoredTransitions(task, hs);
		if (!transitionsFromState.containsKey(hsPrime)) {
			return 0.0;
		}
//		Map<Integer, Double> stepsToProbability;
//		if (initialize) {
//			stepsToProbability = transitionsFromState.computeIfAbsent(hsPrime, k -> new HashMap<>());
//		} else {
//			stepsToProbability = transitionsFromState.get(hsPrime);
//			// here, if it is null then we know this transition is 0.0 and do not want to initialize it (for sparsity)
//			if (stepsToProbability == null) {
//				return 0.0;
//			}
//		}
		Map<Integer, Double> stepsToProbability = transitionsFromState.get(hsPrime);
		double transitionProbability = 0.0;
		for (Integer kSteps : stepsToProbability.keySet()) {
            double discount = Math.pow(gamma, kSteps);
		    double probability = stepsToProbability.get(kSteps);
		    double product = discount * probability;
		    transitionProbability += product;
        }
		return transitionProbability;
	}

	private boolean isTerminal(GroundedTask task, HashableState hs) {
        Map<HashableState, Boolean> taskTerminalCheck = cachedTerminalCheck.computeIfAbsent(task, k -> new HashMap<>());
        Boolean terminal = taskTerminalCheck.get(hs);
		if (terminal == null) {
			State s = task.mapState(hs.s());
			terminal = task.isComplete(s) || task.isFailure(s);// || rootSolve.isComplete(s);
			taskTerminalCheck.put(hs, terminal);
		}
		return terminal;
	}

	private List<HashableState> getKnownTerminalStates(GroundedTask task) {
        Set<HashableState> taskEnvelope = envelopesByTask.get(task);
        List<HashableState> taskTerminalStates = new ArrayList<>();
        for (HashableState state : taskEnvelope) {
            if (isTerminal(task, state)) {
                taskTerminalStates.add(state);
            }
        }
        return taskTerminalStates;
    }

	private double doDynamicProgramming(GroundedTask task) {
		Set<HashableState> taskEnvelope = envelopesByTask.get(task);
		List<HashableState> taskTerminalStates = getKnownTerminalStates(task);
		double maxDelta = 0.0;
		for(HashableState hsPrime : taskEnvelope) {
			// update rewards
			double deltaR = setR_eq4(task, hsPrime);
			if (deltaR > maxDelta) {
				maxDelta = deltaR;
			}
			for (HashableState taskTerminalState : taskTerminalStates) {
				// update transitions
				double deltaP = setT_eq5(task, hsPrime, taskTerminalState);
				if (deltaP > maxDelta) {
					maxDelta = deltaP;
				}
			}
		}
		return maxDelta;
	}

	private double setR_eq4(GroundedTask task, HashableState hs) {

        // get the action (child / subtask) that would be selected by policy
        GroundedTask childTask = pi(task, hs);
        if (childTask == null) {
            System.err.println("null child task -- policy was not computed for the given state");
            List<GroundedTask> childTasks = task.getGroundedChildTasks(task.mapState(hs.s()));
            childTask = childTasks.get(RandomFactory.getMapped(0).nextInt(childTasks.size()));
        }
        double childReward = R(childTask, hs);

        //now compute the expected reward
		List<HashableState> hsPrimes = getHSPrimes(task, hs);
		double expectedReward = 0.0;
		for (HashableState hsPrime : hsPrimes) {
			if (isTerminal(task, hsPrime)) {
				continue;
			}
			double childTransitionProbability = P(childTask, hs, hsPrime, true);
			double parentReward = R(task, hsPrime);
			expectedReward += childTransitionProbability * parentReward;
		}

		// get the old reward, store the new reward, compute the delta
		double oldRewardForTaskInState = R(task, hs);
		double rewardForTaskInState = childReward + expectedReward;
		storeReward(task, hs, rewardForTaskInState);
		double delta = Math.abs(rewardForTaskInState - oldRewardForTaskInState);
		return delta;
	}

	private double setT_eq5(GroundedTask task, HashableState hs, HashableState hsX) {

        // get the action (child / subtask) that would be selected by policy
        GroundedTask childTask = pi(task, hs);
		double childTerminalTransitionProbability = P(childTask, hs, hsX, true);

        List<HashableState> hsPrimes = getHSPrimes(task, hs);
		double expectedTransitionProbability = 0.0;
		for (HashableState hsPrime : hsPrimes) {
			if (isTerminal(task, hsPrime)) {
				continue;
			}
			double childTransitionProbability = P(childTask, hs, hsPrime, true);
			double parentTerminalTransitionProbability = P(task, hsPrime, hsX, true);
			expectedTransitionProbability += childTransitionProbability * parentTerminalTransitionProbability;
		}

        double oldP = P(task, hs, hsX, true);
        double newP = childTerminalTransitionProbability + expectedTransitionProbability;

		int numberOfSteps = stepsByTask.computeIfAbsent(task, k->1);
		storeTransitionProbability(task, hs, hsX, newP, numberOfSteps);

		double delta = Math.abs(newP - oldP);
		return delta;
	}

	private void updateTotalReward(GroundedTask task, HashableState hs, double reward) {
		if (reward == 0.0) {
			// skip zero rewards, don't need to store or update
			return;
		}
        Map<HashableState, Double> taskTotalRewards = totalRewardsByTask.computeIfAbsent(task, k -> new HashMap<>());
        Double totalReward = taskTotalRewards.get(hs);
		if (totalReward == null) {
			totalReward = 0.0;
		}
		totalReward = totalReward + reward;
		taskTotalRewards.put(hs, totalReward);
		totalRewardsByTask.put(task, taskTotalRewards);
	}

	private void incrementStateActionCount(GroundedTask primitiveAction, HashableState hs) {
		int count = n(hs, primitiveAction);
		count = count + 1;
		stateActionCountsByTask.get(primitiveAction).put(hs, count);
	}

	private void incrementTotalTransitionCount(GroundedTask task, HashableState hs, HashableState hsPrime) {
		HashMap<HashableState, HashMap<HashableState, Integer>> totalTransitions = totalTransitionsByTask.get(task);
		if (totalTransitions == null) {
			totalTransitions = new HashMap<>();
			totalTransitionsByTask.put(task, totalTransitions);
		}
		HashMap<HashableState, Integer> transitionsFromState = totalTransitions.get(hs);
		if (transitionsFromState == null) {
			transitionsFromState = new HashMap<>();
			totalTransitions.put(hs, transitionsFromState);
		}
		Integer transitionCount = transitionsFromState.get(hsPrime);
		if (transitionCount == null) {
			transitionCount = 0;
		}
		transitionCount = transitionCount + 1;
		transitionsFromState.put(hsPrime, transitionCount);
		totalTransitions.put(hs, transitionsFromState);
		totalTransitionsByTask.put(task, totalTransitions);
	}

	private boolean doValueIteration(GroundedTask task, Set<HashableState> taskEnvelope) {
		if (taskEnvelope.size() < 1) {
			System.err.println("Warning: empty taskEnvelope");
			return true;
		}
		double maxDelta = 0.0;
		for(HashableState hsPrime : taskEnvelope){
			List<GroundedTask> childTasks = task.getGroundedChildTasks(task.mapState(hsPrime.s()));
			for(GroundedTask childTask : childTasks){
				setQ_eq1(task, hsPrime, childTask);
			}
			double deltaV = setV_eq2(task, hsPrime);
			if(deltaV > maxDelta) {
				maxDelta = deltaV;
			}
		}
		boolean converged = maxDelta < maxDeltaInPolicy;
		return converged;
	}

	private double setQ_eq1(GroundedTask task, HashableState hs, GroundedTask childTask) {

		double oldQ = Q(task, hs, childTask);

		double childReward = R(childTask, hs);

		Map<HashableState, HashMap<Integer,Double>> childTransitions = getStoredTransitions(childTask, hs);

		double expectedValue = 0.0;
		for (HashableState hsPrime : childTransitions.keySet()) {
			double childTransitionProbability = P(childTask, hs, hsPrime, true);
			double parentValueAtStatePrime = V(task, hsPrime);
			expectedValue += childTransitionProbability * parentValueAtStatePrime;
		}

		double newQ = childReward + expectedValue;

		storedQValuesByTask.get(task).get(hs).put(childTask, newQ);

		double delta = Math.abs(newQ - oldQ);
		return delta;
	}

	private double setV_eq2(GroundedTask task, HashableState hs) {

		double oldV = V(task, hs);

		double newV;
//		if(!task.isPrimitive() && isTerminal(task, hs)) {
		if(isTerminal(task, hs)) {
			newV = task.getReward(task.mapState(hs.s()), task.getAction(), task.mapState(hs.s()));
		} else {
			List<GroundedTask> childTasks = task.getGroundedChildTasks(task.mapState(hs.s()));
			double maxQ = Integer.MIN_VALUE;
			for (GroundedTask childTask : childTasks) {
				double qValue = Q(task, hs, childTask);
				if (qValue > maxQ) {
					maxQ = qValue;
				}
			}
			newV = maxQ;
		}

		storedValueByTask.get(task).put(hs, newV);

		double delta = Math.abs(newV - oldV);
		return delta;
	}

	private void setPi(GroundedTask task, HashableState hs) {
		List<GroundedTask> childTasks = task.getGroundedChildTasks(task.mapState(hs.s()));
		double maxQ = Integer.MIN_VALUE;
		List<GroundedTask> maxChildTasks = new ArrayList<>();
		for (GroundedTask childTask : childTasks) {
			double qValue = Q(task, hs, childTask);
			if (qValue > maxQ) {
				maxQ = qValue;
				maxChildTasks.clear();
				maxChildTasks.add(childTask);
			} else if (qValue == maxQ) {
				maxChildTasks.add(childTask);
			}
		}
		// get a random child task among the equally good actions
		GroundedTask maxChildTask = maxChildTasks.get(RandomFactory.getMapped(0).nextInt(maxChildTasks.size()));
        Map<HashableState, GroundedTask> storedPolicies = storedPoliciesByTask.computeIfAbsent(task, k -> new HashMap<>());
        storedPolicies.put(hs, maxChildTask);
	}


}