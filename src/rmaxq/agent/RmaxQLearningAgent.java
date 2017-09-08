package rmaxq.agent;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
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

	//R^a(s) for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, Double>> nonprimitiveRewardsByTask;

	//P^a(s, s') for non-primitive tasks
	private HashMap<GroundedTask, HashMap< HashableState, HashMap< HashableState, Double>>> nonprimitiveTransitionsByTask;

	//n(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Integer>> stateActionCountsByTask;

	//n(s,a,s')
	private HashMap<GroundedTask, HashMap< HashableState, HashMap<HashableState, Integer>>> totalTransitionsByTask;

	//r(s,a)
	private HashMap<GroundedTask, HashMap< HashableState, Double>> totalRewardsByTask;

	//grounded task map
	private HashMap<String, GroundedTask> taskNameToGroundedTask;

	//QProviders for each grounded task
	private HashMap<GroundedTask, QProviderRmaxQ> qProvider;

	//policies
	private HashMap<GroundedTask, SolverDerivedPolicy> qPolicy;

	//envelopesByTask(a)
	private HashMap<GroundedTask, List<HashableState>> envelopesByTask;

	//ta 
	private HashMap<GroundedTask, List<HashableState>> terminalStatesByTask;

	private HashMap<GroundedTask, Integer> actionTimesteps;

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
	private int timestep;
