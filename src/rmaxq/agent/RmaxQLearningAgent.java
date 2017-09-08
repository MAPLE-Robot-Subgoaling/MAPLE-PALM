package rmaxq.agent;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;

import java.util.*;

public class RmaxQLearningAgent implements LearningAgent {

	private static final double PSEUDO_REWARD_FOR_NONPRIMITIVE_TASKS = 1.0;

	//R^a(s) for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, Double>> storedRewardsByTask;

	//P^a(s, s') for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, HashMap< HashableState, Double>>> storedTransitionsByTask;

//	private HashMap<GroundedTask, HashMap< HashableState, Double>> nonprimitiveValueByTask;

	//n(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Integer>> stateActionCountsByTask;

	//n(s,a,s')
	private HashMap<GroundedTask, HashMap< HashableState, HashMap<HashableState, Integer>>> totalTransitionsByTask;

	//r(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Double>> totalRewardsByTask;

	//grounded task map
	private HashMap<String, GroundedTask> taskNameToGroundedTask;

	//QProviders for each grounded task
	private HashMap<GroundedTask, QProviderRmaxQ> qProvidersByTask;

	//policies
//	private HashMap<GroundedTask, SolverDerivedPolicy> qPolicy;

	//envelopesByTask(a)
	private HashMap<GroundedTask, List<HashableState>> envelopesByTask;

	//ta 
	private HashMap<GroundedTask, List<HashableState>> terminalStatesByTask;

	private HashMap<GroundedTask, Integer> timestepsByTask;

	private double maxDeltaInPolicy;
	private double maxDeltaInModel;
	private int threshold;
	private Task root;
	private GroundedTask rootSolve;
	private HashableStateFactory hashingFactory;
	private double Vmax;
	private Environment env;
	private State initialState;	
	private List<HashableState> reachableStates = new ArrayList<HashableState>();
	private long time = 0;
	private int numberPrimitivesExecuted;
//	private boolean computePolicy = true;
//	private List<Double> prevEpisodeRewards;

	public RmaxQLearningAgent(Task root, HashableStateFactory hs, State initState, double vmax, int threshold, double maxDeltaInPolicy, double maxDeltaInModel){
		this.root = root;
		this.totalRewardsByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.storedRewardsByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
//		this.nonprimitiveValueByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.storedTransitionsByTask = new HashMap<GroundedTask, HashMap<HashableState, HashMap<HashableState, Double>>>();
		this.stateActionCountsByTask = new HashMap<GroundedTask, HashMap<HashableState, Integer>>();
		this.qProvidersByTask = new HashMap<GroundedTask, QProviderRmaxQ>();
		this.envelopesByTask = new HashMap<GroundedTask, List<HashableState>>();
		this.totalTransitionsByTask = new HashMap<GroundedTask, HashMap<HashableState,HashMap<HashableState,Integer>>>();
		this.terminalStatesByTask = new HashMap<GroundedTask, List<HashableState>>();
//		this.qPolicy = new HashMap<GroundedTask, SolverDerivedPolicy>();
		this.taskNameToGroundedTask = new HashMap<String, GroundedTask>();
		this.maxDeltaInPolicy = maxDeltaInPolicy;
		this.maxDeltaInModel = maxDeltaInModel;
		this.hashingFactory = hs;
		this.Vmax = vmax;
		this.threshold = threshold;
		this.initialState = initState;
		this.timestepsByTask = new HashMap<GroundedTask, Integer>();
//		this.prevEpisodeRewards = new ArrayList<Double>();
		reachableStates = StateReachability.getReachableStates(initialState, root.getDomain(), hashingFactory);
	}

