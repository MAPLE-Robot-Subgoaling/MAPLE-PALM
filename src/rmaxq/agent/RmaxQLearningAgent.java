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

	//Pa(s', x)
	private Map<GroundedTask, Map<HashableState, Map<HashableState, Double>>> transition;

	//Ra(s) 
	private Map<GroundedTask, Map<HashableState, Double>> reward;
	//pi a

	//r(s,a)
	private Map<HashableState, Map< GroundedTask, Double>> totalReward;

	//n(s,a)
	private Map<HashableState, Map< GroundedTask, Integer>> actionCount;

	//n(s,a,s')
	private Map<HashableState, Map< GroundedTask, Map<HashableState, Integer>>> resultingStateCount;

	//grounded task map
	private Map<String, GroundedTask> groundedTaskMap;

	//QProviders for each grounded task
	private Map<GroundedTask, QProviderRmaxQ> qProvider;

	//policies
	private Map<GroundedTask, SolverDerivedPolicy> qPolicy;

	//taskToEnvelope(a)
	private Map<GroundedTask, List<HashableState>> taskToEnvelope;

	//ta 
	private Map<GroundedTask, List<HashableState>> terminalStatesByTask;

	private Map<GroundedTask, Integer> actionTimesteps;
	
	private double dynamicPrgEpsilon;
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

	public RmaxQLearningAgent(Task root, HashableStateFactory hs, State initState, double vmax, int threshold, double maxDelta){
		this.root = root;
		this.reward = new HashMap<GroundedTask, Map<HashableState,Double>>();
		this.transition = new HashMap<GroundedTask, Map<HashableState, Map<HashableState, Double>>>();
		this.totalReward = new HashMap<HashableState, Map<GroundedTask,Double>>();
		this.actionCount = new HashMap<HashableState, Map<GroundedTask,Integer>>();
		this.qProvider = new HashMap<GroundedTask, QProviderRmaxQ>();
		this.taskToEnvelope = new HashMap<GroundedTask, List<HashableState>>();
		this.resultingStateCount = new HashMap<HashableState, Map<GroundedTask,Map<HashableState,Integer>>>();
		this.terminalStatesByTask = new HashMap<GroundedTask, List<HashableState>>();
		this.qPolicy = new HashMap<GroundedTask, SolverDerivedPolicy>();
		this.groundedTaskMap = new HashMap<String, GroundedTask>();
		this.dynamicPrgEpsilon = maxDelta;
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

	private boolean isTerminal(GroundedTask task, HashableState hs) {
		State s = hs.s();
		boolean terminal = task.isComplete(s)
				|| task.isFailure(s)
				|| rootSolve.isComplete(s);
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
				computePolicy(hs, task);
//				}

//				QProviderRmaxQ qProviderRmaxQ = qProvider.get(task);
//				List<QValue> qvs = qProviderRmaxQ.qValues(hs.s());
//				if (qvs.size() < 1) {
//					qProviderRmaxQ.qValues(hs.s());
//				}
//				Policy qP = qPolicy.get(task);
				Action maxqAction = policySetByTask.action(hs.s());
				String taskName = getActionNameSafe(maxqAction);
				GroundedTask childTaskFromPolicy = groundedTaskMap.get(taskName);
				if (childTaskFromPolicy == null) {
					addChildTasks(task, hs.s());
					childTaskFromPolicy = groundedTaskMap.get(taskName);
				}
				//R pia(s') (s')
				e = R_MaxQ(hs, childTaskFromPolicy, e, maxSteps);
				State s = e.stateSequence.get(e.stateSequence.size() - 1);
				hs = hashingFactory.hashState(s);
			}

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

	private void updateReward(GroundedTask task, HashableState hs, double newR) {
		Map<GroundedTask, Double> rewrd = totalReward.get(hs);
		if(rewrd == null){
			rewrd = new HashMap<GroundedTask, Double>();
			totalReward.put(hs, rewrd);
		}
		Double totR = rewrd.get(task);
		if(totR == null){
			totR = 0.;
		}
		totR = totR + newR;
		rewrd.put(task, totR);
	}

	private void updateActionCount(GroundedTask task, HashableState hs) {
		Map<GroundedTask, Integer> count = actionCount.get(hs);
		if (count == null) {
			count = new HashMap<GroundedTask, Integer>();
			actionCount.put(hs, count);
		}
		Integer sum = count.get(task);
		if (sum == null) {
			sum = 0;
		}
		sum = sum + 1;
		count.put(task, sum);
	}

	private void updateTransitionCount(GroundedTask task, HashableState hs, HashableState hsPrime) {
		Map<GroundedTask, Map<HashableState,Integer>> stateCountByTask = resultingStateCount.get(hs);
		if(stateCountByTask == null){
			stateCountByTask = new HashMap<GroundedTask, Map<HashableState,Integer>>();
			resultingStateCount.put(hs, stateCountByTask);
		}
		Map<HashableState, Integer> stateCount = stateCountByTask.get(task);
		if(stateCount == null){
			stateCount = new HashMap<HashableState, Integer>();
			stateCountByTask.put(task, stateCount);
		}
		Integer countTimesTakenTransition = stateCount.get(hsPrime);
		if(countTimesTakenTransition == null){
			countTimesTakenTransition = 0;
		}
		countTimesTakenTransition = countTimesTakenTransition + 1;
		stateCount.put(hsPrime, countTimesTakenTransition);
	}

//	private void initializeProbabilities(GroundedTask task, HashableState hs, HashableState hsPrime) {
//		Map<HashableState, Map<HashableState,Double>> transitionProbabilities = transition.get(task);
//		if(transitionProbabilities == null){
//			transitionProbabilities = new HashMap<HashableState, Map<HashableState,Double>>();
//			transition.put(task, transitionProbabilities);
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
		updateReward(task, hs, newReward);

		//n(s,a) ++
		updateActionCount(task, hs);

		//n(s,a,s')++
		updateTransitionCount(task, hs, hsPrime);

		//add pa(s, sprime) =0 in order to perform the update in compute model
//		initializeProbabilities(task, hs, hsPrime);

		timestep++;
		return e;
	}


	/**
	 * computes and updates action values for the state in the task
	 * @param hs current state
	 * @param task the current task
	 */
	public void computePolicy(HashableState hs, GroundedTask task){
		
		Integer aTime = actionTimesteps.get(task);
		if(aTime == null){
			aTime = 0;
			actionTimesteps.put(task, aTime);
		}
		
		List<HashableState> envolopA = taskToEnvelope.get(task);
		if(envolopA == null){
			envolopA = new ArrayList<HashableState>();
			taskToEnvelope.put(task, envolopA);
		}

		if(aTime < timestep){
			actionTimesteps.put(task, timestep);
			envolopA.clear();
		}
		prepareEnvolope(hs, task);

		QProviderRmaxQ qp = qProvider.get(task);
		if(qp == null){
			qp = new QProviderRmaxQ(hashingFactory, task);
			qProvider.put(task, qp);
		}

		boolean converged = false;
		while(!converged){   
			double maxDelta = 0;
			for(HashableState hsprime : envolopA){

				List<GroundedTask> ActionIns = task.getGroundedChildTasks(hsprime.s());
				for(GroundedTask a : ActionIns){
					double oldQ = qp.qValue(hsprime.s(), a.getAction());

					double newQ = epuation_1(qp, hsprime, a);
					qp.update(hsprime.s(), a.getAction(), newQ);

					if(Math.abs(oldQ - newQ) > maxDelta)
						maxDelta = Math.abs(oldQ - newQ);
				}
			}
			if(maxDelta < dynamicPrgEpsilon)
				converged = true;
		}
	}

	/**
	 * calculates and stores the possible states that can be reached from the current state 
	 * @param hs the state
	 * @param task the task
	 */
	public void prepareEnvolope(HashableState hs, GroundedTask task){
		List<HashableState> envelope = taskToEnvelope.get(task);
		if(!envelope.contains(hs)){
			envelope.add(hs);
			List<GroundedTask> ActionIns = task.getGroundedChildTasks(hs.s());
			for(GroundedTask a : ActionIns){
				compute_model(hs, a); 

				//get function forPa'(s, .)
				Map<HashableState, Map<HashableState,Double>> sToSPrimeToProbability = transition.get(a);
				if(sToSPrimeToProbability == null){
					sToSPrimeToProbability = new HashMap<HashableState, Map<HashableState,Double>>();
					transition.put(a, sToSPrimeToProbability);
				}
				Map<HashableState, Double> sPrimeToProbability = sToSPrimeToProbability.get(hs);
				if(sPrimeToProbability == null){
					sPrimeToProbability = new HashMap<HashableState, Double>();
					sToSPrimeToProbability.put(hs, sPrimeToProbability);
				}

				Collection<HashableState> nextStates = sPrimeToProbability.keySet();
				for(HashableState hsp : nextStates){
					if(sPrimeToProbability.get(hsp) > 0) {
						prepareEnvolope(hsp, task);
					}
				}
			}
		}
	}

	/**
	 * computes and stores reward and transition values for the task 
	 * @param hs the current state
	 * @param task the current task
	 */
	private void compute_model(HashableState hs, GroundedTask task){
		if(task.isPrimitive()){
			//n(s, a)
			Map<GroundedTask, Integer> scount = actionCount.get(hs);
			if(scount == null){
				scount = new HashMap<GroundedTask, Integer>();
				actionCount.put(hs, scount);
			}
			Integer n_sa = scount.get(task);
			if(n_sa == null){
				n_sa = 0;
				scount.put(task, 0);
			}			
			if(n_sa >= threshold){
				//r(s, a)
				Map<GroundedTask, Double> rewards = totalReward.get(hs);
				if(rewards == null){
					rewards = new HashMap<GroundedTask, Double>();
					totalReward.put(hs, rewards);
				}
				Double r_sa = rewards.get(task);
				if(r_sa == null){
					r_sa = 0.;
					rewards.put(task, r_sa);
				}			

				//set Ra(s) to r(s,a) / n(s,a)
				Map<HashableState, Double> Ra = reward.get(task);
				if(Ra == null){
					Ra = new HashMap<HashableState, Double>();
					reward.put(task, Ra);
				}
				double newR = (double)r_sa / n_sa;
				Ra.put(hs, newR);

				//get Pa(s, .)
				Map<HashableState, Map<HashableState,Double>> Ptask = transition.get(task);
				if(Ptask == null){
					Ptask = new HashMap<HashableState, Map<HashableState,Double>>();
					transition.put(task, Ptask);
				}
				Map<HashableState, Double> pas = Ptask.get(hs); 
				if(pas == null){
					pas = new HashMap<HashableState, Double>();
					Ptask.put(hs, pas);
				}

				Map<GroundedTask, Map<HashableState,Integer>> nS = resultingStateCount.get(hs);
				if(nS == null){
					nS = new HashMap<GroundedTask, Map<HashableState,Integer>>();
					resultingStateCount.put(hs, nS);
				}
				Map<HashableState, Integer> nSA = nS.get(task);
				if(nSA == null){
					nSA = new HashMap<HashableState, Integer>();
					nS.put(task, nSA);
				}
				for(HashableState hsprime : pas.keySet()){
					//get n(s, a, s')
					Integer n_sasp = nSA.get(hsprime);
					if(n_sasp == null){
						n_sasp = 0;
						nSA.put(hsprime, n_sasp);
					}

					//set Pa(s, s') = n(s,a,s') / n(s, a)
					double p_assp = (double)n_sasp / n_sa;
					pas.put(hsprime, p_assp);
				}
			}
		}else{
			computePolicy(hs, task);
			
			QProviderRmaxQ qvalues = qProvider.get(task);
			SolverDerivedPolicy taskFromPolicy = qPolicy.get(task);
			if(taskFromPolicy == null){
				taskFromPolicy = new GreedyQPolicy();
				qPolicy.put(task, taskFromPolicy);
			}
			taskFromPolicy.setSolver(qvalues);

			Map<HashableState, Map<HashableState,Double>> probtask = transition.get(task);
			if(probtask == null){
				probtask = new HashMap<HashableState, Map<HashableState,Double>>();
				transition.put(task, probtask);
			}					

			Map<HashableState, Double> rewtask = reward.get(task);
			if(rewtask == null){
				rewtask = new HashMap<HashableState, Double>();
				reward.put(task, rewtask);
			}
			
			List<HashableState> envelope = taskToEnvelope.get(task);
			if(envelope == null){
				envelope = new ArrayList<HashableState>();
				taskToEnvelope.put(task, envelope);
			}
			
			boolean converged = false;
			while(!converged){
				double maxChange = 0;
				//temporary holders for batch updates
				for(HashableState hsprime : envelope){
					Action maxqAction = taskFromPolicy.action(hsprime.s());
					String taskName = getActionNameSafe(maxqAction);
					GroundedTask childFromPolicy = groundedTaskMap.get(taskName);
					if(childFromPolicy == null){
						addChildTasks(task, hsprime.s());
						childFromPolicy = groundedTaskMap.get(taskName);
					}
					
					//p pia(s') (s',.)
					Map<HashableState, Map<HashableState,Double>> pfrompia = transition.get(childFromPolicy);
					if(pfrompia == null){
						pfrompia = new HashMap<HashableState, Map<HashableState,Double>>();
						transition.put(childFromPolicy, pfrompia);
					}
					Map<HashableState, Double> childProbabilities = pfrompia.get(hsprime);
					if(childProbabilities == null){
						childProbabilities = new HashMap<HashableState, Double>();
						pfrompia.put(hsprime, childProbabilities);
					}
					
					Double prevReward = rewtask.get(hsprime);
					if(prevReward == null){
						prevReward = Vmax;
						rewtask.put(hsprime, prevReward);
					}					
					double newReward = equation_4(task, taskFromPolicy, rewtask, hsprime, childProbabilities);

					//find max change for value iteration
					if(Math.abs(newReward - prevReward) > maxChange)
						maxChange = Math.abs(newReward - prevReward);

					Map<HashableState, Double> Pstosp = probtask.get(hsprime);
					if(Pstosp == null){
						Pstosp = new HashMap<HashableState, Double>();
						probtask.put(hsprime, Pstosp);
					}

					//for all s in ta
					// equation 7
					List<HashableState> terminal = getTerminalStates(task);
					for(HashableState hx :terminal){
						//get current pa(s',x)
						Double oldPrabability = Pstosp.get(hx);
						if(oldPrabability == null){
							oldPrabability = 0.;
							Pstosp.put(hx, oldPrabability);
						}

						double newProb = equation_5(task, probtask, childProbabilities, hx);

						double delta = Math.abs(newProb - oldPrabability);
						if(delta > maxChange) {
							maxChange = delta;
						}

						//set pa(s',x)
						Pstosp.put(hx, newProb);
					}
					if(maxChange < dynamicPrgEpsilon) {
						converged = true;
					}
				}
			}	
		}
	}

	/**
	 * runs eqn 1 from RMAXQ paper on inputs
	 * @param qp action values
	 * @param hsprime the starting state
	 * @param a the action
	 * @return the new q value
	 */
	private double epuation_1(QProviderRmaxQ qp, HashableState hsprime, GroundedTask a) {
		//Ra'(s')
		Map<HashableState, Double> actionAR = reward.get(a);
		if(actionAR == null){
			actionAR = new HashMap<HashableState, Double>();
			reward.put(a, actionAR);
		}
		Double actionReward = actionAR.get(hsprime);
		if(actionReward == null){
			actionReward = Vmax;
			actionAR.put(hsprime, actionReward);
		}

		Map<HashableState, Map<HashableState,Double>> Pchildaction = transition.get(a);
		if(Pchildaction == null){
			Pchildaction = new HashMap<HashableState, Map<HashableState,Double>>();
			transition.put(a, Pchildaction);
		}
		Map<HashableState, Double> fromsp = Pchildaction.get(hsprime);
		if(fromsp == null){
			fromsp = new HashMap<HashableState, Double>();
			Pchildaction.put(hsprime, fromsp);
		}

		double weightedQ = 0;
		for(HashableState hspprime : fromsp.keySet()){
			double value = qp.value(hspprime.s());
			weightedQ += fromsp.get(hspprime) * value;
		}
		double newQ = actionReward + weightedQ;
		return newQ;
	}

	/**
	 * runs eqn 4 from RMAXQ paper on inputs
	 * @param task current task
	 * @param taskFromPolicy the policy based on q values
	 * @param rewtask rewards for the task
	 * @param hsprime the start state
	 * @param childProbabilities transitions of the task
	 * @return the result of eqn 4
	 */
	private double equation_4(GroundedTask task, SolverDerivedPolicy taskFromPolicy, Map<HashableState, Double> rewtask,
			HashableState hsprime, Map<HashableState, Double> childProbabilities) {
		Action maxqAction = taskFromPolicy.action(hsprime.s());
		String taskName = getActionNameSafe(maxqAction);
		GroundedTask childFromPolicy = groundedTaskMap.get(taskName);
		if(childFromPolicy == null){
			addChildTasks(task, hsprime.s());
			childFromPolicy = groundedTaskMap.get(taskName);
		}

		//R pia(s') (s')
		Map<HashableState, Double> childrew = reward.get(childFromPolicy);
		if(childrew == null){
			childrew = new HashMap<HashableState, Double>();
			reward.put(childFromPolicy, childrew);
		}
		Double actionReward = childrew.get(hsprime);
		if(actionReward == null){
			actionReward = Vmax;
			childrew.put(hsprime, actionReward);
		}

		double weightedReward = 0;
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
		double newReward = actionReward + weightedReward;
		rewtask.put(hsprime, newReward);
		return newReward;
	}

	/**
	 * runs eqn 5 from RMAXQ paper on inputs
	 * @param task the current task
	 * @param taskTransitionProbabilities the transition function for the task
	 * @param childProbabilities transitions for subtask
	 * @param hashedTerminalState the terminalStatesByTask state to transition into
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
				if(!groundedTaskMap.containsKey(taskName)) {
					groundedTaskMap.put(taskName, gt);
				}
			}
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