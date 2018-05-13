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

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		this.env = env;
		Episode e = new Episode(initialState);
		numberPrimitivesExecuted = 0;
		for (GroundedTask task : taskDataMap.keySet()) {
			taskDataMap.get(task).clearTimesteps();
		}

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
	 *
	 * @param task 	   the current grounded task (composite or primitive action)
	 * @param e        current episode
	 * @param maxSteps the max number of primitive actions allowed
	 * @return the episode
	 */
	protected Episode R_MaxQ(GroundedTask task, HashableState hs, Episode e, int maxSteps) {

		System.out.println(tabLevel + ">>> " + task.getAction());

		if (task.isPrimitive()) {
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
				int k = stepsAfter - stepsBefore;
				updateNumberOfSteps(childTask, k);

				tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));

				State s = e.stateSequence.get(e.stateSequence.size() - 1);
				hs = hashingFactory.hashState(s);
				addToEnvelope(task, hs);
			} while (
					!isTerminal(task, hs)
				 && (numberPrimitivesExecuted < maxSteps || maxSteps == -1))
			;
			System.out.println(tabLevel + "<<< " + task.getAction());
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

	private void addToEnvelope(GroundedTask task, HashableState hs) {
		getTaskData(task).addToEnvelope(hs);
	}

	private Set<HashableState> getEnvelope(GroundedTask task) {
		return getTaskData(task).getEnvelope();
	}

	public void computePolicy(GroundedTask task, HashableState hs) {

		handleTimesteps(task);

		prepareEnvelope(task, hs);

		Set<HashableState> envelope = getEnvelope(task);
		boolean converged = false;
		int attempts = maxIterationsInModel;
		while (!converged && attempts > 0) {
			converged = doValueIteration(task, envelope);
			attempts -= 1;
		}
		if (attempts < 1) {
			System.err.println("Warning: ValueIteration exhausted attempts to converge");
		}
		setPi(task, hs);
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
	 *
	 */
	private void prepareEnvelope(GroundedTask task, HashableState hs) {
		Set<HashableState> envelope = getEnvelope(task);
		if (!envelope.contains(hs)) {
			envelope.add(hs);
			List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(task, hs));
			for (GroundedTask childTask : childTasks) {

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
	 *
	 * @param childTask a childTask of the current task being considered when this method was called
	 * @param hs        the current state
	 */
	private void computeModel(GroundedTask parent, GroundedTask childTask, HashableState hs) {
		if (childTask.isPrimitive()) {
			computeModelPrimitive(childTask, hs);
		} else {

			computePolicy(childTask, hs);
			Set<HashableState> childTaskEnvelope = getEnvelope(childTask);
			for (HashableState oneOfAllHS : childTaskEnvelope) {
				if (hs.equals(oneOfAllHS)) {
					continue;
				}
				computePolicy(childTask, oneOfAllHS);
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
			}
		}
	}

	private void computeModelPrimitive(GroundedTask primitiveAction, HashableState hs) {
		RMAXQStateData stateData = getStateData(primitiveAction, hs);
		int stateActionCount = stateData.getStateActionCount();
		if (stateActionCount >= threshold) {
			setReward_eq6(hs, primitiveAction);
			for (HashableState hsPrime : allGroundStates) {
				setTransitionProbability_eq7(primitiveAction, hs, hsPrime);
			}
		}
	}

	// R^a(s) <- r(s,a) / n(s,a)
	private void setReward_eq6(HashableState hs, GroundedTask primitiveAction) {
		RMAXQStateData stateData = getStateData(primitiveAction, hs);
		int stateActionCount = stateData.getStateActionCount();
		double totalReward =  stateData.getTotalReward();
		double approximateReward = totalReward / (1.0 * stateActionCount);
		stateData.setStoredReward(approximateReward);
	}

	// P^a(s,sPrime) <- n(s,a,sPrime) / n(s,a)
	private void setTransitionProbability_eq7(GroundedTask primitiveAction, HashableState hs, HashableState hsPrime) {
		RMAXQStateData stateData = getStateData(primitiveAction, hs);
		int stateActionCount = stateData.getStateActionCount();
		int countForThisTransition = stateData.getTotalTransitionCount(hsPrime);
		// computes n(s,a,sPrime) / n(s,a)
		double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
		int k = 1; // this is a primitive action, assumed to always take one step
//		double discount = Math.pow(gamma, k); // discount, as per multi-time model
//		double probability = discount * approximateTransitionProbability;
		double probability = approximateTransitionProbability;
		stateData.setStoredTransitionsBySteps(hsPrime, probability, k);
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

	private double P(GroundedTask task, HashableState hs, HashableState hsPrime, int steps, boolean initialize) {
		return getStoredTransitionProbabilityForSteps(task, hs, hsPrime, steps, initialize);
	}

	private double P(GroundedTask task, HashableState hs, HashableState hsPrime, boolean initialize) {
		return getStoredExpectedProbability(task, hs, hsPrime, initialize);
	}

	private GroundedTask getStoredPolicy(GroundedTask task, HashableState hs) {
		RMAXQStateData stateData = getStateData(task, hs);
		GroundedTask policyAction = stateData.getStoredPolicyAction();
		return policyAction;
	}

	private double getStoredQ(GroundedTask task, HashableState hs, GroundedTask childTask) {
		RMAXQStateData stateData = getStateData(task, hs);
		double qValue = stateData.getQValue(childTask);
		return qValue;
	}

	private double getStoredValue(GroundedTask task, HashableState hs) {
		RMAXQStateData stateData = getStateData(task, hs);
		double value = stateData.getStoredValue();
		return value;
	}

	private double getStoredReward(GroundedTask task, HashableState hs) {
		RMAXQStateData stateData = getStateData(task, hs);
		double reward = stateData.getStoredReward();
		return reward;
	}

	private Map<HashableState, HashMap<Integer, Double>> getStoredTransitions(GroundedTask task, HashableState hs) {
		RMAXQStateData stateData = getStateData(task, hs);
		return stateData.getStoredTransitionsBySteps();
	}

	private List<HashableState> getHSPrimes(GroundedTask task, HashableState hs) {
		return new ArrayList<>(getStoredTransitions(task, hs).keySet());
	}

	private double getStoredTransitionProbabilityForSteps(GroundedTask task, HashableState hs, HashableState hsPrime, int steps, boolean initialize) {
		RMAXQStateData stateData = getStateData(task, hs);
		double probability = stateData.getStoredTransitionProbabilityForSteps(hsPrime, steps, initialize);
		return probability;
	}

	private double getStoredExpectedProbability(GroundedTask task, HashableState hs, HashableState hsPrime, boolean initialize) {
		RMAXQStateData stateData = getStateData(task, hs);
//		double expectedProbability = stateData.getStoredExpectedProbability(hsPrime, initialize);
		double transitionProbability = 0.0;
		HashMap<HashableState,HashMap<Integer,Double>> map = stateData.getStoredTransitionsBySteps();
		Map<Integer,Double> stepsToProbability = map.get(hsPrime);
		if (stepsToProbability == null) {
			return transitionProbability; // return 0.0, do not store it
		}
		for (Integer kSteps : stepsToProbability.keySet()) {
			double discount = Math.pow(gamma, kSteps);
			double probability = stepsToProbability.get(kSteps);
			double product = discount * probability;
			transitionProbability += product;
		}
		return transitionProbability;
//		return expectedProbability;
	}

	private HashMap<GroundedTask, HashMap<HashableState, State>> cachedStateMapping = new HashMap<>();
	private State getMappedState(GroundedTask task, HashableState hs) {
		Map<HashableState, State> stateMapping = cachedStateMapping.computeIfAbsent(task, i -> new HashMap<>());
		State s = stateMapping.computeIfAbsent(hs, i -> task.mapState(hs.s()));
		return s;
//		return task.mapState(hs.s());
	}

	private boolean isTerminal(GroundedTask task, HashableState hs) {
		State s = getMappedState(task, hs);//task.mapState(hs.s());
		Boolean terminal = task.isComplete(s) || task.isFailure(s);// || rootSolve.isComplete(s);
		return terminal;
	}

	private List<HashableState> getKnownTerminalStates(GroundedTask task) {
        Set<HashableState> taskEnvelope = getEnvelope(task);
        List<HashableState> taskTerminalStates = new ArrayList<>();
        for (HashableState state : taskEnvelope) {
            if (isTerminal(task, state)) {
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
			double deltaR = setR_eq4(task, hsPrime);
			if (deltaR > maxDelta) {
				maxDelta = deltaR;
			}
			for (HashableState taskTerminalState : taskTerminalStates) {
				// update transitions
				double deltaP = setP_eq5(task, hsPrime, taskTerminalState);
				if (deltaP > maxDelta) {
					maxDelta = deltaP;
				}
			}
		}
		return maxDelta;
	}

	private double setR_eq4(GroundedTask task, HashableState hs) {
		RMAXQStateData stateData = getStateData(task, hs);

        // get the action (child / subtask) that would be selected by policy
        GroundedTask childTask = pi(task, hs);
        double childReward = R(childTask, hs);

        //now compute the expected reward
		Set<HashableState> hsPrimes = allGroundStates;//getHSPrimes(task, hs);
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
		stateData.setStoredReward(rewardForTaskInState);
		double delta = Math.abs(rewardForTaskInState - oldRewardForTaskInState);
		return delta;
	}

	private double setP_eq5(GroundedTask task, HashableState hs, HashableState hsX) {
		RMAXQTaskData taskData = getTaskData(task);
		RMAXQStateData stateData = getStateData(task, hs);

		double oldP = P(task, hs, hsX, true);
		double newP = 0.0;

		GroundedTask childTask = pi(task, hs);
//		List<Integer> steps = stepsByTask.computeIfAbsent(task, k->new ArrayList<>());
		Set<Integer> possibleNumbersOfSteps = taskData.getPossibleK();
		for (Integer k : possibleNumbersOfSteps) {
			// get the action (child / subtask) that would be selected by policy
			double childTerminalTransitionProbability = P(childTask, hs, hsX, k, false);

			Set<HashableState> hsPrimes = allGroundStates;//getHSPrimes(task, hs);
			double expectedTransitionProbability = 0.0;
			for (HashableState hsPrime : hsPrimes) {
				if (isTerminal(task, hsPrime)) {
					continue;
				}
				double childTransitionProbability = P(childTask, hs, hsPrime, k, true);
				double parentTerminalTransitionProbability = P(task, hsPrime, hsX, k, true);
				expectedTransitionProbability += childTransitionProbability * parentTerminalTransitionProbability;
			}

			double probGivenK = childTerminalTransitionProbability + expectedTransitionProbability;
			double discount = Math.pow(gamma, k);
			double discountedProb = discount * probGivenK;
			double approxProb = discountedProb;
//			double approxProb = probGivenK;
			stateData.setStoredTransitionsBySteps(hsX, approxProb, k);
			newP += approxProb;
		}
		double delta = Math.abs(newP - oldP);
		return delta;
	}

	private void updateTotalReward(GroundedTask primitiveAction, HashableState hs, double reward) {
		RMAXQStateData stateData = getStateData(primitiveAction, hs);
		double totalReward = stateData.getTotalReward();
		totalReward = totalReward + reward;
		stateData.setTotalReward(totalReward);
	}

	private void incrementStateActionCount(GroundedTask primitiveAction, HashableState hs) {
		RMAXQStateData stateData = getStateData(primitiveAction, hs);
		int stateActionCount = stateData.getStateActionCount();
		stateActionCount = stateActionCount + 1;
		stateData.setStateActionCount(stateActionCount);
	}

	private void incrementTotalTransitionCount(GroundedTask primitiveAction, HashableState hs, HashableState hsPrime) {
		RMAXQStateData stateData = getStateData(primitiveAction, hs);
		int transitionCount = stateData.getTotalTransitionCount(hsPrime);
		transitionCount = transitionCount + 1;
		stateData.setTotalTransitionCount(hsPrime, transitionCount);
	}

	private boolean doValueIteration(GroundedTask task, Set<HashableState> taskEnvelope) {
		if (taskEnvelope.size() < 1) {
			System.err.println("Warning: empty taskEnvelope");
			return true;
		}
		double maxDelta = 0.0;
		for(HashableState hsPrime : taskEnvelope){
			List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(task,hsPrime));//task.mapState(hsPrime.s()));
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
		RMAXQStateData stateData = getStateData(task, hs);

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

		stateData.setStoredQValues(childTask, newQ);

		double delta = Math.abs(newQ - oldQ);
		return delta;
	}

	private Map<GroundedTask, HashMap<HashableState,Double>> cachedGoalRewards = new HashMap<>();
	private HashableStateFactory cachingHSF = new SimpleHashableStateFactory();
	private double setV_eq2(GroundedTask task, HashableState hs) {
		RMAXQStateData stateData = getStateData(task, hs);

		double oldV = V(task, hs);

		double newV;
//		if(!task.isPrimitive() && isTerminal(task, hs)) {
		if(isTerminal(task, hs)) {

			// unsure which this should be, either this line
//			newV = stateData.getStoredReward();
			// or this line
//			newV = task.getReward(null, task.getAction(), getMappedState(task, hs));
			State abstractState = getMappedState(task, hs);
			HashableState hashedAbstractState = cachingHSF.hashState(abstractState);
			newV = cachedGoalRewards.computeIfAbsent(task, i -> new HashMap<>()).computeIfAbsent(hashedAbstractState, i -> task.getReward(null, task.getAction(), abstractState));



		} else {
			List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(task, hs));
			double maxQ = Integer.MIN_VALUE;
			for (GroundedTask childTask : childTasks) {
				double qValue = Q(task, hs, childTask);
				if (qValue > maxQ) {
					maxQ = qValue;
				}
			}
			newV = maxQ;
		}

		stateData.setStoredValue(newV);

		double delta = Math.abs(newV - oldV);
		return delta;
	}

	private void setPi(GroundedTask task, HashableState hs) {
		List<GroundedTask> childTasks = task.getGroundedChildTasks(getMappedState(task, hs));
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
		RMAXQStateData stateData = getStateData(task, hs);
		stateData.setStoredPolicyAction(maxChildTask);
	}


}