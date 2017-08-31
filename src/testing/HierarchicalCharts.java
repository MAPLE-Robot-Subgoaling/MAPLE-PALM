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
import org.yaml.snakeyaml.constructor.Constructor;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.RandonPassengerTaxiState;
import taxi.stateGenerator.TaxiStateFactory;
//import utilities.SimpleHashableStateFactory;
import utilities.LearningAlgorithmExperimenter;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class HierarchicalCharts {

	public static void createCharts(TaxiConfig conf, final State s, OOSADomain domain, final Task RAMDPRoot, final Task RMEXQRoot,
									final double rmax, final int threshold, final double maxDelta, final double discount,
									int numEpisode, int maxSteps, int numTrial){
		SimulatedEnvironment env;
		HashableStateFactory hs;
		GroundedTask RAMDPGroot;

		if(conf.stochastic.random_start) {
			env = new SimulatedEnvironment(domain, new RandonPassengerTaxiState());
			RAMDPGroot = RAMDPRoot.getAllGroundedTasks(env.currentObservation()).get(0);
		} else {
            env = new SimulatedEnvironment(domain, s);
			RAMDPGroot = RAMDPRoot.getAllGroundedTasks(s).get(0);
		}

		hs = new SimpleHashableStateFactory(true);

		VisualActionObserver obs;
		if(conf.output.visualizer.enabled) {
			obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height));
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
						return new RAMDPLearningAgent(RAMDPGroot, threshold, discount, rmax, hs, maxDelta);
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
						return new RmaxQLearningAgent(RMEXQRoot, hs, s, rmax, threshold, maxDelta);
					}
				};
			}
		}

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, numTrial, numEpisode, maxSteps, agents);
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
		Yaml yaml = new Yaml(new Constructor(TaxiConfig.class));
		TaxiConfig conf = new TaxiConfig();
		if(args.length < 1) {
			System.err.println("No configuration file specified");
			System.exit(404);
		}
		try {
			InputStream input = new FileInputStream(new File(args[0]));
			conf = (TaxiConfig) yaml.load(input);
		} catch (FileNotFoundException fnfex) {
			System.err.println("Could not find configuration file: " + args[0]);
			System.exit(404);
		}

		System.out.println("---- Configuration ----");
		System.out.println(yaml.dump(conf));

		TaxiState s = null;
		if(!conf.stochastic.random_start) {
			switch (conf.state) {
				case "tiny":
					s = TaxiStateFactory.createTinyState();
					break;
				case "small":
					s = TaxiStateFactory.createSmallState();
					break;
				default:
					s = TaxiStateFactory.createClassicState();
					break;
			}
		}

		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle, false);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
		Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle);
		createCharts(conf, s, base, RAMDProot, RMAXQroot, conf.rmax.vmax, conf.rmax.threshold, conf.rmax.max_delta, conf.gamma,
				conf.episodes, conf.max_steps, conf.trials);
	}
}
