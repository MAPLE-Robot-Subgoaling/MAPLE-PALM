package edu.umbc.cs.maple.testing;

import edu.umbc.cs.maple.amdp.planning.AMDPPlanner;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.cleanup.hierarchies.CleanupHierarchy;
import edu.umbc.cs.maple.cleanup.hierarchies.CleanupHierarchyAMDP;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.state.hashing.cached.CachedHashableStateFactory;
import edu.umbc.cs.maple.taxi.TaxiVisualizer;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiHierarchy;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiHierarchyExpert;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class AMDPPlanTest {

    public static void plan(ExperimentConfig conf, Task root, State initialState, HashableStateFactory hs, OOSADomain baseDomain) {

        AMDPPlanner amdp = new AMDPPlanner(root, conf.gamma, hs, conf.rmax.max_delta, conf.planning.rollouts, conf.max_steps);
        List<Episode> eps = new ArrayList<Episode>();

        System.err.println("For now, setting episodes always to just 1 inside AMDPPlanTest");
        conf.episodes = 1;
        for(int i = 0; i < conf.episodes; i++){
            Episode e = amdp.planFromState(baseDomain, initialState);
            eps.add(e);
            System.out.println(e.actionSequence);
            System.out.println(e.rewardSequence);
            amdp.resetSolver();
        }

        EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
                (TaxiVisualizer.getVisualizer(conf.output.visualizer.width, conf.output.visualizer.height), baseDomain, eps);;
        ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
        ev.initGUI();
    }

    public static void main(String[] args) {
        String configFile = "./config/taxi/jwtest.yaml";
        if(args.length > 0) {
            configFile = args[0];
        }

        ExperimentConfig config = new ExperimentConfig();
        try {
            System.out.println("Using configuration: " + configFile);
            config = ExperimentConfig.load(configFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Could not find configuration file");
            System.exit(404);
        }

        if(config.seed > 0) {
            RandomFactory.seedMapped(0, config.seed);
            System.out.println("Using seed: " + config.seed);
        }

        State s = config.generateState();

        TaxiHierarchy hierarchy = new TaxiHierarchyExpert();
//        CleanupHierarchy hierarchy = new CleanupHierarchyAMDP();
        Task palmRoot = hierarchy.createHierarchy(config, true);
        OOSADomain base = hierarchy.getBaseDomain();
//        HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        HashableStateFactory hashingFactory = new CachedHashableStateFactory(false);
        plan(config, palmRoot, s, hashingFactory, base);
    }
}