	public long getTime(){
		return time;
	}

	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	public Episode runLearningEpisode(Environment env, int maxSteps) {
		this.env = env;
		Episode e = new Episode(initialState);
		rootSolve = root.getAllGroundedTasks(env.currentObservation()).get(0);
		numberPrimitivesExecuted = 0;
//		actionTimesteps.clear();

		time = System.currentTimeMillis();
		HashableState hs = hashingFactory.hashState(env.currentObservation());
		e = R_MaxQ(hs, rootSolve, e, maxSteps);
		time = System.currentTimeMillis() - time;

//		checkConvergence(e, maxSteps);

		return e;
	}

//	protected void checkConvergence(Episode e, int maxSteps){
//		double cumulativeReward = 0;
//		for(double epRew : e.rewardSequence){
//			cumulativeReward += epRew;
//		}
//		prevEpisodeRewards.add(cumulativeReward);
//		int size = prevEpisodeRewards.size();
//		if( size > 2 && Math.abs(prevEpisodeRewards.get(size - 1) - prevEpisodeRewards.get(size - 2)) <= 1
//				&& Math.abs(prevEpisodeRewards.get(size - 2) - prevEpisodeRewards.get(size - 3)) <= 1
//				&& Math.abs(prevEpisodeRewards.get(size - 1) - prevEpisodeRewards.get(size - 3)) <= 1
//				&& e.actionSequence.size() < maxSteps){
//			computePolicy = false;
//			System.out.println("Stopping compute");
//		}else{
//			computePolicy = true;
//		}
//	}

	private HashMap<GroundedTask, HashMap<HashableState, Boolean>> cachedTerminalCheck = new HashMap<>();
	private boolean isTerminal(GroundedTask task, HashableState hs) {
		HashMap<HashableState, Boolean> taskTerminalCheck = cachedTerminalCheck.get(task);
		if (taskTerminalCheck == null) { taskTerminalCheck = new HashMap<>(); cachedTerminalCheck.put(task, taskTerminalCheck); }
		Boolean terminal = taskTerminalCheck.get(hs);
		if (terminal == null) {
			State s = hs.s();
			terminal = task.isComplete(s) || task.isFailure(s) || rootSolve.isComplete(s);
			taskTerminalCheck.put(hs, terminal);
		}
		return terminal;
	}

//	private SolverDerivedPolicy initializePolicyAndSolver(GroundedTask task) {
//		QProviderRmaxQ qValues = qProvider.get(task);
//		if (qValues == null) {
//			qValues = new QProviderRmaxQ(hashingFactory, task);
//			qProvider.put(task, qValues);
//		}
//
//		SolverDerivedPolicy policySetByTask = qPolicy.get(task);
//		if (policySetByTask == null) {
//			policySetByTask = new GreedyQPolicy();
//			qPolicy.put(task, policySetByTask);
//		}
//		policySetByTask.setSolver(qValues);
//		return policySetByTask;
//	}

	/**
	 * main loop of the algorithm that recurses down to primitive actions then updates counts
	 * @param hs the current state
	 * @param task the task to solve
	 * @param e current episode
	 * @param maxSteps the max number of primitive actions allowed
	 * @return the episode
	 */
	protected Episode R_MaxQ(HashableState hs, GroundedTask task, Episode e, int maxSteps){
//		System.out.println(task);
		if(task.isPrimitive()){
			e = executePrimitive(e, task, hs);
			return e;
		} else {
			while(!isTerminal(task, hs) && (numberPrimitivesExecuted < maxSteps || maxSteps == -1)) {
//				if (computePolicy) {
				computePolicy(task, hs);
//				}
				GroundedTask childTask = pi(task, hs);

				String taskName = getActionNameSafe(childTask.getAction());
				if (taskName.contains("_")) {
					System.out.print(taskName +"\n\t");
					for (QValue qv : qProvidersByTask.get(task).qValues(hs.s())) {
						System.out.print(qv.a + " " + qv.q + "\n\t");
					}
					System.out.println("");
				} else {
					System.out.print(taskName + " chosen from ");
					for (QValue qv : qProvidersByTask.get(task).qValues(hs.s())) {
						System.out.print(qv.a + " " + qv.q + ", ");
					}
					System.out.println("");
				}

				e = R_MaxQ(hs, childTask, e, maxSteps);
				State s = e.stateSequence.get(e.stateSequence.size() - 1);
				hs = hashingFactory.hashState(s);
			}
			System.out.println("\n");
			return e;
		}
	}

