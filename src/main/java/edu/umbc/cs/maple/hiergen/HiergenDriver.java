package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.debugtools.DPrint;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.CreateActionModels;
import edu.umbc.cs.maple.hiergen.CAT.TrajectoryGenerator;
import edu.umbc.cs.maple.hiergen.CAT.VariableTree;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.stategenerator.RandomPassengerTaxiState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HierGenDriver {
    public static void main(String[] args) {
        Taxi test = new Taxi();
        int numTrajectories = 5;
        double gamma = 0.01;
        OOSADomain domain = test.generateDomain();

        System.out.println("Generating trajectories");
        List<Episode> episodes = TrajectoryGenerator.generateQLearnedTrajectories(new RandomPassengerTaxiState(), numTrajectories, domain, gamma, new SimpleHashableStateFactory());

        /*
        EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(5, 5),
                dom, episodes);
        v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
        v.initGUI();
        */

        System.out.println("Learning the action models");
        ArrayList<CATrajectory> CATs = new ArrayList<>();
        Map<String, Map<String, VariableTree>> actionModels = CreateActionModels.createModels(episodes);

        System.out.println("Cauaslly annotating the trajectories");
        for (Episode e : episodes) {
            CATrajectory temp = new CATrajectory();
            temp.annotateTrajectory(e, actionModels, (FullModel) domain.getModel());
            CATs.add(temp);
        }

        System.out.println("Running the main HierGenAlgorithm");
        HierGenAlgorithm.generate(actionModels, CATs);

    }
}
