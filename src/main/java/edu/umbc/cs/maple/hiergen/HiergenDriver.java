package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
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
        List<Episode> episodes = TrajectoryGenerator.generateQLearnedTrajectories(new RandomPassengerTaxiState(), numTrajectories, domain, gamma, new SimpleHashableStateFactory());

        /*
        EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(5, 5),
                dom, episodes);
        v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
        v.initGUI();
        */

        ArrayList<CATrajectory> CATs = new ArrayList<CATrajectory>();
        Map<String, Map<String, VariableTree>> actionModels = CreateActionModels.createModels(episodes);

        for (Episode e : episodes) {
            CATrajectory temp = new CATrajectory();
            temp.annotateTrajectory(e, actionModels, (FullModel) domain.getModel());
            CATs.add(temp);
        }

        HierGenAlgorithm.generate(actionModels, CATs).toString();
    }
}
