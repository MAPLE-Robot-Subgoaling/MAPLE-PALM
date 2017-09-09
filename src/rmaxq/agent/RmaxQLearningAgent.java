package rmaxq.agent;

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
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;

import java.util.*;

public class RmaxQLearningAgent implements LearningAgent {

	//R^a(s) for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, Double>> storedRewardsByTask;

	//P^a(s, s') for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, HashMap< HashableState, Double>>> storedTransitionsByTask;

	private HashMap<GroundedTask, HashMap< HashableState, Double>> storedValueByTask;

	private HashMap<GroundedTask, HashMap< HashableState, HashMap <GroundedTask, Double>>> storedQValuesByTask;

	private HashMap<GroundedTask, HashMap< HashableState, GroundedTask>> storedPoliciesByTask;

	//n(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Integer>> stateActionCountsByTask;

	//n(s,a,s')
	private HashMap<GroundedTask, HashMap< HashableState, HashMap<HashableState, Integer>>> totalTransitionsByTask;

	//r(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Double>> totalRewardsByTask;

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

	public RmaxQLearningAgent(Task root, HashableStateFactory hs, State initState, double vmax, int threshold, double maxDeltaInPolicy, double maxDeltaInModel){
		this.root = root;
		this.hashingFactory = hs;
		this.initialState = initState;
		this.Vmax = vmax;
		this.threshold = threshold;
		this.maxDeltaInPolicy = maxDeltaInPolicy;
		this.maxDeltaInModel = maxDeltaInModel;
		this.totalRewardsByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.storedRewardsByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.storedValueByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.storedQValuesByTask = new HashMap<>();
		this.storedPoliciesByTask = new HashMap<>();
		this.storedTransitionsByTask = new HashMap<GroundedTask, HashMap<HashableState, HashMap<HashableState, Double>>>();
		this.stateActionCountsByTask = new HashMap<GroundedTask, HashMap<HashableState, Integer>>();
		this.envelopesByTask = new HashMap<GroundedTask, List<HashableState>>();
		this.totalTransitionsByTask = new HashMap<GroundedTask, HashMap<HashableState,HashMap<HashableState,Integer>>>();
		this.terminalStatesByTask = new HashMap<GroundedTask, List<HashableState>>();
		this.timestepsByTask = new HashMap<GroundedTask, Integer>();
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
		timestepsByTask.clear();

		time = System.currentTimeMillis();
		HashableState hs = hashingFactory.hashState(env.currentObservation());
		e = R_MaxQ(rootSolve, hs, e, maxSteps);
		time = System.currentTimeMillis() - time;

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

		if(task.isPrimitive()){
			e = executePrimitive(e, task, hs);
			return e;
		} else {
			do {

				computePolicy(task, hs);

				GroundedTask childTask = pi(task, hs);

				tabLevel += "\t";

				e = R_MaxQ(childTask, hs, e, maxSteps);

				tabLevel = tabLevel.substring(0, (tabLevel.length() - 1));

				State s = e.stateSequence.get(e.stateSequence.size() - 1);
				hs = hashingFactory.hashState(s);
			} while(!isTerminal(task, hs) && (numberPrimitivesExecuted < maxSteps || maxSteps == -1));
			System.out.println(tabLevel + "<<< " + task.getAction());
			return e;
		}
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

	/**
	 * computes and updates action values for the state in the task
	 * @param task the current task
	 * @param hs current state
	 */
	public void computePolicy(GroundedTask task, HashableState hs){

		// initialize timesteps
		Integer timesteps = timestepsByTask.get(task);
		if(timesteps == null){
			timesteps = 0;
			timestepsByTask.put(task, timesteps);
		}

		// initialize taskEnvelope
		List<HashableState> taskEnvelope = envelopesByTask.get(task);
		if(taskEnvelope == null){
			taskEnvelope = new ArrayList<HashableState>();
			envelopesByTask.put(task, taskEnvelope);
		}

		// clear task envelope if timestampForTask < total actual timesteps
		if (timestepsByTask.get(task) < numberPrimitivesExecuted) {
			timestepsByTask.put(task, numberPrimitivesExecuted);
			taskEnvelope.clear();
		}

		prepareEnvelope(task, hs);

		boolean converged = false;
		while(!converged){
			converged = doValueIteration(task, taskEnvelope);
		}
		setPi(task, hs);
	}

	private boolean doValueIteration(GroundedTask task, List<HashableState> taskEnvelope) {
		double maxDelta = 0.0;
		for(HashableState hsPrime : taskEnvelope){
			List<GroundedTask> childTasks = task.getGroundedChildTasks(hsPrime.s());
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

		double expectedValue = 0.0;
		for (HashableState hsPrime : reachableStates) {
			double childTransitionProbability = P(childTask, hs, hsPrime);
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
		if(!task.isPrimitive() && isTerminal(task, hs)) {
			newV = task.getReward(hs.s(), task.getAction(), hs.s());
		} else {
			List<GroundedTask> childTasks = task.getGroundedChildTasks(hs.s());
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
		List<GroundedTask> childTasks = task.getGroundedChildTasks(hs.s());
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
		HashMap<HashableState, GroundedTask> storedPolicies = storedPoliciesByTask.get(task);
		if (storedPolicies == null) {
			storedPolicies = new HashMap<>();
			storedPoliciesByTask.put(task, storedPolicies);
		}
		storedPolicies.put(hs, maxChildTask);
	}

	/**
	 * calculates and stores the possible states that can be reached from the current state
	 * @param task the task
	 * @param hs the state
	 */
	public void prepareEnvelope(GroundedTask task, HashableState hs){
		List<HashableState> envelope = envelopesByTask.get(task);
		if (isTerminal(task, hs)) {
			return; // skip terminal states since we can't "plan" beyond the terminal point of this task
		}
		if(!envelope.contains(hs)){
			envelope.add(hs);
			List<GroundedTask> childTasks = task.getGroundedChildTasks(hs.s());
			for(GroundedTask childTask : childTasks){

				computeModel(childTask, hs);

				Set<HashableState> possibleStates = getPossibleSuccessorStates(childTask, hs);
				if (possibleStates == null) { continue; }
				for (HashableState hsPrime : possibleStates) {
					double transitionProbability = P(childTask, hs, hsPrime);
					if (transitionProbability > 0) {
						prepareEnvelope(task, hsPrime);
					}
				}
			}
		}
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
			boolean converged = false;
			while(!converged){
				converged = doDynamicProgramming(task);
			}
		}
	}

	private boolean doDynamicProgramming(GroundedTask task) {
		List<HashableState> taskEnvelope = envelopesByTask.get(task);
		double maxDelta = 0.0;
		for(HashableState hsPrime : taskEnvelope) {
			double oldValue = V(task, hsPrime);
			// get the action (child / subtask) that would be selected by policy
			GroundedTask childTask = pi(task, hsPrime);
			// update rewards
			setR_eq4(task, hsPrime, childTask);
			List<HashableState> taskTerminalStates = terminalStatesByTask.get(task);
			if (taskTerminalStates == null) {
				taskTerminalStates = getTerminalStates(task);
				terminalStatesByTask.put(task, taskTerminalStates);
			}
			for (HashableState taskTerminalState : taskTerminalStates) {
				// update transitions
				setT_eq5(task, hsPrime, childTask, taskTerminalState);
			}
//			double newValue;
//			if (isTerminal(task, hsPrime)) {
//				newValue = 0.0;
//			} else {
//				List<GroundedTask> childTasks = task.getGroundedChildTasks(hsPrime.s());
//				for (GroundedTask childTask : childTasks) {
//
//				}
//			}
		}
		boolean converged = maxDelta < maxDeltaInModel;
		return converged;
	}
	private double setR_eq4(GroundedTask task, HashableState hs, GroundedTask childTask) {

		double oldR = R(task, hs);

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

		double newR = childReward + expectedReward;

		storeReward(task, hs, newR);

		double delta = Math.abs(newR - oldR);
		return delta;
	}

	private double setT_eq5(GroundedTask task, HashableState hs, GroundedTask childTask, HashableState hsX) {

		double oldP = P(task, hs, hsX);

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

		double newP = childTerminalTransitionProbability + expectedTransitionProbability;

		storeTransitionProbability(task, hs, hsX, newP);

		double delta = Math.abs(newP - oldP);
		return delta;
	}

	private void computeModelPrimitive(GroundedTask task, HashableState hs) {
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			setReward_eq6(task, hs);
			for (HashableState hsPrime : reachableStates) {
				setTransitionProbability_eq7(task, hs, hsPrime);
			}
		}
	}

	private void setReward_eq6(GroundedTask task, HashableState hs) {
		if (!task.isPrimitive()) {
			throw new RuntimeException("Error: tried to approximate reward (equation 6) on non-primitive task");
		}
		// only primitive tasks are allowed to be computed this way
		double reward;
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
		}  else {
			reward = Vmax; // never reached ?
		}
		storeReward(task, hs, reward);
	}

	private void setTransitionProbability_eq7(GroundedTask task, HashableState hs, HashableState hsPrime) {
		double transitionProbability;
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			HashMap<HashableState, HashMap<HashableState, Integer>> totalTransitions = totalTransitionsByTask.get(task);
			HashMap<HashableState, Integer> totalTransitionsFromState = totalTransitions.get(hs);
			Integer countForThisTransition = totalTransitionsFromState.get(hsPrime);
			if (countForThisTransition == null) { countForThisTransition = 0; }
			double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
			transitionProbability = approximateTransitionProbability;
		} else {
			transitionProbability = 0.0; // never reached?
		}
		storeTransitionProbability(task, hs, hsPrime, transitionProbability);
	}