	private GroundedTask pi(GroundedTask task, HashableState hs) {
		QProviderRmaxQ qProvider = this.qProvidersByTask.get(task);
		SolverDerivedPolicy taskPolicy = new GreedyQPolicy();
		taskPolicy.setSolver(qProvider);
		Action maxqAction = taskPolicy.action(hs.s());
		String taskName = getActionNameSafe(maxqAction);
		addChildTasks(task, hs);
		GroundedTask childTask = taskNameToGroundedTask.get(taskName);
		return childTask;
	}

	private static HashMap<String, String> cachedNames = new HashMap<>();
	public static String getActionNameSafe(Action action) {
		String name = cachedNames.get(action.toString());
		if (name != null) { return name; }
		name = action.actionName();
		if (action instanceof ObjectParameterizedAction) {
			ObjectParameterizedAction opa = (ObjectParameterizedAction) action;
			name = action.actionName() + "_" + String.join("_",opa.getObjectParameters());
		}
		cachedNames.put(action.toString(), name);
		return name;
	}

	private void updateTotalReward(GroundedTask task, HashableState hs, double reward) {
		if (reward == 0.0) {
			// skip zero rewards, don't need to store or update
			return;
		}
		HashMap<HashableState, Double> taskTotalRewards = totalRewardsByTask.get(task);
		if (taskTotalRewards == null) {
			taskTotalRewards = new HashMap<>();
			totalRewardsByTask.put(task, taskTotalRewards);
		}
		Double totalReward = taskTotalRewards.get(hs);
		if (totalReward == null) {
			totalReward = 0.0;
		}
		totalReward = totalReward + reward;
		taskTotalRewards.put(hs, totalReward);
		totalRewardsByTask.put(task, taskTotalRewards);
	}

