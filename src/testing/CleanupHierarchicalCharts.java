package testing;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import cleanup.CleanupVisualizer;
import cleanup.hierarchies.CleanupHierarchyAMDP;
import cleanup.hierarchies.CleanupHierarchyRMAXQ;
import config.ExperimentConfig;
import config.cleanup.CleanupConfig;
import config.output.ChartConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import palm.agent.PALMLearningAgent;
import palm.rmax.agent.PALMRmaxModelGenerator;
import rmaxq.agent.RmaxQLearningAgent;
import state.hashing.simple.CachedHashableStateFactory;
import utilities.LearningAlgorithmExperimenter;

import java.io.FileNotFoundException;
import java.util.List;

//import utilities.SimpleHashableStateFactory;

public class CleanupHierarchicalCharts {

    public static void createCharts(final ExperimentConfig conf, final State s, OOSADomain domain, final Task RAMDPRoot, final Task RMAXQRoot ){
        SimulatedEnvironment env;
        final HashableStateFactory hs;
        final GroundedTask RAMDPGroundedRoot, RMAXQGroundedRoot;


        env = new SimulatedEnvironment(domain, s);
        RAMDPGroundedRoot = RAMDPRoot.getAllGroundedTasks(s).get(0);
        RMAXQGroundedRoot = RMAXQRoot.getAllGroundedTasks(s).get(0);

//        hs = new SimpleHashableStateFactory(true);
        hs = new CachedHashableStateFactory(true);

        CleanupConfig cleanup = (CleanupConfig) conf.domain;
        if(conf.output.visualizer.enabled) {
            int width = cleanup.maxX - cleanup.minX;
            int height = cleanup.maxY - cleanup.minY;
            VisualActionObserver obs = new VisualActionObserver(domain, CleanupVisualizer.getVisualizer(width, height), conf.output.visualizer.width, conf.output.visualizer.height);
            obs.setFrameDelay(1L);
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
                        PALMRmaxModelGenerator modelGen = new PALMRmaxModelGenerator(conf.rmax.threshold,
                                conf.rmax.vmax,hs, conf.gamma,conf.rmax.use_multitime_model);
                        return new PALMLearningAgent(RAMDPGroundedRoot, modelGen, hs, conf.rmax.max_delta,
                                conf.rmax.max_iterations_in_model);
                    }
                };
            }

            // RMAX
            if(agent.equals("rmaxq")) {
                agents[i] = new LearningAgentFactory() {
                    @Override
                    public String getAgentName() {
                        return "R-MAXQ";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        return new RmaxQLearningAgent(RMAXQGroundedRoot, hs, s, conf.rmax.vmax, conf.gamma, conf.rmax.threshold, conf.rmax.max_delta_rmaxq, conf.rmax.max_delta,conf.rmax.max_iterations_in_model);
                    }
                };
            }
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

        List<Episode> episodes = exp.startExperiment();
        if(conf.output.csv.enabled) {
            exp.writeEpisodeDataToCSV(conf.output.csv.output);
        }

        if (conf.output.visualizer.episodes){
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
                    (CleanupVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height), domain, episodes);
            ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
            ev.initGUI();
        }
    }

    public static void main(String[] args) {

        String conffile = "config/cleanup/jwtest.yaml";
        if(args.length > 0) {
            conffile = args[0];
        }

        ExperimentConfig config = new ExperimentConfig();
        try {
            System.out.println("Using configuration: " + conffile);
            config = ExperimentConfig.load(conffile);
        } catch (FileNotFoundException ex) {
            System.err.println("Could not find configuration file");
            System.exit(404);
        }

        State initialState = config.generateState();
        CleanupHierarchyAMDP ramdpHierarchy = new CleanupHierarchyAMDP();
        CleanupHierarchyRMAXQ rmaxqHierarchy = new CleanupHierarchyRMAXQ();
        Task ramdpRoot = ramdpHierarchy.createAMDPHierarchy(config);
        Task rmaxqRoot = rmaxqHierarchy.createRMAXQHierarchy(config);
        OOSADomain baseDomain = ramdpHierarchy.getBaseDomain();
        createCharts(config, initialState, baseDomain, ramdpRoot, rmaxqRoot);

    }
}
