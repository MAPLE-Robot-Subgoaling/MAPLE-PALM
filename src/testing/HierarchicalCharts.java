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
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.RandomPassengerTaxiState;
import utilities.LearningAlgorithmExperimenter;

import java.io.FileNotFoundException;

//------------------------------------
import java.util.List;

//import utilities.SimpleHashableStateFactory;

public class HierarchicalCharts {

	public static HashableStateFactory initializeHashableStateFactory() {
		// use the hashable state factory that caches states
		return new CachedHashableStateFactory(false);
	}

	public static void createCharts(final TaxiConfig conf, final State s, OOSADomain domain, final Task RAMDPRoot, /*final Task RMAXQRoot,*/ final Task hierGenRoot) {
		SimulatedEnvironment env;
		final GroundedTask RAMDPGroot, hierGenGroot, RMAXQGroot;

		if(conf.stochastic.random_start) {
			env = new SimulatedEnvironment(domain, new RandomPassengerTaxiState());
			RAMDPGroot = RAMDPRoot.getAllGroundedTasks(env.currentObservation()).get(0);
			//RMAXQGroot = RMAXQRoot.getAllGroundedTasks(s).get(0);
			hierGenGroot = hierGenRoot.getAllGroundedTasks(env.currentObservation()).get(0);
		} else {
            env = new SimulatedEnvironment(domain, s);
			RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0);

			//RMAXQGroot = RMAXQRoot.getAllGroundedTasks(s).get(0);
			hierGenGroot = hierGenRoot.getAllGroundedTasks(s).get(0);
		}

		// new SimpleHashableStateFactory(false); //new CachedHashableStateFactory(true); // new SimpleHashableStateFactory(true);

		if(conf.output.visualizer.enabled) {
			VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height));
			obs.initGUI();
			obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
			env.addObservers(obs);
		}

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
						return new PALMLearningAgent(RAMDPGroot, modelGen, hs, conf.rmax.max_delta,
								conf.rmax.max_iterations_in_model);
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
						return new PALMLearningAgent(RAMDPGroot, modelGen, hs, conf.rmax.max_delta,
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
                        return new PALMLearningAgent(hierGenGroot, modelGen, hs, conf.rmax.max_delta,
                                conf.rmax.max_iterations_in_model);
					}
				};
			}

			/*
			// RMAXQ
			if(agent.equals("rmaxq")) {
				agents[i] = new LearningAgentFactory() {
					@Override
					public String getAgentName() {
						return "R-MAXQ";
					}

					@Override
					public LearningAgent generateAgent() {
						HashableStateFactory hs = initializeHashableStateFactory();
						return new RmaxQLearningAgent(RMAXQGroot, hs, s, conf.rmax.vmax, conf.gamma, conf.rmax.threshold, conf.rmax.max_delta_rmaxq, conf.rmax.max_delta, conf.rmax.max_iterations_in_model);
					}
				};
			}


			// RMAX with Hiergen
			if(agent.equals("rmaxq-h")) {
				agents[i] = new LearningAgentFactory() {
					@Override
					public String getAgentName() {
						return "R-MAXQ with Hiergen";
					}

					@Override
					public LearningAgent generateAgent() {
						HashableStateFactory hs = initializeHashableStateFactory();
						return new RmaxQLearningAgent(hierGenGroot, hs, s, conf.rmax.vmax, conf.gamma, conf.rmax.threshold, conf.rmax.max_delta_rmaxq, conf.rmax.max_delta, conf.rmax.max_iterations_in_model);
					}
				};
			}
			*/
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
            EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
                    (TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height), domain, episodes);;
            ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
            ev.initGUI();
        }
	}

	public static void main(String[] args) {
		
		//output for the console
		
		//runtime
		//get the starting time from execution
		long startTime = System.nanoTime();
		System.out.println("Trial start time: " + startTime);
		
		
		String conffile = "config/taxi/classic.yaml";
		if(args.length > 0) {
			conffile = args[0];
		}

		TaxiConfig conf = new TaxiConfig();
		try {
			System.out.println("Using configuration: " + conffile);
			conf = TaxiConfig.load(conffile);
		} catch (FileNotFoundException ex) {
			System.err.println("Could not find configuration file");
			System.exit(404);
		}

		TaxiState s = conf.generateState();
		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle, false);
		Task hiergenRoot = TaxiHierarchy.createHierGenHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
		Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle);
		createCharts(conf, s, base, RAMDProot, /*RMAXQroot,*/ hiergenRoot);
		
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("The estimated elapsed trial time is" + estimatedTime);
	}
}
