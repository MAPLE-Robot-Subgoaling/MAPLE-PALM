package action.models;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;
import utilities.ValueIteration;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryGengerator {

	/**
	 * generate a certain number of trajectories on a domain using a q learner agent
	 * @param stateGen the collect of start states
	 * @param numTrajectories the number of trajectories to generate*
	 * @param domain the domain
	 * @param gamma the discount factor
	 * @param hashingFactory a state hashing factory
	 * @return the list of learned trajectories
	 */
	public static List<Episode> generateQLearnedTrajectories(StateGenerator stateGen, int numTrajectories,
															 SADomain domain, double gamma, HashableStateFactory hashingFactory){

		LearningAgent qlearner = new QLearning(domain,gamma, hashingFactory, 0,0.01);
		SimulatedEnvironment env = new SimulatedEnvironment(domain, stateGen);
		List<Episode> episodes = new ArrayList<Episode>();
		for(int i = 0; i < numTrajectories; i++){
			Episode e = qlearner.runLearningEpisode(env);
			episodes.add(e);
			env.resetEnvironment();
		}
		
		return episodes;
	}

	/**
	 * create a given number of trajectores from a value iteration agent
	 * @param stateGen the starting state set
	 * @param numTrajectories the number of trajectories to make
	 * @param domain the domain to plan in
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 * @param maxDelta the maximum allowed error for value iteration
	 * @param maxIterations the maximum number of vi iterations
	 * @return a list of planned trajectories
	 */
	public static List<Episode> generateVIPlannedTrajectories(StateGenerator stateGen, int numTrajectories,
															  SADomain domain, double gamma, HashableStateFactory hashingFactory,
															  double maxDelta, int maxIterations){
		List<Episode> trajectories = new ArrayList<Episode>();

		ValueIteration vi = new ValueIteration(domain, gamma, hashingFactory,maxDelta, maxIterations);

		for (int i = 0; i < numTrajectories; i++){
			State s = stateGen.generateState();
			Policy p = vi.planFromState(s);
			Episode e = PolicyUtils.rollout(p, s, domain.getModel());
			trajectories.add(e);
		}
		return trajectories;
	}
}
