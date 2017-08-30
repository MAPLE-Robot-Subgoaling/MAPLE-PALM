package testing;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import cleanup.CleanupVisualizer;
import cleanup.hierarchies.CleanupHierarchy;
import cleanup.state.CleanupRandomStateGenerator;
import cleanup.state.CleanupState;
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

public class CleanupHierarchicalCharts {

    public static void createCharts(final State s, OOSADomain domain, final Task RAMDPRoot,
                                    final double rmax, final int threshold, final double maxDelta, final double discount,
                                    int numEpisode, int maxSteps, int numTrial, int width, int height){
        final HashableStateFactory hs = new SimpleHashableStateFactory(true);
        final GroundedTask RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
        VisualActionObserver obs = new VisualActionObserver(domain, CleanupVisualizer.getVisualizer(width, height));
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
        exp.writeEpisodeDataToCSV("./ramdp-cleanup.csv");
    }

    public static void main(String[] args) {
        int maxSteps = 100;
        int rmaxThreshold = 5;
        int numTrials = 20;
        double rmax = 20;

        RandomFactory.seedMapped(0, 2320942930L);

        int minX = 0;
        int minY = 0;
        int maxX = 9;
        int maxY = 9;
        CleanupRandomStateGenerator sg = new CleanupRandomStateGenerator(minX, minY, maxX, maxY);

        String stateType = "threeRooms";
        int numBlocks = 2;
        CleanupState s = (CleanupState) sg.getStateFor(stateType, numBlocks);
        Task ramdpRoot = CleanupHierarchy.createAMDPHierarchy(minX, minY, maxX, maxY);
        OOSADomain base = CleanupHierarchy.getBaseDomain();
        HashableStateFactory hs = new SimpleHashableStateFactory(true);

        double gamma = 0.95;
        double maxDelta = 0.01;
        int maxRollouts = 1000;
        int numEpisodes = 2;
        int width = maxX - minX;
        int height = maxY - minY;
        createCharts(s, base, ramdpRoot, rmax, rmaxThreshold, maxDelta, gamma, numEpisodes, maxSteps, numTrials, width, height);
//		createRandomCharts(base, RAMDProot, rmax, rmaxThreshold, maxDelta, gamma, numEpisodes, maxSteps, numTrials);
    }
}
