package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import config.taxi.TaxiConfig;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.RandomPassengerTaxiState;
import taxi.stateGenerator.TaxiStateFactory;

public class HierarchicalLearnerTest {

	public static void runRAMDPEpisodes(TaxiConfig conf, Task root, State initial, OOSADomain groundDomain) {
		List<Episode> episodes = new ArrayList<Episode>();
		GroundedTask rootgt = root.getAllGroundedTasks(initial).get(0);
		
		RAMDPLearningAgent ramdp = new RAMDPLearningAgent(rootgt, conf.rmax.threshold, conf.gamma, conf.rmax.vmax,
				new SimpleHashableStateFactory(true), conf.rmax.max_delta, conf.rmax.max_iterations_in_model);
		SimulatedEnvironment env;
		if(conf.stochastic.random_start) {
			env = new SimulatedEnvironment(groundDomain, new RandomPassengerTaxiState());
		} else {
			env = new SimulatedEnvironment(groundDomain, initial);
		}

		if(conf.output.visualizer.enabled) {
            VisualActionObserver obs = new VisualActionObserver(groundDomain, TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height));
			obs.initGUI();
			obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
			env.addObservers(obs);
		}
		
		for(int i = 1; i <= conf.episodes; i++){
			long time = System.currentTimeMillis();
			Episode e = ramdp.runLearningEpisode(env, conf.max_steps);
			time = System.currentTimeMillis() - time;
			episodes.add(e);
			System.out.println("Episode " + i + " time " + (double)time/1000 );
			env.resetEnvironment();
		}

		if(conf.output.visualizer.enabled) {
			EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
					(TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height), groundDomain, episodes);
			ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
			ev.initGUI();
		}
	}
	
	public static void runRMAXQEpsodes(TaxiConfig conf, Task root, State initState, OOSADomain domain){
		HashableStateFactory hs = new SimpleHashableStateFactory(true);
		
		SimulatedEnvironment env = new SimulatedEnvironment(domain, initState);

		if(conf.output.visualizer.enabled) {
			VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height));
			obs.initGUI();
			obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
			env.addObservers(obs);
		}

		List<Episode> episodes = new ArrayList<Episode>();
		RmaxQLearningAgent rmaxq = new RmaxQLearningAgent(root, hs, initState, conf.rmax.vmax, conf.rmax.threshold, conf.rmax.max_delta_rmaxq, conf.rmax.max_delta);
		
		for(int i = 1; i <= conf.episodes; i++){
			Episode e = rmaxq.runLearningEpisode(env, conf.max_steps);
			episodes.add(e);
			System.out.println("Episode " + i + " time " + rmaxq.getTime() / 1000.0);
			env.resetEnvironment();
		}

        EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
                (TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height), domain, episodes);
        ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
        ev.initGUI();
	}
	
	public static void main(String[] args) {
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
		if(conf.agents.contains("ramdp")) {
			Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle, false);
			OOSADomain base = TaxiHierarchy.getBaseDomain();
			runRAMDPEpisodes(conf, RAMDProot, s, base);
		}
		if(conf.agents.contains("ramdp")) {
			Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle);
			OOSADomain base = TaxiHierarchy.getBaseDomain();
			runRMAXQEpsodes(conf, RMAXQroot, s, base);
		}
	}
}
