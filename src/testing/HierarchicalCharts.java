package testing;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
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

//------------------------------------

//import utilities.SimpleHashableStateFactory;

public class HierarchicalCharts {

    public static HashableStateFactory initializeHashableStateFactory() {
        // use the hashable state factory that caches states
        return new CachedHashableStateFactory(false);
    }

    public static void createCharts(final ExperimentConfig config, final State s, OOSADomain domain, Task[] hierarchies) {
        SimulatedEnvironment env;
        final Task RAMDPRoot = hierarchies[0];
        final Task RMAXQRoot = hierarchies[1];
        final Task hierGenRoot = hierarchies[2];

        GroundedTask gRootPalm = null, gRootHierGen = null, gRootRMAXQ = null;

        env = new SimulatedEnvironment(domain, s);
        gRootPalm = RAMDPRoot.getAllGroundedTasks(s).get(0);
        gRootRMAXQ = RMAXQRoot.getAllGroundedTasks(s).get(0);
        if (hierGenRoot != null) {
            gRootHierGen = hierGenRoot.getAllGroundedTasks(s).get(0);
        }

        // new SimpleHashableStateFactory(false); //new CachedHashableStateFactory(true); // new SimpleHashableStateFactory(true);

        if(config.output.visualizer.enabled) {
            VisualActionObserver obs = new VisualActionObserver(domain, config.getVisualizer(config));
            obs.setFrameDelay(0);
            obs.initGUI();
            obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
            env.addObservers(obs);
        }

        final GroundedTask palm = gRootPalm;
        final GroundedTask palmWithNav = gRootPalm;
        final GroundedTask rmaxq = gRootRMAXQ;
        final GroundedTask palmHierGen = gRootHierGen;
        final GroundedTask rmaxqHierGen = gRootHierGen;

        // Loop to keep order of agents defined in YAML
        LearningAgentFactory[] agents = new LearningAgentFactory[config.agents.size()];
        for(int i = 0; i < config.agents.size(); i++) {
            String agent = config.agents.get(i);

            if(agent.equals(PALM_EXPERT.getType())) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return PALM_EXPERT.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        PALMRmaxModelGenerator modelGen = new PALMRmaxModelGenerator(config.rmax.threshold,
                                config.rmax.vmax,hs, config.gamma, config.rmax.use_multitime_model);
                        PALMLearningAgent agent = new PALMLearningAgent(palm, modelGen, hs, config.rmax.max_delta,
                                config.rmax.max_iterations_in_model, config.rmax.wait_for_children);
                        return agent;
                    }
                };
            }
            if(agent.equals(PALM_EXPERT_NAV_GIVEN.getType())) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return PALM_EXPERT_NAV_GIVEN.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        PALMModelGenerator modelGen = new ExpertNavModelGenerator(config.rmax.threshold,
                                config.rmax.vmax,hs, config.gamma, config.rmax.use_multitime_model);
                        return new PALMLearningAgent(palmWithNav, modelGen, hs, config.rmax.max_delta,
                                config.rmax.max_iterations_in_model, config.rmax.wait_for_children);
                    }
                };
            }
            if (agent.equals(PALM_HIERGEN.getType())){

                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return PALM_HIERGEN.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        PALMRmaxModelGenerator modelGen = new PALMRmaxModelGenerator(config.rmax.threshold,
                                config.rmax.vmax,hs, config.gamma, config.rmax.use_multitime_model);
                        return new PALMLearningAgent(palmHierGen, modelGen, hs, config.rmax.max_delta,
                                config.rmax.max_iterations_in_model, config.rmax.wait_for_children);
                    }
                };
            }

            // RMAXQ
            if(agent.equals(RMAXQ_EXPERT.getType())) {
                agents[i] = new LearningAgentFactory() {
                    @Override
                    public String getAgentName() {
                        return RMAXQ_EXPERT.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        return new RmaxQLearningAgent(rmaxq, hs, s, config.rmax.vmax, config.gamma, config.rmax.threshold, config.rmax.max_delta_rmaxq, config.rmax.max_delta, config.rmax.max_iterations_in_model);
                    }
                };
            }

            // RMAX with Hiergen
            if(agent.equals(RMAXQ_HIERGEN.getType())) {
                agents[i] = new LearningAgentFactory() {
                    @Override
                    public String getAgentName() {
                        return RMAXQ_HIERGEN.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        return new RmaxQLearningAgent(rmaxqHierGen, hs, s, config.rmax.vmax, config.gamma, config.rmax.threshold, config.rmax.max_delta_rmaxq, config.rmax.max_delta, config.rmax.max_iterations_in_model);
                    }
                };
            }
            if(agent.equals(KAPPA_EXPERT.getType())) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return KAPPA_EXPERT.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        ExpectedRmaxModelGenerator modelGen = new ExpectedRmaxModelGenerator(config.rmax.threshold,
                                config.rmax.vmax,hs, config.gamma);
                        PALMLearningAgent agent = new PALMLearningAgent(palm, modelGen, hs, config.rmax.max_delta,
                                config.rmax.max_iterations_in_model, config.rmax.wait_for_children);
                        return agent;
                    }
                };
            }
            if(agent.equals(KAPPA_HIERGEN.getType())) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return KAPPA_HIERGEN.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        ExpectedRmaxModelGenerator modelGen = new ExpectedRmaxModelGenerator(config.rmax.threshold,
                                config.rmax.vmax,hs, config.gamma);
                        PALMLearningAgent agent = new PALMLearningAgent(palmHierGen, modelGen, hs, config.rmax.max_delta,
                                config.rmax.max_iterations_in_model, config.rmax.wait_for_children);
                        return agent;
                    }
                };
            }
            //QLearning
            if(agent.equals(Q_LEARNING.getType())){
                agents[i] = new LearningAgentFactory(){
                    @Override
                    public String getAgentName(){
                        return Q_LEARNING.getPlotterDisplayName();
                    }

                    @Override
                    public LearningAgent generateAgent(){
                        HashableStateFactory hs = initializeHashableStateFactory();
                        return new QLearning(domain, config.gamma, hs, 0.0, 0.1);
                    }
                };

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
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer(config.getVisualizer(config), domain, episodes);;
            ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
            ev.initGUI();
        }
    }

    public static void main(String[] args) {

        //runtime
        //get the starting time from execution
        long actualTimeElapsed = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(actualTimeElapsed);
        System.out.println(sdf.format(resultdate));
        long startTime = System.nanoTime();
        System.out.println("Trial current nano time: " + startTime);


        String configFile = "config/taxi/classic.yaml";
        if(args.length > 0) {
            configFile = args[0];
            if (configFile.equals("ALL-TAXI")) {
                String configPathTaxi = "./config/taxi/";
                File folder = new File(configPathTaxi);
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles != null) {
                    for (File listOfFile : listOfFiles) {
                        if (listOfFile.isFile()) {
                            String name = listOfFile.getName();
                            String config = configPathTaxi + name;
                            System.out.println("\n\n*************************\nINITIALIZING: " + config +"\n*************************\n\n");
                            main(new String[]{config});
                        }
                    }
                }
                System.exit(33);
            }
        }

        ExperimentConfig config = new ExperimentConfig();
        try {
            System.out.println("Using configuration: " + configFile);
            config = ExperimentConfig.load(configFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Could not find configuration file");
            System.exit(404);
        }

        State s = config.generateState();

        OOSADomain base;
        Task[] hierarchies = new Task[3];
        if (config.domain instanceof TaxiConfig) {
            TaxiConfig domain = (TaxiConfig) config.domain;
            Task rootAMDP = new TaxiHierarchyAMDP().createHierarchy(domain.correct_move, domain.fickle, false);
            Task rootRMAXQ = new TaxiHierarchyRMAXQ().createHierarchy(domain.correct_move, domain.fickle, false);
            Task hiergenRoot = new TaxiHierarchyHierGen().createHierarchy(domain.correct_move, domain.fickle, false);
            base = TaxiHierarchy.getBaseDomain();
            hierarchies[0] = rootAMDP;
            hierarchies[1] = rootRMAXQ;
            hierarchies[2] = hiergenRoot;
        } else if (config.domain instanceof CleanupConfig) {
            CleanupHierarchyAMDP rootAMDP = new CleanupHierarchyAMDP();
            CleanupHierarchyRMAXQ rootRMAXQ= new CleanupHierarchyRMAXQ();
            CleanupHierarchyHiergen hiergenHierarchy = new CleanupHierarchyHiergen();
            Task ramdpRoot = rootAMDP.createHierarchy(config, false);
            Task rmaxqRoot = rootRMAXQ.createHierarchy(config, false);
//            Task hiergenRoot = hiergenHierarchy.createHiergenHierarchy(config);
            base = rootAMDP.getBaseDomain();
            hierarchies[0] = ramdpRoot;
            hierarchies[1] = rmaxqRoot;
//            hierarchies[2] = hiergenRoot;
        } else {
            throw new RuntimeException("Error: unknown domain in config file");
        }
        createCharts(config, s, base, hierarchies);

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("The estimated elapsed trial time is " + estimatedTime);
        actualTimeElapsed = System.currentTimeMillis() - actualTimeElapsed;
        System.out.println("Estimated trial clock time elapsed: " + actualTimeElapsed);
    }
}
