package testing;

import java.util.List;

import action.models.CreateActionModels;
import action.models.TrajectoryGengerator;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import taxi.Taxi;
import taxi.stateGenerator.RandomPassengerTaxiState;

public class TreeTest {

	public static void main(String[] args) {
		
		Taxi taxi = new Taxi();
		OOSADomain domain = taxi.generateDomain();
		StateGenerator randomPasseger = new RandomPassengerTaxiState();
		double gamma = 0.9;
		HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		double maxDelta = 0.01;
		int maxIterations = 100;

		List<Episode> trajectories = TrajectoryGengerator.generateTrajectories(randomPasseger, 10, domain,
				gamma, hashingFactory, maxDelta, maxIterations);
			
		CreateActionModels.createModels(trajectories);
	}

}