	private void incrementStateActionCount(GroundedTask task, HashableState hs) {
		int count = n(task, hs);
		count = count + 1;
		stateActionCountsByTask.get(task).put(hs, count);
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

	private Episode executePrimitive(Episode e, GroundedTask task, HashableState hs) {
		Action a = task.getAction();
		EnvironmentOutcome outcome = env.executeAction(a);
		e.transition(outcome);
		State sPrime = outcome.op;
		HashableState hsPrime = hashingFactory.hashState(sPrime);
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

	private void initializeQProviderForTask(GroundedTask task) {
		QProviderRmaxQ qProvider = qProvidersByTask.get(task);
		if(qProvider == null){
			qProvider = new QProviderRmaxQ(hashingFactory, task);
			qProvidersByTask.put(task, qProvider);
		}
	}

	private List<HashableState> initializeEnvelopeForTask(GroundedTask task) {

		Integer timesteps = timestepsByTask.get(task);
		if(timesteps == null){
			timesteps = 0;
			timestepsByTask.put(task, timesteps);
		}

		List<HashableState> taskEnvelope = envelopesByTask.get(task);
		if(taskEnvelope == null){
			taskEnvelope = new ArrayList<HashableState>();
			envelopesByTask.put(task, taskEnvelope);
		}
		return taskEnvelope;
	}

//
//		List<HashableState> taskEnvelope = envelopesByTask.get(task);
//		if(taskEnvelope == null){
//			taskEnvelope = new ArrayList<HashableState>();
//			envelopesByTask.put(task, taskEnvelope);
//		}
//
//		if(aTime < numberPrimitivesExecuted){
//			actionTimesteps.put(task, numberPrimitivesExecuted);
//			taskEnvelope.clear();
//		}
//
//		QProviderRmaxQ qp = qProvider.get(task);
//		if(qp == null){
//			qp = new QProviderRmaxQ(hashingFactory, task);
//			qProvider.put(task, qp);
//		}
//
//		return taskEnvelope;
//		throw new RuntimeException("not implemented");
//	}

	/**
	 * computes and updates action values for the state in the task
	 * @param task the current task
	 * @param hs current state
	 */
	public void computePolicy(GroundedTask task, HashableState hs){

		List<HashableState> taskEnvelope = initializeEnvelopeForTask(task);
		if (timestepsByTask.get(task) < numberPrimitivesExecuted) {
			timestepsByTask.put(task, numberPrimitivesExecuted);
			taskEnvelope.clear();
		}

		prepareEnvelope(task, hs);

		initializeQProviderForTask(task);
		boolean converged = false;
		while(!converged){   
			double maxDelta = 0;
			for(HashableState hsPrime : taskEnvelope){
				List<GroundedTask> childTasks = task.getGroundedChildTasks(hsPrime.s());
				for(GroundedTask childTask : childTasks){
					double deltaQ = equation_1(task, hsPrime, childTask);
					if(deltaQ > maxDelta) {
						maxDelta = deltaQ;
					}
				}
			}
			if(maxDelta < maxDeltaInPolicy) {
				converged = true;
			}
		}
	}

	/**
	 * calculates and stores the possible states that can be reached from the current state 
	 * @param task the task
	 * @param hs the state
	 */
	public void prepareEnvelope(GroundedTask task, HashableState hs){
		List<HashableState> envelope = envelopesByTask.get(task);
		if(!envelope.contains(hs)){
			envelope.add(hs);
			List<GroundedTask> childTasks = task.getGroundedChildTasks(hs.s());
			for(GroundedTask childTask : childTasks){

				computeModel(childTask, hs);

				HashMap<HashableState, HashMap<HashableState, Double>> transitions = storedTransitionsByTask.get(childTask);
				if (transitions == null) {
					continue;
				}
				HashMap<HashableState, Double> transitionsFromState = transitions.get(hs);
				if (transitionsFromState == null) {
					continue;
				}
				Set<HashableState> possibleStates = transitionsFromState.keySet();
				for (HashableState hsPrime : possibleStates) {
					double transitionProbability = P(childTask, hs, hsPrime);
					if (transitionProbability > 0) {
						prepareEnvelope(task, hsPrime);
					}
				}
			}
		}
	}

	private void computeModelPrimitive(GroundedTask task, HashableState hs) {
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			equation_6(task, hs);
			for (HashableState hsPrime : reachableStates) {
				equation_7(task, hs, hsPrime);
			}
		} else {
			// only need to update model beyond threshold
			return;
		}
		return;
//		throw new RuntimeException("the issue seems to be here, in storing and reading the model, in that vmax is never READ(breakpoint on it");
	}

	private void equation_6(GroundedTask task, HashableState hs) {
		if (!task.isPrimitive()) {
			throw new RuntimeException("Error: tried to approximate reward (equation 6) on non-primitive task");
		}
		// only primitive tasks are allowed to be computed this way
		double reward = 0.0;
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			HashMap<HashableState, Double> taskRewards = totalRewardsByTask.get(task);
			if (taskRewards == null) {
				reward = 0.0;
			} else {
				Double totalReward = taskRewards.get(hs);
				if (totalReward == null) { totalReward = 0.0; }
				double approximateReward = totalReward / (1.0 * stateActionCount);
				reward = approximateReward;
			}
		} else {
			reward = Vmax;
		}
		// only store non-zero rewards
//		if (reward != 0.0) {
		storedRewardsByTask.get(task).put(hs, reward);
//		}
	}

