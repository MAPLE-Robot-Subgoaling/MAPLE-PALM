package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.CreateActionModels;
import edu.umbc.cs.maple.hiergen.CAT.TrajectoryGenerator;
import edu.umbc.cs.maple.hiergen.CAT.VariableTree;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.TaxiVisualizer;
import edu.umbc.cs.maple.taxi.stategenerator.HierGenTrajectorySource;
import edu.umbc.cs.maple.taxi.stategenerator.RandomPassengerTaxiState;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HierGenMain {

    public static void main(String[] args) {

        long seed = 20948304976L;

        System.out.println("Running HierGen algorithm on seed: " + seed);

        RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX).setSeed(seed);

        Taxi test = new Taxi();
        int numTrajectories = 5;
        double gamma = 0.01;
        OOSADomain domain = test.generateDomain();

        System.out.println("Generating trajectories");
        List<Episode> episodes = TrajectoryGenerator.generateQLearnedTrajectories(new HierGenTrajectorySource(), numTrajectories, domain, gamma, new SimpleHashableStateFactory());

        Episode one = episodes.get(0);
        System.out.println(one.stateSequence.get(one.stateSequence.size()-1));
        System.out.println(one.rewardSequence.get(one.rewardSequence.size()-1));
        System.out.println(one.actionSequence.get(one.actionSequence.size()-1));

        EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(5, 5), domain, episodes);
        v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
        v.initGUI();

        System.out.println("Learning the action models");
        ArrayList<CATrajectory> CATs = new ArrayList<>();
        Map<String, Map<String, VariableTree>> actionModels = CreateActionModels.createModels(episodes);

        System.out.println("Causally annotating the trajectories");
        for (Episode e : episodes) {
            CATrajectory temp = new CATrajectory();
            temp.annotateTrajectory(e, actionModels, (FullModel) domain.getModel());
            CATs.add(temp);
        }

        System.out.println("Running the main HierGenAlgorithm");
        HierGenTask root = HierGenAlgorithm.generate(actionModels, CATs);
        System.out.println(root);


    }
}
