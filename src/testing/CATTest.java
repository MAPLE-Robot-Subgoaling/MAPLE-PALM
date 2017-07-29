package testing;

import action.models.CreateActionModels;
import action.models.TrajectoryGengerator;
import action.models.VariableTree;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchies.structureLearning.CATrajectory;
import taxi.Taxi;
import taxi.stateGenerator.RandomPassengerTaxiState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CATTest {

	public static void main(String[] args) {
		Taxi taxi = new Taxi();
		OOSADomain domain = taxi.generateDomain();
		FullModel model = (FullModel) domain.getModel();
		StateGenerator randomPasseger = new RandomPassengerTaxiState();
		double gamma = 0.9;
		HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		double maxDelta = 0.01;
		int maxIterations = 100;
		int numTrajectories = 50;
		boolean loadFiles = true;
		String directory = "trees";

		List<Episode> trajectories = TrajectoryGengerator.generateTrajectories(randomPasseger, numTrajectories, domain,
				gamma, hashingFactory, maxDelta, maxIterations);
//        EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
//                (TaxiVisualizer.getVisualizer(5, 5), domain, trajectories);
//        ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
//        ev.initGUI();

		Map<String, Map<String, VariableTree>> trees;
		if(loadFiles)
			trees = CreateActionModels.readTreeFiles(directory);
		else
			trees = CreateActionModels.createModels(trajectories);

		List<CATrajectory> cats = new ArrayList<CATrajectory>();
		for (Episode trajectory : trajectories){
			CATrajectory cat = new CATrajectory();
			cat.annotateTrajectory(trajectory, trees, model);
			cats.add(cat);
		}

		for (CATrajectory cat : cats){
			System.out.println(cat);
			System.out.println();
		}
	}

}
