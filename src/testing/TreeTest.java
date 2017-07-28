package testing;

import action.models.CreateActionModels;
import action.models.TrajectoryGengerator;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import taxi.Taxi;
import taxi.stateGenerator.RandomPassengerTaxiState;

import java.util.List;

public class TreeTest {

	public static void main(String[] args) {
		
		Taxi taxi = new Taxi();
		OOSADomain domain = taxi.generateDomain();
		StateGenerator randomPasseger = new RandomPassengerTaxiState();
		double gamma = 0.9;
		HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		double maxDelta = 0.01;
		int maxIterations = 100;
		int numTrajectories = 50;

		List<Episode> trajectories = TrajectoryGengerator.generateTrajectories(randomPasseger, numTrajectories, domain,
				gamma, hashingFactory, maxDelta, maxIterations);
//        EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
//                (TaxiVisualizer.getVisualizer(5, 5), domain, trajectories);
//        ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
//        ev.initGUI();
			
		CreateActionModels.createModels(trajectories);
	}

}