//	private boolean computePolicy = true;
//	private List<Double> prevEpisodeRewards;

	public RmaxQLearningAgent(Task root, HashableStateFactory hs, State initState, double vmax, int threshold, double maxDeltaInPolicy, double maxDeltaInModel){
		this.root = root;
		this.totalRewardsByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.nonprimitiveRewardsByTask = new HashMap<GroundedTask, HashMap<HashableState,Double>>();
		this.nonprimitiveTransitionsByTask = new HashMap<GroundedTask, HashMap<HashableState, HashMap<HashableState, Double>>>();
		this.stateActionCountsByTask = new HashMap<GroundedTask, HashMap<HashableState, Integer>>();
		this.qProvider = new HashMap<GroundedTask, QProviderRmaxQ>();
		this.envelopesByTask = new HashMap<GroundedTask, List<HashableState>>();
		this.totalTransitionsByTask = new HashMap<GroundedTask, HashMap<HashableState,HashMap<HashableState,Integer>>>();
		this.terminalStatesByTask = new HashMap<GroundedTask, List<HashableState>>();
		this.qPolicy = new HashMap<GroundedTask, SolverDerivedPolicy>();
		this.taskNameToGroundedTask = new HashMap<String, GroundedTask>();
		this.maxDeltaInPolicy = maxDeltaInPolicy;
		this.maxDeltaInModel = maxDeltaInModel;
		this.hashingFactory = hs;
		this.Vmax = vmax;
		this.threshold = threshold;
		this.initialState = initState;
		this.actionTimesteps = new HashMap<GroundedTask, Integer>();
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
		timestep = 0;
		actionTimesteps.clear();

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

	private SolverDerivedPolicy initializePolicyAndSolver(GroundedTask task) {
		QProviderRmaxQ qValues = qProvider.get(task);
		if (qValues == null) {
			qValues = new QProviderRmaxQ(hashingFactory, task);
			qProvider.put(task, qValues);
		}

		SolverDerivedPolicy policySetByTask = qPolicy.get(task);
		if (policySetByTask == null) {
			policySetByTask = new GreedyQPolicy();
			qPolicy.put(task, policySetByTask);
		}
		policySetByTask.setSolver(qValues);
		return policySetByTask;
	}

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
			SolverDerivedPolicy policySetByTask = initializePolicyAndSolver(task);
			while(!isTerminal(task, hs) && (timestep < maxSteps || maxSteps == -1)) {
//				if (computePolicy) {
				computePolicy(task, hs);
//				}

				Action maxqAction = policySetByTask.action(hs.s());
				String taskName = getActionNameSafe(maxqAction);
				if (taskName.contains("_")) {
					System.out.print(taskName +"\n\t");
					for (QValue qv : qProvider.get(task).qValues(hs.s())) {
						System.out.print(qv.a + " " + qv.q + "\n\t");
					}
					System.out.println("");
				} else {
					System.out.print(taskName);
					for (QValue qv : qProvider.get(task).qValues(hs.s())) {
						System.out.print(qv.a + " " + qv.q + ", ");
					}
					System.out.println("");
				}
				GroundedTask childTaskFromPolicy = taskNameToGroundedTask.get(taskName);
				if (childTaskFromPolicy == null) {
					addChildTasks(task, hs.s());
					childTaskFromPolicy = taskNameToGroundedTask.get(taskName);
				}
				//R pia(s') (s')
				e = R_MaxQ(hs, childTaskFromPolicy, e, maxSteps);
				State s = e.stateSequence.get(e.stateSequence.size() - 1);
				hs = hashingFactory.hashState(s);
			}
			System.out.println("\n");
			return e;
		}
	}

	private String getActionNameSafe(Action action) {
		String name = action.actionName();
		if (action instanceof ObjectParameterizedAction) {
			ObjectParameterizedAction opa = (ObjectParameterizedAction) action;
			name = action.actionName() + "_" + String.join("_",opa.getObjectParameters());
		}
		return name;
	}

	private void updateTotalReward(GroundedTask task, HashableState hs, double reward) {
		HashMap<HashableState, Double> taskTotalRewards = totalRewardsByTask.get(task);
		if (taskTotalRewards == null) {
			taskTotalRewards = new HashMap<>();
			totalRewardsByTask.put(task, taskTotalRewards);
		}
		Double totalReward = taskTotalRewards.get(hs);
		if (totalReward == null) {
			totalReward = 0.0; // right?
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

//	private void initializeProbabilities(GroundedTask task, HashableState hs, HashableState hsPrime) {
//		Map<HashableState, Map<HashableState,Double>> transitionProbabilities = transitionsByTask.get(task);
//		if(transitionProbabilities == null){
//			transitionProbabilities = new HashMap<HashableState, Map<HashableState,Double>>();
//			transitionsByTask.put(task, transitionProbabilities);
//		}
//		Map<HashableState, Double> transitionProbsFromS = transitionProbabilities.get(hs);
//		if(transitionProbsFromS == null){
//			transitionProbsFromS = new HashMap<HashableState, Double>();
//			transitionProbabilities.put(hs, transitionProbsFromS);
//		}
//		Double prob = transitionProbsFromS.get(hsPrime);
//		if(prob == null) {
//			transitionProbsFromS.put(hsPrime, 0.);
//		}
//	}

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

		//add pa(s, sprime) =0 in order to perform the update in compute model
//		initializeProbabilities(task, hs, hsPrime);

		timestep++;
		return e;
	}

	private List<HashableState> initializeEnvelopeForTask(GroundedTask task) {
		Integer aTime = actionTimesteps.get(task);
		if(aTime == null){
			aTime = 0;
			actionTimesteps.put(task, aTime);
		}

		List<HashableState> taskEnvelope = envelopesByTask.get(task);
		if(taskEnvelope == null){
			taskEnvelope = new ArrayList<HashableState>();
			envelopesByTask.put(task, taskEnvelope);
		}

		if(aTime < timestep){
			actionTimesteps.put(task, timestep);
			taskEnvelope.clear();
		}

		QProviderRmaxQ qp = qProvider.get(task);
		if(qp == null){
			qp = new QProviderRmaxQ(hashingFactory, task);
			qProvider.put(task, qp);
		}

		return taskEnvelope;
	}

	/**
	 * computes and updates action values for the state in the task
	 * @param task the current task
	 * @param hs current state
	 */
	public void computePolicy(GroundedTask task, HashableState hs){
		
		List<HashableState> taskEnvelope = initializeEnvelopeForTask(task);
		QProviderRmaxQ qp = qProvider.get(task);

		prepareEnvelope(task, hs);

		boolean converged = false;
		while(!converged){   
			double maxDelta = 0;
			for(HashableState hsPrime : taskEnvelope){

				List<GroundedTask> childTasks = task.getGroundedChildTasks(hsPrime.s());
				for(GroundedTask childTask : childTasks){
					double oldQ = qp.qValue(hsPrime.s(), childTask.getAction());
					double newQ = equation_1(qp, childTask, hsPrime);

					qp.update(hsPrime.s(), childTask.getAction(), newQ);

					if(Math.abs(oldQ - newQ) > maxDelta) {
						maxDelta = Math.abs(oldQ - newQ);
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

				for (HashableState hsPrime : reachableStates) {
					Double transitionProbability = P(childTask, hs, hsPrime);
					if (transitionProbability == null || transitionProbability > 0) {
						prepareEnvelope(task, hsPrime);
					}
				}

//				HashMap<HashableState,HashMap<HashableState, Double>> childTaskTransitions = transitionsByTask.get(childTask);
//				if (childTaskTransitions == null) {
//					childTaskTransitions = new HashMap<>();
//					transitionsByTask.put(childTask, childTaskTransitions);
//				}
//				HashMap<HashableState, Double> transitionsFromState = childTaskTransitions.get(hs);
//				if (transitionsFromState == null) {
//					transitionsFromState = new HashMap<>();
//					childTaskTransitions.put(hs, transitionsFromState);
//				}
//				for (HashableState hsPrime : transitionsFromState.keySet()) {
//					double transitionProbability = transitionsFromState.get(hsPrime);
//					if (transitionProbability > 0.0) {
//						prepareEnvelope(task, hsPrime); // note: it is envelope over TASK not the childTask
//					}
//				}
			}
		}
	}

	private void computeModelPrimitive(GroundedTask task, HashableState hs) {
		// not needed in new version, as R^a(s) and P^a(s,sPrime) are always just computed by equation 6 and 7
	}

//	private void initializeModelNonprimitive(GroundedTask task) {
//		QProviderRmaxQ qProvider = this.qProvider.get(task);
//		SolverDerivedPolicy taskPolicy = qPolicy.get(task);
//		if(taskPolicy == null){
//			taskPolicy = new GreedyQPolicy();
//			qPolicy.put(task, taskPolicy);
//		}
//		taskPolicy.setSolver(qProvider);
//
////		Map<HashableState, Map<HashableState,Double>> taskTransitions = transitionsByTask.get(task);
////		if(taskTransitions == null){
////			taskTransitions = new HashMap<HashableState, Map<HashableState,Double>>();
////			transitionsByTask.put(task, taskTransitions);
////		}
//
////		Map<HashableState, Double> taskRewards = totalRewardsByTask.get(task);
////		if(taskRewards == null){
////			taskRewards = new HashMap<HashableState, Double>();
////			totalRewardsByTask.put(task, taskRewards);
////		}
//
//		List<HashableState> taskEnvelope = envelopesByTask.get(task);
//		if(taskEnvelope == null){
//			taskEnvelope = new ArrayList<HashableState>();
//			envelopesByTask.put(task, taskEnvelope);
//		}
//	}

	private double equation_6(GroundedTask task, HashableState hs) {
		if (!task.isPrimitive()) {
			throw new RuntimeException("Error: tried to approximate reward (equation 6) on non-primitive task");
		}
		// only primitive tasks are allowed to be computed this way
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			HashMap<HashableState, Double> taskRewards = totalRewardsByTask.get(task);
			double totalReward = taskRewards.get(hs);
			double approximateReward = totalReward / (1.0 * stateActionCount);
			return approximateReward;
		} else {
			return Vmax;
		}
	}

	private double equation_7(GroundedTask task, HashableState hs, HashableState hsPrime) {
		double stateActionCount = n(task, hs);
		if (stateActionCount >= threshold) {
			HashMap<HashableState, HashMap<HashableState, Integer>> totalTransitions = totalTransitionsByTask.get(task);
			HashMap<HashableState, Integer> transitionsFromState = totalTransitions.get(hs);
			double countForThisTransition = transitionsFromState.get(hsPrime);
			double approximateTransitionProbability = countForThisTransition / (1.0 * stateActionCount);
			return approximateTransitionProbability;
		} else {
			return 0.0;
		}
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
			SolverDerivedPolicy taskPolicy = qPolicy.get(task);
			Action maxqAction = taskPolicy.action(hsPrime.s());
			String actionName = getActionNameSafe(maxqAction);
			GroundedTask childTask = taskNameToGroundedTask.get(actionName);
			// update rewards
			equation_4(task, hsPrime, childTask);
			List<HashableState> taskTerminalStates = terminalStatesByTask.get(task);
			for (HashableState taskTerminalState : taskTerminalStates) {
				// update transitions
				equation_5(task, hsPrime, taskTerminalState);
			}
		}
		return maxChange < maxDeltaInModel;
	}


//			GroundedTask nextChildTask = taskNameToGroundedTask.get(taskName);
//			if (nextChildTask == null) {
//				addChildTasks(task, hsPrime.s());
//				nextChildTask = taskNameToGroundedTask.get(taskName);
//			}
			//p pia(s') (s',.)
//					Map<HashableState, Map<HashableState,Double>> pfrompia = transitionsByTask.get(nextChildTask);
//					if(pfrompia == null){
//						pfrompia = new HashMap<HashableState, Map<HashableState,Double>>();
//						transitionsByTask.put(nextChildTask, pfrompia);
//					}
//					Map<HashableState, Double> childProbabilities = pfrompia.get(hsPrime);
//					if(childProbabilities == null){
//						childProbabilities = new HashMap<HashableState, Double>();
//						pfrompia.put(hsPrime, childProbabilities);
//					}

////					Map<HashableState, Double> taskRewards = totalRewardsByTask.get(task);
////					Double prevReward = taskRewards.get(hsPrime);
////					if(prevReward == null){
////						prevReward = Vmax;
////						taskRewards.put(hsPrime, prevReward);
////					}
////					double newReward = equation_4(task, taskPolicy, taskRewards, hsPrime, childProbabilities);
//
//					//find max change for value iteration
//					if(Math.abs(newReward - prevReward) > maxChange)
//						maxChange = Math.abs(newReward - prevReward);

//					Map<HashableState, Map<HashableState,Double>> taskTransitions = transitionsByTask.get(task);
//					Map<HashableState, Double> Pstosp = taskTransitions.get(hsPrime);
//					if(Pstosp == null){
//						Pstosp = new HashMap<HashableState, Double>();
//						taskTransitions.put(hsPrime, Pstosp);
//					}
//
//					//for all s in ta
//					// equation 7
//					List<HashableState> terminal = getTerminalStates(task);
//					for(HashableState hx : terminal){
//						//get current pa(s',x)
//						Double oldProb = Pstosp.get(hx);
//						if(oldProb == null){
//							oldProb = 0.;
//							Pstosp.put(hx, oldProb);
//						}
//
//						double newProb = equation_5(task, taskTransitions, childProbabilities, hx);
//
//						double delta = Math.abs(newProb - oldProb);
//						if(delta > maxChange) {
//							maxChange = delta;
//						}
//
//						//set pa(s',x)
//						Pstosp.put(hx, newProb);
//					}


	/**
	 * runs eqn 1 from RMAXQ paper on inputs
	 * @param qProvider action values
	 * @param childTask the action
	 * @param hsPrime the starting state
	 * @return the new q value
	 */
	private double equation_1(QProviderRmaxQ qProvider, GroundedTask childTask, HashableState hsPrime) {
		//Ra'(s')
		Double rewardForTask = R(childTask, hsPrime);

//		Map<HashableState, Map<HashableState,Double>> Pchildaction = transitionsByTask.get(childTask);
//		if(Pchildaction == null){
//			Pchildaction = new HashMap<HashableState, Map<HashableState,Double>>();
//			transitionsByTask.put(childTask, Pchildaction);
//		}
//////		Map<HashableState, Double> fromsp = Pchildaction.get(hsPrime);
//////		if(fromsp == null){
//////			fromsp = new HashMap<HashableState, Double>();
//////			Pchildaction.put(hsPrime, fromsp);
//////		}
////
////		double weightedQ = 0;
////		for(HashableState hspprime : fromsp.keySet()){
////			double value = qProvider.value(hspprime.s());
////			weightedQ += fromsp.get(hspprime) * value;
////		}
//		double newQ = rewardForTask + weightedQ;
//		return newQ;

		return rewardForTask;
	}

	private double R(GroundedTask task, HashableState hs) {
		if (task.isPrimitive()) {
			return equation_6(task, hs);
		} else {
			HashMap<HashableState, Double> nonprimitiveRewards = nonprimitiveRewardsByTask.get(task);
			if (nonprimitiveRewards == null) {
				nonprimitiveRewards = new HashMap<>();
				nonprimitiveRewardsByTask.put(task, nonprimitiveRewards);
			}
			Double reward = nonprimitiveRewards.get(hs);

			// allow null reward here
			return reward;

//			if (reward == null) {
//				reward = 0.0;
//				nonprimitiveRewards.put(hs, reward);
//			}
//			return reward;
		}
	}

	private double P(GroundedTask task, HashableState hs, HashableState hsPrime) {
		if (task.isPrimitive()) {
			return equation_7(task, hs, hsPrime);
		} else {
			HashMap<HashableState,HashMap<HashableState, Double>> nonprimitiveTransitions = nonprimitiveTransitionsByTask.get(task);
			if (nonprimitiveTransitions == null) {
				nonprimitiveTransitions = new HashMap<>();
				nonprimitiveTransitionsByTask.put(task, nonprimitiveTransitions);
			}
			HashMap<HashableState, Double> transitionsFromState = nonprimitiveTransitions.get(hs);
			if (transitionsFromState == null) {
				transitionsFromState = new HashMap<>();
				nonprimitiveTransitions.put(hs, transitionsFromState);
			}

			Double transitionProbability = transitionsFromState.get(hsPrime);

			// allow null transition
			return transitionProbability;
		}
	}

	/**
	 * runs eqn 4 from RMAXQ paper on inputs
	 * @return the result of eqn 4
	 */
	private double equation_4(GroundedTask task, HashableState hs, GroundedTask childTask) {

		double immediateReward = R(childTask, hs);

		double expectedReward = 0.0;

		for (HashableState hsPrime : reachableStates) {
			if (isTerminal(task, hsPrime)) {
				continue;
			}

		}


		for(HashableState hnext : childProbabilities.keySet()){
			//get Ra(nextstate)
			if(task.isComplete(hnext.s()) || task.isFailure(hnext.s()))
				continue;

			Double nextReward = rewtask.get(hnext);
			if(nextReward == null){
				nextReward = Vmax;
				rewtask.put(hnext, nextReward);
			}
			weightedReward += childProbabilities.get(hnext) * nextReward;
		}
//		double newReward = actionReward + weightedReward;
//		rewtask.put(hsprime, newReward);
//		return newReward;
		throw new RuntimeException(" not finished ");
	}

	/**
	 * runs eqn 5 from RMAXQ paper on inputs
	 * @param task the current task
	 * @param taskTransitionProbabilities the transitionsByTask function for the task
	 * @param childProbabilities transitions for subtask
	 * @param hashedTerminalState the terminalStatesByTask state to transitionsByTask into
	 * @return the result of eqn 5
	 */
	private double equation_5(GroundedTask task, Map<HashableState, Map<HashableState, Double>> taskTransitionProbabilities,
			Map<HashableState, Double> childProbabilities, HashableState hashedTerminalState) {

		Double childProbability = childProbabilities.get(hashedTerminalState);
		if(childProbability == null){
			childProbability = 0.;
			childProbabilities.put(hashedTerminalState, childProbability);
		}

		double weightedTransition = 0;
		//sum over all p pia(s) (s',.)
		for(HashableState hashedSPrime: childProbabilities.keySet()){
			if(task.isComplete(hashedSPrime.s()) || task.isFailure(initialState) || task.isFailure(hashedSPrime.s())) {
				continue;
			}

			double childProbStateToSPrime = childProbabilities.get(hashedSPrime);
			//pa (s'',x)
			Map<HashableState, Double> sPrimeToTerminalStateProbability = taskTransitionProbabilities.get(hashedSPrime);
			if(sPrimeToTerminalStateProbability == null){
				sPrimeToTerminalStateProbability = new HashMap<HashableState, Double>();
				taskTransitionProbabilities.put(hashedSPrime, sPrimeToTerminalStateProbability);
			}
			Double probSPrimeToTerminalState = sPrimeToTerminalStateProbability.get(hashedTerminalState);
			if(probSPrimeToTerminalState == null){
				probSPrimeToTerminalState = 0.;
				sPrimeToTerminalStateProbability.put(hashedTerminalState, probSPrimeToTerminalState);
			}

			weightedTransition += childProbStateToSPrime * probSPrimeToTerminalState;
		}
		double newProb = childProbability + weightedTransition;
		return newProb;
	}
		
	/**
	 * add child task to action lookup
	 * @param task the current task
	 * @param s the current state
	 */
	protected void addChildTasks(GroundedTask task, State s){
		if(!task.isPrimitive()){
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
	 * @param t the task
	 * @return the terminalStatesByTask states
	 */
	protected List<HashableState> getTerminalStates(GroundedTask t){
		if(terminalStatesByTask.containsKey(t))
			return terminalStatesByTask.get(t);
		List<HashableState> terminals = new ArrayList<HashableState>();
		for(HashableState s :reachableStates){
			if(t.isComplete(s.s()) || t.isFailure(s.s()))
				terminals.add(s);
		}

		terminalStatesByTask.put(t, terminals);
		if(terminals.size() == 0)
			throw new RuntimeException("no terminalStatesByTask");
		return terminals;
	}
}