package testing;

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
import hierarchy.framework.Task;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.stateGenerator.TaxiStateFactory;

public class AMDPPlanTest {
	
	public static void plan(Task root, State init, HashableStateFactory hs, OOSADomain baseDomain,
			double gamma, double maxDelta, int maxRollouts, int numEpisodes){
		
		AMDPPlanner amdp = new AMDPPlanner(root, gamma, hs, maxDelta, maxRollouts);
		List<Episode> eps = new ArrayList<Episode>();

		for(int i = 0; i < numEpisodes; i++){
			eps.add(amdp.planFromState(init));
		}
		
		EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
				(TaxiVisualizer.getVisualizer(5, 5), baseDomain, eps);;
		ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
		ev.initGUI();
	}
	
	public static void main(String[] args) {
		double correctMoveprob = 1;
		double fickleProb = 0;

		double gamma = 0.9;
		double maxDelta = 0.01;
		int maxRollouts = 1000;
		int numEpisodes = 100;
		
		TaxiState s = TaxiStateFactory.createMultiState();
		RandomFactory.seedMapped(0, 2320942930L);

		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(correctMoveprob, fickleProb, true);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
		plan(RAMDProot, s, new SimpleHashableStateFactory(), base, gamma, maxDelta, maxRollouts, numEpisodes);
	}
}
