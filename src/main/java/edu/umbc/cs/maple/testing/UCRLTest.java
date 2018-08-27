package edu.umbc.cs.maple.testing;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.cleanup.hierarchies.CleanupHierarchyAMDP;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.config.cleanup.CleanupConfig;
import edu.umbc.cs.maple.config.output.ChartConfig;
import edu.umbc.cs.maple.config.taxi.TaxiConfig;
import edu.umbc.cs.maple.hierarchy.framework.NonprimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiHierarchy;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiHierarchyExpert;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiHierarchyHierGen;
import edu.umbc.cs.maple.utilities.LearningAlgorithmExperimenter;

import javax.swing.*;
import java.util.List;

import static edu.umbc.cs.maple.testing.AgentType.*;
import static edu.umbc.cs.maple.testing.AgentType.Q_LEARNING;
import static edu.umbc.cs.maple.testing.AgentType.UCRL;
public class UCRLTest {

    public static void createCharts(final ExperimentConfig config, OOSADomain baseDomain, Task[] hierarchies, Task ucrlRoot, StateGenerator stateGenerator) {

        final Task expertRoot = hierarchies[0];

        SimulatedEnvironment env;
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
            } else if(agent.equals(UCRL.getType())) {
                ucrlRoot.setDomain(baseDomain);
                agents[i] = UCRL.generateLearningAgentFactory(ucrlRoot, config);
            }else if(agent.equals(PALM_UCRL.getType())){
                agents[i] = PALM_UCRL.generateLearningAgentFactory(ucrlRoot, config);
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

        exp.toggleVisualPlots(config.output.chart.enabled);
        if (config.output.chart.enabled) {
            exp.setUpPlottingConfiguration(cc.width, cc.height, cc.columns, cc.max_height, TrialMode.valueOf(cc.trial_mode), metrics);
        }

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

        TaxiConfig domain = (TaxiConfig) config.domain;
        Taxi taxiDomain;
        if(domain.fickle == 0){
            taxiDomain = new Taxi(false, domain.fickle, domain.correct_move);
        }else{
            taxiDomain = new Taxi(true, domain.fickle, domain.correct_move);
        }
        OOSADomain base = null;
        TaxiHierarchy ucrlHierarchy;
        Task ucrlRoot = null;
        Task[] hierarchies = new Task[2];
        if (config.domain instanceof TaxiConfig) {
            TaxiHierarchy expert = new TaxiHierarchyExpert();
            TaxiHierarchy hierGen = new TaxiHierarchyHierGen();
            ucrlHierarchy = new TaxiHierarchyExpert();
            Task expertRoot = expert.createHierarchy(config, false);
            Task hierGenRoot = hierGen.createHierarchy(config, false);
            ucrlRoot = ucrlHierarchy.createHierarchy(config, false);
            base = ucrlHierarchy.getBaseDomain();
            expert.setBaseDomain(base);
            expert.setBaseDomain(base);
            hierarchies[0] = expertRoot;
            hierarchies[1] = hierGenRoot;
        }
        createCharts(config, base, hierarchies, ucrlRoot, stateGenerator);

//        long estimatedTime = System.nanoTime() - startTime;
//        System.out.println("The estimated elapsed trial time is " + estimatedTime);
//        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
//        System.out.println("Estimated trial clock time elapsed: " + actualTimeElapsed);
    }

    public static void main(String[] args) {

        String configFile = "config/taxi/small-ucrl-jw.yaml";
        if(args.length > 0) {
            configFile = args[0];
        }

        ExperimentConfig config = ExperimentConfig.loadConfig(configFile);

        run(config);

    }
}
