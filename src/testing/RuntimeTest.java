package testing;


import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.TaxiStateFactory;
import utilities.LearningAgentRuntimeAnalizer;

public class RuntimeTest {
	public static void createCrarts(final State s, OOSADomain domain, final Task RAMDPRoot, final Task RMEXQRoot,
									final double rmax, final int threshold, final double maxDelta, final double discount,
									int numEpisode, int maxSteps, int numTrial, int maxIterationsInModel){
		final HashableStateFactory hs = new SimpleHashableStateFactory(true);
		final GroundedTask RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
//		VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(5, 5));
//        obs.initGUI();
//        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
//        env.addObservers(obs);

		LearningAgentFactory rmaxq = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "R-MAXQ";
			}

			@Override
			public LearningAgent generateAgent() {
				throw new RuntimeException("not fixed on this branch");
//				return new RmaxQLearningAgent(RMEXQRoot, hs, s, rmax, threshold, maxDelta);
			}
		};

		LearningAgentFactory ramdp = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "R-AMDP";
			}

			@Override
			public LearningAgent generateAgent() {
				return new RAMDPLearningAgent(RAMDPGroot, threshold, discount, rmax, hs, maxDelta, maxIterationsInModel);
			}
		};

		LearningAgentRuntimeAnalizer timer = new LearningAgentRuntimeAnalizer(500, 300, ramdp, rmaxq);
		timer.runRuntimeAnalysis(numTrial, numEpisode, maxSteps, env);
		timer.writeDataToCSV("C:\\Users\\mland\\Box Sync\\Maple\\hierarchical learning data\\determintistic runtime ramdp");
	}


	public static void main(String[] args) {
		double correctMoveprob = 1;
		double fickleProb = 0;
		int numEpisodes = 30;
		int maxSteps = 2000;
		int rmaxThreshold = 1;
		int numTrials = 5;
		double gamma = 0.9;
		double rmax = 20;
		double maxDelta = 0.01;
		int maxIterationsInModel = 10000;

		TaxiState s = TaxiStateFactory.createTinyState();
		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(correctMoveprob, fickleProb,  false);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
		Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(correctMoveprob,  fickleProb);
		createCrarts(s, base, RAMDProot, RMAXQroot, rmax, rmaxThreshold, maxDelta, gamma,
				numEpisodes, maxSteps, numTrials, maxIterationsInModel);
	}
}