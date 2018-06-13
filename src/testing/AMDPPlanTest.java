package testing;

import amdp.planning.AMDPPlanner;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import config.ExperimentConfig;
import hierarchy.framework.Task;
import state.hashing.cached.CachedHashableStateFactory;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.hierarchies.TaxiHierarchyExpert;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class AMDPPlanTest {

    public static void plan(ExperimentConfig conf, Task root, State init, HashableStateFactory hs, OOSADomain baseDomain) {

        AMDPPlanner amdp = new AMDPPlanner(root, conf.gamma, hs, conf.rmax.max_delta, conf.planning.rollouts, conf.max_steps);
        List<Episode> eps = new ArrayList<Episode>();

        for(int i = 0; i < conf.episodes; i++){
            Episode e = amdp.planFromState(init);
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
        String configFile = "./config/taxi/classic.yaml";
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
        Task palmRoot = hierarchy.createHierarchy(config, true);
        OOSADomain base = hierarchy.getBaseDomain();
//        HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        HashableStateFactory hashingFactory = new CachedHashableStateFactory(false);
        plan(config, palmRoot, s, hashingFactory, base);
    }
}
