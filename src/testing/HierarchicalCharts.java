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
import palm.rmax.agent.ExpertNavModelGenerator;
import palm.rmax.agent.PALMRmaxModelGenerator;
import rmaxq.agent.RmaxQLearningAgent;
import state.hashing.simple.CachedHashableStateFactory;
import taxi.hierarchies.TaxiHierarchy;
import taxi.hierarchies.TaxiHierarchyAMDP;
import taxi.hierarchies.TaxiHierarchyHierGen;
import taxi.hierarchies.TaxiHierarchyRMAXQ;
import utilities.LearningAlgorithmExperimenter;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//------------------------------------

//import utilities.SimpleHashableStateFactory;

public class HierarchicalCharts {

    public static HashableStateFactory initializeHashableStateFactory() {
        // use the hashable state factory that caches states
        return new CachedHashableStateFactory(false);
    }

    public static void createCharts(final ExperimentConfig conf, final State s, OOSADomain domain, Task[] hierarchies) {
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

        if(conf.output.visualizer.enabled) {
            VisualActionObserver obs = new VisualActionObserver(domain, conf.getVisualizer(conf));
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
        LearningAgentFactory[] agents = new LearningAgentFactory[conf.agents.size()];
        for(int i = 0; i < conf.agents.size(); i++) {
            String agent = conf.agents.get(i);

            // RAMDP
            if(agent.equals("palmExpert")) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return "PALM with expert AMDP";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        PALMRmaxModelGenerator modelGen = new PALMRmaxModelGenerator(conf.rmax.threshold,
                                conf.rmax.vmax,hs, conf.gamma, conf.rmax.use_multitime_model);
                        PALMLearningAgent agent = new PALMLearningAgent(palm, modelGen, hs, conf.rmax.max_delta,
                                conf.rmax.max_iterations_in_model);
                        return agent;
                    }
                };
            }
            if(agent.equals("palmExpertWithNavGiven")) {
                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return "PALM expert, given a Nav Model";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        PALMModelGenerator modelGen = new ExpertNavModelGenerator(conf.rmax.threshold,
                                conf.rmax.vmax,hs, conf.gamma, conf.rmax.use_multitime_model);
                        return new PALMLearningAgent(palmWithNav, modelGen, hs, conf.rmax.max_delta,
                                conf.rmax.max_iterations_in_model);
                    }
                };
            }
            if (agent.equals("palmHiergen")){

                agents[i] = new LearningAgentFactory() {

                    @Override
                    public String getAgentName() {
                        return "PALM with HierGen AMDP";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        PALMRmaxModelGenerator modelGen = new PALMRmaxModelGenerator(conf.rmax.threshold,
                                conf.rmax.vmax,hs, conf.gamma, conf.rmax.use_multitime_model);
                        return new PALMLearningAgent(palmHierGen, modelGen, hs, conf.rmax.max_delta,
                                conf.rmax.max_iterations_in_model);
                    }
                };
            }

            // RMAXQ
            if(agent.equals("rmaxq")) {
                agents[i] = new LearningAgentFactory() {
                    @Override
                    public String getAgentName() {
                        return "RMAXQ";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        return new RmaxQLearningAgent(rmaxq, hs, s, conf.rmax.vmax, conf.gamma, conf.rmax.threshold, conf.rmax.max_delta_rmaxq, conf.rmax.max_delta, conf.rmax.max_iterations_in_model);
                    }
                };
            }

            // RMAX with Hiergen
            if(agent.equals("rmaxq-h")) {
                agents[i] = new LearningAgentFactory() {
                    @Override
                    public String getAgentName() {
                        return "RMAXQ with Hiergen";
                    }

                    @Override
                    public LearningAgent generateAgent() {
                        HashableStateFactory hs = initializeHashableStateFactory();
                        return new RmaxQLearningAgent(rmaxqHierGen, hs, s, conf.rmax.vmax, conf.gamma, conf.rmax.threshold, conf.rmax.max_delta_rmaxq, conf.rmax.max_delta, conf.rmax.max_iterations_in_model);
                    }
                };
            }

            //QLearning
            if(agent.equals("qlearning")){
                agents[i] = new LearningAgentFactory(){
                    @Override
                    public String getAgentName(){
                        return "QLearning";
                    }

                    @Override
                    public LearningAgent generateAgent(){
                        HashableStateFactory hs = initializeHashableStateFactory();
                        return new QLearning(domain, conf.gamma, hs, 0.0, 0.1);
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
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer(conf.getVisualizer(conf), domain, episodes);;
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


        String conffile = "config/taxi/classic.yaml";
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
