package edu.umbc.cs.maple.hiergen;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import edu.umbc.cs.maple.hiergen.CAT.CATrajectory;
import edu.umbc.cs.maple.hiergen.CAT.CreateActionModels;
import edu.umbc.cs.maple.hiergen.CAT.TrajectoryGenerator;
import edu.umbc.cs.maple.hiergen.CAT.VariableTree;
import edu.umbc.cs.maple.state.hashing.bugfix.BugfixHashableStateFactory;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.TaxiVisualizer;
import edu.umbc.cs.maple.taxi.stategenerator.HierGenTrajectorySource;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HierGenMain {

    public static final String DIRECTORY_PATH_HIERGEN_OUTPUT = "./output_hiergen/";
    public static final String DIRECTORY_PATH_TRAJECTORY = "trajectories/";
    public static final String DIRECTORY_PATH_ACTION_MODEL_TREES = "trees/";
    public static final String TRAJECTORY_FILE_PREFIX = "trajectory";

    public static void generateTrajectories(String pathToTrajectories, int trajectoryCount) {

        HashableStateFactory hsf = new BugfixHashableStateFactory(false);

        HierGenTrajectorySource trajectorySource = new HierGenTrajectorySource();

        Taxi test = new Taxi();
        OOSADomain domain = test.generateDomain();

        System.out.println("Generating trajectories");
        double gamma = 0.95;
        List<Episode> episodes = TrajectoryGenerator.generateQLearnedTrajectories(trajectorySource, trajectoryCount, domain, gamma, hsf);

        Episode.writeEpisodes(episodes, pathToTrajectories, TRAJECTORY_FILE_PREFIX);

    }

    public static void main(String[] args) {

        String pathToTrajectories = DIRECTORY_PATH_HIERGEN_OUTPUT + DIRECTORY_PATH_TRAJECTORY;
        String pathToTrees = DIRECTORY_PATH_HIERGEN_OUTPUT + DIRECTORY_PATH_ACTION_MODEL_TREES;

        long seed = 20948304976L;

        System.out.println("Running HierGen algorithm on seed: " + seed);

        RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX).setSeed(seed);

//        int trajectoryCount = 30;
//        generateTrajectories(pathToTrajectories, trajectoryCount);

        List<Episode> trajectories = Episode.readEpisodes(pathToTrajectories);

//        Episode one = trajectories.get(0);
//        System.out.println(one.stateSequence.get(one.stateSequence.size()-1));
//        System.out.println(one.rewardSequence.get(one.rewardSequence.size()-1));
//        System.out.println(one.actionSequence.get(one.actionSequence.size()-1));
//
//        EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(5, 5), new Taxi().generateDomain(), trajectories);
//        v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
//        v.initGUI();
//
//        System.out.println("Learning the action models");
//        CreateActionModels.createModels(pathToTrees, trajectories);

        Map<String, Map<String, VariableTree>> actionModels = CreateActionModels.readTreeFiles(pathToTrees);

        SADomain domain = new Taxi().generateDomain();
//        domain.setModel(null); // for Causal Annotation, we assume access to the model
        FullModel model = (FullModel) domain.getModel();

        ArrayList<CATrajectory> cats = new ArrayList<>();
        System.out.println("Causally annotating the trajectories");
        for (Episode trajectory : trajectories) {
            CATrajectory cat = new CATrajectory();
            cat.annotateTrajectory(trajectory, actionModels, model);//
            cats.add(cat);
        }

        System.out.println("Testing CAT-Scan");
        CATScan.test(cats);

        System.out.println("Running the main HierGenAlgorithm");
        HierGenTask root = HierGenAlgorithm.generate(actionModels, cats);
        System.out.println(root);


    }
}
