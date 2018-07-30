package edu.umbc.cs.maple.testing;

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
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.config.hierarchy.HierarchyConfig;
import edu.umbc.cs.maple.config.output.ChartConfig;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.utilities.LearningAlgorithmExperimenter;

import javax.swing.*;
import java.util.*;

public class HierarchicalCharts {


    private static void createCharts(final ExperimentConfig config, OOSADomain baseDomain, StateGenerator stateGenerator) {
        SimulatedEnvironment env;

        env = new SimulatedEnvironment(baseDomain, stateGenerator);

        if(config.output.visualizer.enabled) {
            VisualActionObserver obs = new VisualActionObserver(baseDomain, config.getVisualizer(config), 550, 550);
            obs.setFrameDelay(0);
            obs.initGUI();
            obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
            env.addObservers(obs);
        }

        List<LearningAgentFactory> agents = new LinkedList<>();
        Map<String, Task> hierarchyMap = new HashMap<>();
        for(String agentType : config.agents.keySet()){
            for (String hierarchy : (LinkedHashSet<String>)config.agents.get(agentType)){
                if(! (hierarchyMap.keySet().contains(hierarchy))){
                    HierarchyConfig hierarchyConfig = HierarchyConfig.load(config,"config/hierarchy/"+hierarchy+".yaml");
                    hierarchyMap.put(hierarchy, hierarchyConfig.getRoot(config));
                }
                String agentName = hierarchy+"-"+agentType;
                LearningAgentFactory factory = AgentType.generateLearningAgentFactory(hierarchyMap.get(hierarchy), config, agentType, agentName);
                agents.add(factory);
            }
        }

        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, config.trials, config.episodes, config.max_steps, agents.toArray(new LearningAgentFactory[agents.size()]));
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
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer(config.getVisualizer(config), baseDomain, episodes, 550, 550);
            ev.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            ev.initGUI();
        }
    }

    public static void run(ExperimentConfig config) {
        State source = config.generateState();
        StateGenerator stateGenerator = new ConstantStateGenerator(source);
        createCharts(config, (OOSADomain) config.baseDomain, stateGenerator);
    }

    public static void main(String[] args) {

        String configFile = "config/taxi/classic-2-fickle.yaml";
        if(args.length > 0) {
            configFile = args[0];
        }

        ExperimentConfig config = ExperimentConfig.loadConfig(configFile);

        run(config);

    }
}