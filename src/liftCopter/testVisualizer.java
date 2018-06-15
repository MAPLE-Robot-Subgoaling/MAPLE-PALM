package liftCopter;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.dense.ConcatenatedObjectFeatures;
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.TileCodingFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.TilingArrangement;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import liftCopter.state.LiftCopterState;
import liftCopter.stateGenerator.LiftCopterStateFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class testVisualizer {
    public static void main( String[] args){
        LCSARSA();
    }
    public static void LCSARSA(){
        LiftCopter lc = new LiftCopter();
        OOSADomain domain = lc.generateDomain();

        LiftCopterState s = LiftCopterStateFactory.createClassicState();

        ConcatenatedObjectFeatures inputFeatures = new ConcatenatedObjectFeatures()
                .addObjectVectorizion(LiftCopterConstants.CLASS_AGENT, new NumericVariableFeatures());

        int nTilings = 6;
        double resolution = 50;

        double xWidth = 5 / resolution;
        double yWidth = 5 / resolution;
        double xVelocityWidth = 2 * LiftCopterConstants.PHYS_MAX_VX / resolution;
        double yVelocityWidth = 2 * LiftCopterConstants.PHYS_MAX_VY / resolution;
        double heightWidth = 1;
        double widthWidth = 1;
        TileCodingFeatures tilecoding = new TileCodingFeatures(inputFeatures);
        tilecoding.addTilingsForAllDimensionsWithWidths(
                new double []{xWidth, yWidth, xVelocityWidth, yVelocityWidth, heightWidth, widthWidth},
                nTilings,
                TilingArrangement.RANDOM_JITTER);




        double defaultQ = 0.5;
        DifferentiableStateActionValue vfa = tilecoding.generateVFA(defaultQ/nTilings);
        GradientDescentSarsaLam agent = new GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5);
        Visualizer v = LiftCopterVisualizer.getVisualizer(5,5);
        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
        List<Episode> episodes = new ArrayList<Episode>();
        VisualActionObserver obs = new VisualActionObserver(domain, v);
        obs.setFrameDelay(0);
        obs.initGUI();
        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
        env.addObservers(obs);
        for(int i = 0; i < 50000; i++){
            Episode ea = agent.runLearningEpisode(env);
            episodes.add(ea);
            System.out.println(i + ": " + ea.maxTimeStep());
            env.resetEnvironment();
        }


        new EpisodeSequenceVisualizer(v, domain, episodes);


    }

}
