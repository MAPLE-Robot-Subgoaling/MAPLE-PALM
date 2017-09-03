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
import config.cleanup.CleanupConfig;
import config.output.ChartConfig;
import config.taxi.TaxiConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.stateGenerator.RandomPassengerTaxiState;
import utilities.LearningAlgorithmExperimenter;

import java.io.FileNotFoundException;

//import utilities.SimpleHashableStateFactory;

public class CleanupHierarchicalCharts {

    public static void createCharts(final CleanupConfig conf, final State s, OOSADomain domain, final Task RAMDPRoot){ //}, final Task RMEXQRoot ){
        SimulatedEnvironment env;
        final HashableStateFactory hs;
        final GroundedTask RAMDPGroot;


        env = new SimulatedEnvironment(domain, s);
        RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0);

        hs = new SimpleHashableStateFactory(true);

        if(conf.output.visualizer.enabled) {
            int width = conf.maxX - conf.minX;
            int height = conf.maxY - conf.minY;
            VisualActionObserver obs = new VisualActionObserver(domain, CleanupVisualizer.getVisualizer(width, height), conf.output.visualizer.width, conf.output.visualizer.height);
            obs.initGUI();
            obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
            env.addObservers(obs);
        }
        // Loop to keep order of agents defined in YAML
        LearningAgentFactory[] agents = new LearningAgentFactory[conf.agents.size()];
        for(int i = 0; i < conf.agents.size(); i++) {
            String agent = conf.agents.get(i);

            // RAMDP
            if(agent.equals("ramdp")) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return "R-AMDP";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        return new RAMDPLearningAgent(RAMDPGroot, conf.rmax.threshold, conf.gamma, conf.rmax.vmax, hs, conf.rmax.max_delta);
                    }
                };
            }

            // RMAX
//            if(agent.equals("rmaxq")) {
//                agents[i] = new LearningAgentFactory() {
//                    @Override
//                    public String getAgentName() {
//                        return "R-MAXQ";
//                    }
//
//                    @Override
//                    public LearningAgent generateAgent() {
//                        return new RmaxQLearningAgent(RMEXQRoot, hs, s, conf.rmax.vmax, conf.rmax.threshold, conf.rmax.max_delta);
//                    }
//                };
//            }
        }

        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, conf.trials, conf.episodes, conf.max_steps, agents);
        if(conf.output.chart.enabled) {
            ChartConfig cc = conf.output.chart;

            PerformanceMetric[] metrics = new PerformanceMetric[cc.metrics.size()];
            for(int i = 0; i < cc.metrics.size(); i++) {
                metrics[i] = PerformanceMetric.valueOf(cc.metrics.get(i));
            }

            exp.setUpPlottingConfiguration(cc.width, cc.height, cc.columns, cc.max_height,
                    TrialMode.valueOf(cc.trial_mode), metrics
            );
        }

        exp.startExperiment();
        if(conf.output.csv.enabled) {
            exp.writeEpisodeDataToCSV(conf.output.csv.output);
        }
    }

    public static void main(String[] args) {

        String conffile = "config/cleanup/3rooms2blocks.yaml";
        if(args.length > 0) {
            conffile = args[0];
        }

        CleanupConfig config = new CleanupConfig();
        try {
            System.out.println("Using configuration: " + conffile);
            config = CleanupConfig.load(conffile);
        } catch (FileNotFoundException ex) {
            System.err.println("Could not find configuration file");
            System.exit(404);
        }

        CleanupState initialState = config.generateState();
        Task ramdpRoot = CleanupHierarchy.createAMDPHierarchy(config);
        OOSADomain baseDomain = CleanupHierarchy.getBaseDomain();
        createCharts(config, initialState, baseDomain, ramdpRoot);
//
//        int minX = 0;
//        int minY = 0;
//        int maxX = 5;
//        int maxY = 5;
//        CleanupRandomStateGenerator sg = new CleanupRandomStateGenerator(minX, minY, maxX, maxY);
//
//        String stateType = "threeRooms";//"threeRooms";
//        int numBlocks = 2;
//        CleanupState s = (CleanupState) sg.getStateFor(stateType, numBlocks);
//        Task ramdpRoot = CleanupHierarchy.createAMDPHierarchy(minX, minY, maxX, maxY);
//        OOSADomain base = CleanupHierarchy.getBaseDomain();
//        HashableStateFactory hs = new SimpleHashableStateFactory(true);
//
//        int maxSteps = 1000;
//        int rmaxThreshold = 1;
//        int numTrials = 2;
//        double rmax = 1000;
//        double gamma = 0.95;
//        double maxDelta = 0.001;
//        int numEpisodes = 100;
//        int width = maxX - minX;
//        int height = maxY - minY;
//        createCharts(s, base, ramdpRoot, rmax, rmaxThreshold, maxDelta, gamma, numEpisodes, maxSteps, numTrials, width, height);
//		createRandomCharts(base, RAMDProot, rmax, rmaxThreshold, maxDelta, gamma, numEpisodes, maxSteps, numTrials);



    }
}