	private void storeReward(GroundedTask task, HashableState hs, double newReward) {
		HashMap<HashableState, Double> storedRewards = storedRewardsByTask.get(task);
		if (storedRewards == null) {
			storedRewards = new HashMap<>();
			storedRewardsByTask.put(task, storedRewards);
		}
		storedRewardsByTask.get(task).put(hs, newReward);
	}

	private void storeTransitionProbability(GroundedTask task, HashableState hs, HashableState hsPrime, double transitionProbability) {
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
		transitionsFromState.put(hsPrime, transitionProbability);
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

	private double P(GroundedTask task, HashableState hs, HashableState hsPrime) {
		return getStoredTransitionProbability(task, hs, hsPrime);
	}

	private GroundedTask getStoredPolicy(GroundedTask task, HashableState hs) {
		HashMap<HashableState, GroundedTask> storedPolicies = storedPoliciesByTask.get(task);
		if (storedPolicies == null) {
			storedPolicies = new HashMap<>();
			storedPoliciesByTask.put(task, storedPolicies);
		}
		GroundedTask childTask = storedPolicies.get(hs);
		return childTask;
	}

	private double getStoredQ(GroundedTask task, HashableState hs, GroundedTask childTask) {
		HashMap<HashableState, HashMap<GroundedTask, Double>> storedQs = storedQValuesByTask.get(task);
		if (storedQs == null) {
			storedQs = new HashMap<>();
			storedQValuesByTask.put(task, storedQs);
		}
		HashMap<GroundedTask, Double> childTaskToQValue = storedQs.get(hs);
		if (childTaskToQValue == null) {
			childTaskToQValue = new HashMap<>();
			storedQs.put(hs, childTaskToQValue);
		}
		Double qValue = childTaskToQValue.get(childTask);
		if (qValue == null) {
			qValue = 0.0;
			childTaskToQValue.put(childTask, qValue);
		}
		return qValue;
	}

	private double getStoredValue(GroundedTask task, HashableState hs) {
		HashMap<HashableState, Double> storedValue = storedValueByTask.get(task);
		if (storedValue == null) {
			storedValue = new HashMap<>();
			storedValueByTask.put(task, storedValue);
		}
		Double value = storedValue.get(hs);
		if (value == null) {
			value = 0.0;
			storedValue.put(hs, value);
		}
		return value;
	}

	private double getStoredReward(GroundedTask task, HashableState hs) {
		HashMap<HashableState, Double> storedRewards = storedRewardsByTask.get(task);
		if (storedRewards == null) {
			storedRewards = new HashMap<>();
			storedRewardsByTask.put(task, storedRewards);
		}
		Double reward = storedRewards.get(hs);
		if (reward == null) {
			reward = Vmax;//0.0;
			storedRewards.put(hs, reward);
		}
		return reward;
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
		if (transitionProbability == null) {
			transitionProbability = 0.0;
			transitionsFromState.put(hsPrime, transitionProbability);
		}
		return transitionProbability;
	}

	private Set<HashableState> getPossibleSuccessorStates(GroundedTask task, HashableState hs) {

		HashMap<HashableState, HashMap<HashableState, Double>> transitions = storedTransitionsByTask.get(task);
		if (transitions == null) {
			return null;
		}
		HashMap<HashableState, Double> transitionsFromState = transitions.get(hs);
		if (transitionsFromState == null) {
			return null;
		}

		return  transitionsFromState.keySet();
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

}