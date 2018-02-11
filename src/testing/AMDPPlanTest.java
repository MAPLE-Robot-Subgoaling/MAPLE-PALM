package testing;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import amdp.planning.AMDPPlanner;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import config.taxi.TaxiConfig;
import hierarchy.framework.Task;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.TaxiStateFactory;

public class AMDPPlanTest {
	
	public static void plan(TaxiConfig conf, Task root, State init, HashableStateFactory hs, OOSADomain baseDomain) {
		
		AMDPPlanner amdp = new AMDPPlanner(root, conf.gamma, hs, conf.rmax.max_delta, conf.planning.rollouts, conf.max_steps);
		List<Episode> eps = new ArrayList<Episode>();

		for(int i = 0; i < conf.episodes; i++){
			Episode e = amdp.planFromState(init);
			eps.add(e);
			System.out.println()
			amdp.resetSolver();
		}

		EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
				(TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height), baseDomain, eps);;
		ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
		ev.initGUI();
	}
	
	public static void main(String[] args) {
		String conffile = "config/taxi/jwtest-classic-2passengers.yaml";
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

		if(conf.stochastic.seed > 0) {
			RandomFactory.seedMapped(0, conf.stochastic.seed);
			System.out.println("Using seed: " + conf.stochastic.seed);
		}

		TaxiState s = conf.generateState();
		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(conf.stochastic.correct_move, conf.stochastic.fickle, true);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
		plan(conf, RAMDProot, s, new SimpleHashableStateFactory(), base);
	}
}
