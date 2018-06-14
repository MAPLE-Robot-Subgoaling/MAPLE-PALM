package testing;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import cleanup.hierarchies.CleanupHierarchyAMDP;
import config.ExperimentConfig;
import config.cleanup.CleanupConfig;
import config.output.ChartConfig;
import config.taxi.TaxiConfig;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import taxi.hierarchies.TaxiHierarchy;
import taxi.hierarchies.TaxiHierarchyExpert;
import taxi.hierarchies.TaxiHierarchyHierGen;
import utilities.LearningAlgorithmExperimenter;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static testing.AgentType.*;

public class HierarchicalCharts {


    public static void createCharts(final ExperimentConfig config, OOSADomain baseDomain, Task[] hierarchies, StateGenerator stateGenerator) {
        SimulatedEnvironment env;
        final Task expertRoot = hierarchies[0];
        final Task hierGenRoot = hierarchies[1];

        env = new SimulatedEnvironment(baseDomain, stateGenerator);

        if(config.output.visualizer.enabled) {
            VisualActionObserver obs = new VisualActionObserver(baseDomain, config.getVisualizer(config));
            obs.setFrameDelay(0);
            obs.initGUI();
            obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
            env.addObservers(obs);
        }

        LearningAgentFactory[] agents = new LearningAgentFactory[config.agents.size()];
        for(int i = 0; i < config.agents.size(); i++) {
            String agent = config.agents.get(i);
            if(agent.equals(PALM_EXPERT.getType())) {
                agents[i] = PALM_EXPERT.generateLearningAgentFactory(expertRoot, config);
            } else if(agent.equals(PALM_EXPERT_NAV_GIVEN.getType())) {
                agents[i] = PALM_EXPERT_NAV_GIVEN.generateLearningAgentFactory(expertRoot, config);
            } else if(agent.equals(PALM_HIERGEN.getType())){
                agents[i] = PALM_HIERGEN.generateLearningAgentFactory(hierGenRoot, config);
            } else if(agent.equals(RMAXQ_EXPERT.getType())) {
                agents[i] = RMAXQ_EXPERT.generateLearningAgentFactory(expertRoot, config);
            } else if(agent.equals(RMAXQ_HIERGEN.getType())) {
                agents[i] = RMAXQ_HIERGEN.generateLearningAgentFactory(hierGenRoot, config);
            } else if(agent.equals(KAPPA_EXPERT.getType())) {
                agents[i] = KAPPA_EXPERT.generateLearningAgentFactory(expertRoot, config);
            } else if(agent.equals(KAPPA_HIERGEN.getType())) {
                agents[i] = KAPPA_HIERGEN.generateLearningAgentFactory(hierGenRoot, config);
            } else if(agent.equals(Q_LEARNING.getType())){
                Task qLearningWrapper = new NonprimitiveTask(baseDomain);
                agents[i] = Q_LEARNING.generateLearningAgentFactory(qLearningWrapper, config);
            }
        }

        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, config.trials, config.episodes, config.max_steps, agents);
        ChartConfig cc = config.output.chart;

        PerformanceMetric[] metrics = new PerformanceMetric[cc.metrics.size()];
        for(int i = 0; i < cc.metrics.size(); i++) {
            metrics[i] = PerformanceMetric.valueOf(cc.metrics.get(i));
        }

        exp.setUpPlottingConfiguration(cc.width, cc.height, cc.columns, cc.max_height, TrialMode.valueOf(cc.trial_mode), metrics);
        exp.toggleVisualPlots(config.output.chart.enabled);

        List<Episode> episodes = exp.startExperiment();
        if(config.output.csv.enabled) {
            exp.writeEpisodeDataToCSV(config.output.csv.output);
        }

        if (config.output.visualizer.episodes){
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer(config.getVisualizer(config), baseDomain, episodes);;
            ev.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            ev.initGUI();
        }
    }

    public static void run(ExperimentConfig config) {

        // TODO ? possibly update this to permit more variable state generation procedures
        State source = config.generateState();
        StateGenerator stateGenerator = new ConstantStateGenerator(source);

        OOSADomain base;
        Task[] hierarchies = new Task[2];
        if (config.domain instanceof TaxiConfig) {
            TaxiHierarchy expert = new TaxiHierarchyExpert();
            TaxiHierarchy hierGen = new TaxiHierarchyHierGen();
            Task expertRoot = expert.createHierarchy(config, false);
            Task hierGenRoot = hierGen.createHierarchy(config, false);
            base = expert.getBaseDomain();
            expert.setBaseDomain(base);
            expert.setBaseDomain(base);
            hierarchies[0] = expertRoot;
            hierarchies[1] = hierGenRoot;
        } else if (config.domain instanceof CleanupConfig) {
            CleanupHierarchyAMDP expert = new CleanupHierarchyAMDP();
//            CleanupHierarchyHierGen hierGen = new CleanupHierarchyHierGen();
            Task expertRoot = expert.createHierarchy(config, false);
//            Task hierGenRoot = hierGen.createHierarchy(config, false);
            base = expert.getBaseDomain();
            expert.setBaseDomain(base);
//            hierGen.setBaseDomain(base);
            hierarchies[0] = expertRoot;
//            hierarchies[1] = hierGenRoot;
        } else {
            throw new RuntimeException("Error: unknown domain in config file");
        }

//        //runtime
//        //get the starting time from execution
//        long actualTimeElapsed = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
//        Date resultdate = new Date(actualTimeElapsed);
//        System.out.println(sdf.format(resultdate));
//        long startTime = System.nanoTime();
//        System.out.println("Trial current nano time: " + startTime);

        createCharts(config, base, hierarchies, stateGenerator);

//        long estimatedTime = System.nanoTime() - startTime;
//        System.out.println("The estimated elapsed trial time is " + estimatedTime);
//        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
//        System.out.println("Estimated trial clock time elapsed: " + actualTimeElapsed);
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
