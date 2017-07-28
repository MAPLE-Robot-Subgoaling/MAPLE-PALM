package action.models;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryGengerator {

	public static List<Episode> generateTrajectories(StateGenerator stateGen, int numTrajectories,
			 SADomain domain, double gamma, HashableStateFactory hashingFactory, Double maxDelta, int maxIterations){

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
}
