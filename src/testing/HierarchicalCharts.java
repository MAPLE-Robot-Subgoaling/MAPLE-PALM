package testing;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import cleanup.hierarchies.CleanupHierarchyAMDP;
import cleanup.hierarchies.CleanupHierarchyHiergen;
import cleanup.hierarchies.CleanupHierarchyRMAXQ;
import config.ExperimentConfig;
import config.cleanup.CleanupConfig;
import config.output.ChartConfig;
import config.taxi.TaxiConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import palm.agent.PALMLearningAgent;
import palm.agent.PALMModelGenerator;
import palm.rmax.agent.ExpectedRmaxModelGenerator;
import palm.rmax.agent.ExpertNavModelGenerator;
import palm.rmax.agent.PALMRmaxModelGenerator;
import rmaxq.agent.RmaxQLearningAgent;
import state.hashing.cached.CachedHashableStateFactory;
import taxi.hierarchies.TaxiHierarchy;
import taxi.hierarchies.TaxiHierarchyAMDP;
import taxi.hierarchies.TaxiHierarchyHierGen;
import taxi.hierarchies.TaxiHierarchyRMAXQ;
import utilities.LearningAlgorithmExperimenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static testing.AgentType.*;

public class HierarchicalCharts {


    public static void createCharts(final ExperimentConfig config, OOSADomain baseDomain, Task[] hierarchies, StateGenerator stateGenerator) {
        SimulatedEnvironment env;
        final Task palmRoot = hierarchies[0];
        final Task rmaxqRoot = hierarchies[1];
        final Task hierGenRoot = hierarchies[2];

        env = new SimulatedEnvironment(baseDomain, stateGenerator);

        if(config.output.visualizer.enabled) {
            VisualActionObserver obs = new VisualActionObserver(baseDomain, config.getVisualizer(config));
            obs.setFrameDelay(0);
            obs.initGUI();
            obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
            env.addObservers(obs);
        }


        // Loop to keep order of agents defined in YAML
        LearningAgentFactory[] agents = new LearningAgentFactory[config.agents.size()];
        for(int i = 0; i < config.agents.size(); i++) {
            String agent = config.agents.get(i);

            if(agent.equals(PALM_EXPERT.getType())) {
                agents[i] = PALM_EXPERT.generateLearningAgentFactory(palmRoot, config);
            } else if(agent.equals(PALM_EXPERT_NAV_GIVEN.getType())) {
                agents[i] = PALM_EXPERT_NAV_GIVEN.generateLearningAgentFactory(palmRoot, config);
            } else if(agent.equals(PALM_HIERGEN.getType())){
                agents[i] = PALM_HIERGEN.generateLearningAgentFactory(palmRoot, config);
            } else if(agent.equals(RMAXQ_EXPERT.getType())) {
                agents[i] = .generateLearningAgentFactory(rmaxqRoot, config);
            } else if(agent.equals(RMAXQ_HIERGEN.getType())) {
                agents[i] = .generateLearningAgentFactory(rmaxqRoot, config);
            } else if(agent.equals(KAPPA_EXPERT.getType())) {
                agents[i] = .generateLearningAgentFactory(rmaxqRoot, config);
            } else if(agent.equals(KAPPA_HIERGEN.getType())) {
                agents[i] = .generateLearningAgentFactory(rmaxqRoot, config);
            } else if(agent.equals(Q_LEARNING.getType())){
                Task qLearningWrapper = new NonprimitiveTask(baseDomain);
                agents[i] = .generateLearningAgentFactory(qLearningWrapper, config);
            }
        }

        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, config.trials, config.episodes, config.max_steps, agents);
        if(config.output.chart.enabled) {
            ChartConfig cc = config.output.chart;

            PerformanceMetric[] metrics = new PerformanceMetric[cc.metrics.size()];
            for(int i = 0; i < cc.metrics.size(); i++) {
                metrics[i] = PerformanceMetric.valueOf(cc.metrics.get(i));
            }

            exp.setUpPlottingConfiguration(cc.width, cc.height, cc.columns, cc.max_height,
                    TrialMode.valueOf(cc.trial_mode), metrics
            );
        }

        List<Episode> episodes = exp.startExperiment();
        if(config.output.csv.enabled) {
            exp.writeEpisodeDataToCSV(config.output.csv.output);
        }

        if (config.output.visualizer.episodes){
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer(config.getVisualizer(config), baseDomain, episodes);;
            ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
            ev.initGUI();
        }
    }

    public static void run(ExperimentConfig config) {

        OOSADomain base;
        Task[] hierarchies = new Task[3];
        if (config.domain instanceof TaxiConfig) {
            TaxiHierarchy amdpHierarchy = new TaxiHierarchyAMDP();
            TaxiHierarchy rmaxqHierarchy = new TaxiHierarchyRMAXQ();
            TaxiHierarchy hierGenHierarchy = new TaxiHierarchyHierGen();
            Task rootAMDP = amdpHierarchy.createHierarchy(config, false);
            Task rootRMAXQ = rmaxqHierarchy.createHierarchy(config, false);
            Task hiergenRoot = hierGenHierarchy.createHierarchy(config, false);
            base = amdpHierarchy.getBaseDomain();
            amdpHierarchy.setBaseDomain(base);
            rmaxqHierarchy.setBaseDomain(base);
            hierGenHierarchy.setBaseDomain(base);
            hierarchies[0] = rootAMDP;
            hierarchies[1] = rootRMAXQ;
            hierarchies[2] = hiergenRoot;
        } else if (config.domain instanceof CleanupConfig) {
            CleanupHierarchyAMDP amdpHierarchy = new CleanupHierarchyAMDP();
            CleanupHierarchyRMAXQ rmaxqHierarchy= new CleanupHierarchyRMAXQ();
            CleanupHierarchyHiergen hierGenHierarchy = new CleanupHierarchyHiergen();
            Task palmRoot = amdpHierarchy.createHierarchy(config, false);
            Task rmaxqRoot = rmaxqHierarchy.createHierarchy(config, false);
//            Task hiergenRoot = hiergenHierarchy.createHiergenHierarchy(config);
            base = amdpHierarchy.getBaseDomain();
            amdpHierarchy.setBaseDomain(base);
            rmaxqHierarchy.setBaseDomain(base);
//            hierGenHierarchy.setBaseDomain(base);
            hierarchies[0] = palmRoot;
            hierarchies[1] = rmaxqRoot;
//            hierarchies[2] = hiergenRoot;
        } else {
            throw new RuntimeException("Error: unknown domain in config file");
        }

        //runtime
        //get the starting time from execution
        long actualTimeElapsed = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(actualTimeElapsed);
        System.out.println(sdf.format(resultdate));
        long startTime = System.nanoTime();
        System.out.println("Trial current nano time: " + startTime);

        createCharts(config, base, hierarchies);

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("The estimated elapsed trial time is " + estimatedTime);
        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
        System.out.println("Estimated trial clock time elapsed: " + actualTimeElapsed);
    }

    public static void main(String[] args) {

        String configFile = "config/taxi/classic.yaml";
        if(args.length > 0) {
            configFile = args[0];
        }

        ExperimentConfig config = ExperimentConfig.loadConfig(configFile);

        run(config);
    }
}
