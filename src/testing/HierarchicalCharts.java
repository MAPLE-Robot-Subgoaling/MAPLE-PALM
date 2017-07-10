package testing;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.RandonPassengerTaxiState;
import taxi.stateGenerator.TaxiStateFactory;
//import utilities.SimpleHashableStateFactory;
import utilities.LearningAlgorithmExperimenter;

public class HierarchicalCharts {

	public static void createCrarts(final State s, OOSADomain domain, final Task RAMDPRoot, final Task RMEXQRoot, 
			final double rmax, final int threshold, final double maxDelta, final double discount,
			int numEpisode, int maxSteps, int numTrial, boolean relearn, int relearnThreshold){
		final HashableStateFactory hs = new SimpleHashableStateFactory(true);
		final GroundedTask RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0); 
		

//		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
		SimulatedEnvironment env = new SimulatedEnvironment(domain, new RandonPassengerTaxiState());

		VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(5, 5));
        obs.initGUI();
        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
        env.addObservers(obs);

		LearningAgentFactory reamdp = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "RE-AMDP";
			}
			
			@Override
			public LearningAgent generateAgent() {
				return new RAMDPLearningAgent(RAMDPGroot, threshold, discount, rmax,
						new SimpleHashableStateFactory(true), maxDelta, relearn, relearnThreshold);
			}
		};
		
		LearningAgentFactory ramdp = new LearningAgentFactory() {
			
			@Override
			public String getAgentName() {
				return "R-AMDP";
			}
			
			@Override
			public LearningAgent generateAgent() {
				return  new RAMDPLearningAgent(RAMDPGroot, threshold, discount, rmax,
						new SimpleHashableStateFactory(true), maxDelta, false, 0);
			}
		};

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, numTrial, numEpisode, maxSteps, reamdp);
		exp.setUpPlottingConfiguration(500, 300, 2, 1000,
				TrialMode.MOST_RECENT_AND_AVERAGE,
				PerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE
				);
		
		exp.startExperiment();
		exp.writeEpisodeDataToCSV("C:\\Users\\mland\\Box Sync\\Maple\\hierarchical learning data\\ramdp full state fickle.csv");
	}
	
	public static void createRandomCrarts(OOSADomain domain, final Task RAMDPRoot, 
			final double rmax, final int threshold, final double maxDelta, final double discount,
			int numEpisode, int maxSteps, int numTrial){
		
		SimulatedEnvironment env = new SimulatedEnvironment(domain, new RandonPassengerTaxiState());
		
		final HashableStateFactory hs = new SimpleHashableStateFactory(true);
		final GroundedTask RAMDPGroot = RAMDPRoot.getAllGroundedTasks(env.currentObservation()).get(0); 
		
		VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(5, 5));
        obs.initGUI();
        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
        env.addObservers(obs);
		
		
		LearningAgentFactory ramdp = new LearningAgentFactory() {
			
			@Override
			public String getAgentName() {
				return "R-AMDP";
			}
			
			@Override
			public LearningAgent generateAgent() {
				return new RAMDPLearningAgent(RAMDPGroot, threshold, discount, rmax, hs, maxDelta);
			}
		};
		
		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, numTrial, numEpisode, maxSteps, ramdp);
		exp.setUpPlottingConfiguration(500, 300, 2, 1000,
				TrialMode.MOST_RECENT_AND_AVERAGE,
				PerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE
				);
		
		exp.startExperiment();
		exp.writeEpisodeDataToCSV("C:\\Users\\mland\\Box Sync\\Maple\\hierarchical learning data\\ramdp classic state fickle.csv");
	}
	
	public static void main(String[] args) {


		double correctMoveprob = 1;
		double fickleProb = .03;
		int numEpisodes = 400;
		int maxSteps = 1000;
		int rmaxThreshold = 2;
		double gamma = 0.9;
		double rmax = 20;
		double maxDelta = 0.01;
		int episodeRelearn = 200;
		int numTrials = 1;

		
		TaxiState s = TaxiStateFactory.createTinyState();
		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(correctMoveprob, fickleProb, false);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
//		Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(correctMoveprob, fickleProb);
		createCrarts(s, base, RAMDProot, RAMDProot, rmax, rmaxThreshold, maxDelta, gamma,
				numEpisodes, maxSteps, numTrials, true, episodeRelearn);
//		createRandomCrarts(base, RAMDProot, rmax, rmaxThreshold, maxDelta, gamma, numEpisodes, maxSteps, numTrials);
	}
}
