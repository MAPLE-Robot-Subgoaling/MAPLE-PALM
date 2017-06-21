package testing;

import java.util.ArrayList;
import java.util.List;

import amdp.planning.AMDPPlanner;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.Task;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;
import taxi.state.TaxiStateFactory;

public class AMDPPlanTest {
	
	public static void plan(Task root, State init, HashableStateFactory hs, OOSADomain baseDomain,
			double gamma, double maxDelta, int maxRollouts){
		
		AMDPPlanner amdp = new AMDPPlanner(root, gamma, hs, maxDelta, maxRollouts);
		Episode e = amdp.planFromState(init);
		List<Episode> eps = new ArrayList<Episode>();
		eps.add(e);
		
		EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
				(TaxiVisualizer.getVisualizer(5, 5), baseDomain, eps);;
		ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
		ev.initGUI();
	}
	
	public static void main(String[] args) {
		double correctMoveprob = 0.8;
		double fickleProb = 0.5;
		double gamma = 0.9;
		double maxDelta = 0.01;
		int maxRollouts = 1000;
		
		TaxiState s = TaxiStateFactory.createClassicState();
		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(correctMoveprob, fickleProb);
		OOSADomain base = TaxiHierarchy.getBaseDomain();
		plan(RAMDProot, s, new SimpleHashableStateFactory(), base, gamma, maxDelta, maxRollouts);
	}
}
