package testing;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import config.output.ChartConfig;
import config.taxi.TaxiConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.RandomPassengerTaxiState;
import taxi.stateGenerator.TaxiStateFactory;
import utilities.LearningAlgorithmExperimenter;

import java.io.FileNotFoundException;

//import utilities.SimpleHashableStateFactory;

public class HierarchicalCharts {

	public static void createCharts(final TaxiConfig conf, final State s, OOSADomain domain, final Task RAMDPRoot, final Task RMEXQRoot, final Task hierGenRoot) {
		SimulatedEnvironment env;
		final HashableStateFactory hs;
		final GroundedTask RAMDPGroot, hierGenGroot;

		if(conf.stochastic.random_start) {
			env = new SimulatedEnvironment(domain, new RandomPassengerTaxiState());
			RAMDPGroot = RAMDPRoot.getAllGroundedTasks(env.currentObservation()).get(0);
			hierGenGroot = hierGenRoot.getAllGroundedTasks(env.currentObservation()).get(0);
		} else {
            env = new SimulatedEnvironment(domain, s);
			RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0);
			hierGenGroot = hierGenRoot.getAllGroundedTasks(s).get(0);
		}

		hs = new SimpleHashableStateFactory(true);

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
			if(agent.equals("hiergen")){
				agents[i] = new LearningAgentFactory() {

					@Override
					public String getAgentName() {
						return "HierGen R-AMDP";
					}

					@Override
					public LearningAgent generateAgent() {
						return new RAMDPLearningAgent(hierGenGroot, conf.rmax.threshold, conf.gamma, conf.rmax.vmax, hs, conf.rmax.max_delta);
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
						return new RmaxQLearningAgent(RMEXQRoot, hs, s, conf.rmax.vmax, conf.rmax.threshold, conf.rmax.max_delta, conf.rmax.max_delta_in_model);
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
		
		exp.startExperiment();
		if(conf.output.csv.enabled) {
			exp.writeEpisodeDataToCSV(conf.output.csv.output);
		}
	}

	public static void main(String[] args) {
		String conffile = "config/taxi/multipassenger.yaml";
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
		createCharts(conf, s, base, RAMDProot, RMAXQroot, hiergenRoot);
	}
}