	private void equation_7(GroundedTask task, HashableState hs, HashableState hsPrime) {
		double transitionProbability = 0.0;
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			HashMap<HashableState, HashMap<HashableState, Integer>> totalTransitions = totalTransitionsByTask.get(task);
			HashMap<HashableState, Integer> transitionsFromState = totalTransitions.get(hs);
			Integer countForThisTransition = transitionsFromState.get(hsPrime);
			if (countForThisTransition == null) { countForThisTransition = 0; }
			double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
			transitionProbability = approximateTransitionProbability;
		} else {
			transitionProbability = 0.0;
		}
//		if (transitionProbability != 0.0) {
		storedTransitionsByTask.get(task).get(hs).put(hsPrime, transitionProbability);
//		}
	}

	private int n(GroundedTask task, HashableState hs) {
		HashMap<HashableState, Integer> stateCounts = stateActionCountsByTask.get(task);
		if (stateCounts == null) {
			stateCounts = new HashMap<>();
			stateActionCountsByTask.put(task, stateCounts);
		}
		Integer count = stateCounts.get(hs);
		if (count == null) {
			count = 0;
			stateCounts.put(hs, count);
		}
		return count;
	}


	/**
	 * computes and stores totalRewardsByTask and transitionsByTask values for the task
	 * @param task the current task
	 * @param hs the current state
	 */
	private void computeModel(GroundedTask task, HashableState hs){
		if(task.isPrimitive()){
			computeModelPrimitive(task, hs);
		}else{

			computePolicy(task, hs);
			
//			initializeModelNonprimitive(task);

			boolean converged = false;
			while(!converged){
				converged = computeDynamicProgramming(task);
			}	
		}
	}

	private boolean computeDynamicProgramming(GroundedTask task) {
		List<HashableState> taskEnvelope = envelopesByTask.get(task);
		double maxChange = 0;
		for(HashableState hsPrime : taskEnvelope) {
			// get the action (child / subtask) that would be selected by policy
			GroundedTask childTask = pi(task, hsPrime);
			// update rewards
			equation_4(task, hsPrime, childTask);
			List<HashableState> taskTerminalStates = terminalStatesByTask.get(task);
			if (taskTerminalStates == null) {
				taskTerminalStates = getTerminalStates(task);
				terminalStatesByTask.put(task, taskTerminalStates);
			}
			for (HashableState taskTerminalState : taskTerminalStates) {
				// update transitions
				equation_5(task, hsPrime, childTask, taskTerminalState);
			}
		}
		return maxChange < maxDeltaInModel;
	}



	/**
	 * runs eqn 1 from RMAXQ paper on inputs
	 * @return the new q value
	 */
	private double equation_1(GroundedTask task, HashableState hs, GroundedTask childTask) {

		QProviderRmaxQ qProvider = qProvidersByTask.get(task);
		double oldQ = qProvider.qValue(hs.s(), childTask.getAction());

		double childReward = R(childTask, hs);

		double expectedValue = 0.0;
		for (HashableState hsPrime : reachableStates) {
			double childTransitionProbability = P(childTask, hs, hsPrime);
			double parentValueAtStatePrime = equation_2(task, hsPrime);
			expectedValue += childTransitionProbability * parentValueAtStatePrime;
		}

		double newQ = childReward + expectedValue;

		qProvider.update(hs.s(), childTask.getAction(), newQ);

		double delta = Math.abs(newQ - oldQ);
		System.out.println(newQ + " " + oldQ + " " + delta + " for " + task);

		return delta;
	}

	private double equation_2(GroundedTask task, HashableState hs) {
		QProviderRmaxQ qProvider = qProvidersByTask.get(task);
		double value = qProvider.value(hs.s());
		return value;
	}

