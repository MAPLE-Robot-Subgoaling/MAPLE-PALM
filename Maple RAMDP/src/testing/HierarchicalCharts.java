package testing;

import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.TaxiDomain;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import utilities.SimpleHashableStateFactory;

public class HierarchicalCharts {

	public static void createCrarts(State s, OOSADomain domain, Task RAMDPRoot, Task RMEXQRoot, 
			int rmax, int threshold, double maxDelta, double discount, int numEpisode, int numTrial){
		HashableStateFactory hs = new SimpleHashableStateFactory();
		GroundedTask RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0); 
		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
		VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(5, 5));
        obs.initGUI();
        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
        env.addObservers(obs);
		
		LearningAgentFactory rmaxq = new LearningAgentFactory() {
			
			@Override
			public String getAgentName() {
				return "R-MAXQ";
			}
			
			@Override
			public LearningAgent generateAgent() {
				return new RmaxQLearningAgent(RMEXQRoot, hs, s, rmax, threshold, maxDelta);
			}
		};
		
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
		
		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, numTrial, numEpisode, ramdp, rmaxq);
		exp.setUpPlottingConfiguration(900, 500, 2, 1000,
				TrialMode.MOST_RECENT_AND_AVERAGE,
				PerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE
				);
		
		exp.startExperiment();
		exp.writeEpisodeDataToCSV("C:\\Users\\mland\\Box Sync\\Maple\\hierarchical learning data\\ramdp full state data2.csv");
	}
	
	public static void main(String[] args) {
		boolean ficjle = false;
		TaxiState s = TaxiDomain.getClassicState(false);
		Task RAMDProot = TaxiHierarchy.createRAMDPHierarchy(s, ficjle);
		OOSADomain base = TaxiHierarchy.getGroundDomain();
		Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(s, ficjle);
		createCrarts(s, base, RAMDProot, RMAXQroot, 30, 3, 0.01, 0.9, 150, 5);
	}
}