//	private double equation_2(GroundedTask task, HashableState hs) {
//		if (task.isPrimitive()) {
//			throw new RuntimeException("error, trying to compute value on primitive task");
//		} else if (isTerminal(task, hs)) {
//			return PSEUDO_REWARD_FOR_NONPRIMITIVE_TASKS;
//		} else {
//			// composite, non-terminal
//			HashMap<HashableState, Double> nonprimitiveValue = nonprimitiveValueByTask.get(task);
//			// not 100% sure of next line
//			List<GroundedTask> childTasks = task.getGroundedChildTasks(hs.s());
//			double maxQ = Integer.MIN_VALUE;
//			for (GroundedTask childTask : childTasks) {
//				double q =
//			}
//		}
//	}
//
//	private double V(GroundedTask task, HashableState hs) {
//		HashMap<HashableState, Double> nonprimitiveValue = nonprimitiveValueByTask.get(task);
//		if (nonprimitiveValue == null) {
//			nonprimitiveValue = new HashMap<>();
//			nonprimitiveValueByTask.put(task, nonprimitiveValue);
//		}
//		Double value = nonprimitiveValue.get(hs);
//		// allow null here
//		return value;
//	}


	private double R(GroundedTask task, HashableState hs) {
		return getStoredReward(task, hs);
	}

	private double P(GroundedTask task, HashableState hs, HashableState hsPrime) {
		return getStoredTransitionProbability(task, hs, hsPrime);
	}

	private double getStoredReward(GroundedTask task, HashableState hs) {
		HashMap<HashableState, Double> storedRewards = storedRewardsByTask.get(task);
		if (storedRewards == null) {
			storedRewards = new HashMap<>();
			storedRewardsByTask.put(task, storedRewards);
		}
		Double reward = storedRewards.get(hs);
		// don't store 0.0, represented sparsely
		if (reward == null) {
			return 0.0;
		} else {
			return reward;
		}
	}

	private double getStoredTransitionProbability(GroundedTask task, HashableState hs, HashableState hsPrime) {
		HashMap<HashableState,HashMap<HashableState, Double>> storedTransitions = storedTransitionsByTask.get(task);
		if (storedTransitions == null) {
			storedTransitions = new HashMap<>();
			storedTransitionsByTask.put(task, storedTransitions);
		}
		HashMap<HashableState, Double> transitionsFromState = storedTransitions.get(hs);
		if (transitionsFromState == null) {
			transitionsFromState = new HashMap<>();
			storedTransitions.put(hs, transitionsFromState);
		}

		Double transitionProbability = transitionsFromState.get(hsPrime);
		// don't store 0.0 transitions, represent them sparsely
		if (transitionProbability == null) {
			return 0.0;
		}
		return transitionProbability;
	}

	/**
	 * runs eqn 4 from RMAXQ paper on inputs
	 * @return the result of eqn 4
	 */
	private double equation_4(GroundedTask task, HashableState hs, GroundedTask childTask) {

		double childReward = R(childTask, hs);

		double expectedReward = 0.0;

		for (HashableState hsPrime : reachableStates) {
			if (isTerminal(task, hsPrime)) {
				continue;
			}
			double childTransitionProbability = P(childTask, hs, hsPrime);
			double parentReward = R(task, hsPrime);
			expectedReward += childTransitionProbability * parentReward;
		}

		return childReward + expectedReward;
	}

	/**
	 * runs eqn 5 from RMAXQ paper on inputs
	 * @return the result of eqn 5
	 */
	private double equation_5(GroundedTask task, HashableState hs, GroundedTask childTask, HashableState hsX) {

		double childTerminalTransitionProbability = P(childTask, hs, hsX);

		double expectedTransitionProbability = 0.0;

		for (HashableState hsPrime : reachableStates) {
			if (isTerminal(task, hsPrime)) {
				continue;
			}
			double childTransitionProbability = P(childTask, hs, hsPrime);
			double parentTerminalTransitionProbability = P(task, hsPrime, hsX);
			expectedTransitionProbability += childTransitionProbability * parentTerminalTransitionProbability;
		}

		return childTerminalTransitionProbability + expectedTransitionProbability;
	}
		
	/**
	 * add child task to action lookup
	 * @param task the current task
	 * @param hs the hashed current state
	 */
	protected void addChildTasks(GroundedTask task, HashableState hs){
		if(!task.isPrimitive()){
			State s = hs.s();
			List<GroundedTask> childGroundedTasks =  task.getGroundedChildTasks(s);

			for(GroundedTask gt : childGroundedTasks){
				Action action = gt.getAction();
				String taskName = getActionNameSafe(action);
				if(!taskNameToGroundedTask.containsKey(taskName)) {
					taskNameToGroundedTask.put(taskName, gt);
				}
			}
		} else {
			throw new RuntimeException("tying to add child tasks for primitive task?");
		}
	}

	/**
	 * use reachability analysis to get a list of terminalStatesByTask states
	 * @param task the task
	 * @return the terminalStatesByTask states
	 */
	protected List<HashableState> getTerminalStates(GroundedTask task){
		if (terminalStatesByTask.containsKey(task)) {
			return terminalStatesByTask.get(task);
		}
		List<HashableState> terminalStates = new ArrayList<>();
		for (HashableState hs : reachableStates) {
			if (isTerminal(task, hs)) {
				terminalStates.add(hs);
			}
		}
		terminalStatesByTask.put(task, terminalStates);
		if (terminalStates.size() < 1) {
			throw new RuntimeException("error: no terminal states for the given task");
		}
		return terminalStates;
	}
}