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
import static liftCopter.LiftCopterConstants.*;

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

        LiftCopterState s = LiftCopterStateFactory.createMiniState();

        ConcatenatedObjectFeatures inputFeatures = new ConcatenatedObjectFeatures()
                .addObjectVectorizion(LiftCopterConstants.CLASS_AGENT, new NumericVariableFeatures()
                        .addToWhiteList(ATT_X)
                        .addToWhiteList(ATT_Y)
                        .addToWhiteList(ATT_VX)
                        .addToWhiteList(ATT_VY)
                        .addToWhiteList(ATT_H)
                        .addToWhiteList(ATT_W))
                .addObjectVectorizion(LiftCopterConstants.CLASS_CARGO, new NumericVariableFeatures()
                        .addToWhiteList(ATT_X)
                        .addToWhiteList(ATT_Y)
                        .addToWhiteList(ATT_H)
                        .addToWhiteList(ATT_W))
                .addObjectVectorizion(LiftCopterConstants.CLASS_LOCATION, new NumericVariableFeatures()
                        .addToWhiteList(ATT_X)
                        .addToWhiteList(ATT_Y)
                        .addToWhiteList(ATT_H)
                        .addToWhiteList(ATT_W))
                .addObjectVectorizion(LiftCopterConstants.CLASS_WALL, new NumericVariableFeatures()
                        .addToWhiteList(ATT_START_X)
                        .addToWhiteList(ATT_START_Y)
                        .addToWhiteList(ATT_HEIGHT)
                        .addToWhiteList(ATT_WIDTH))
;

        int nTilings = 34;
        double res = 50;

        double agentXWidth = 1 / res;
        double agentYWidth = 3/ res;
        double agentXVelocityWidth = 2 * LiftCopterConstants.PHYS_MAX_VX / res;
        double agentYVelocityWidth = 2 * LiftCopterConstants.PHYS_MAX_VY / res;
        double agentHeightWidth = 1/res;
        double agentWidthWidth = 1/res;

        double cargoXWidth = 1 / res;
        double cargoYWidth = 3 / res;
        double cargoHeightWidth = 1/res;
        double cargoWidthWidth = 1/res;

        double loc1XWidth = 1 / res;
        double loc1YWidth = 3 / res;
        double loc1HeightWidth = 1/res;
        double loc1WidthWidth = 1/res;

        double loc2XWidth = 1 / res;
        double loc2YWidth = 3 / res;
        double loc2HeightWidth = 1/res;
        double loc2WidthWidth = 1/res;

        double wall1XWidth = 1 / res;
        double wall1YWidth = 3 / res;
        double wall1HeightWidth = 1/res;
        double wall1WidthWidth = 1/res;

        double wall2XWidth = 1 / res;
        double wall2YWidth = 3 / res;
        double wall2HeightWidth = 1/res;
        double wall2WidthWidth = 1/res;

        double wall3XWidth = 1 / res;
        double wall3YWidth = 3 / res;
        double wall3HeightWidth = 1/res;
        double wall3WidthWidth = 1/res;

        double wall4XWidth = 1 / res;
        double wall4YWidth = 3 / res;
        double wall4HeightWidth = 1/res;
        double wall4WidthWidth = 1/res;

        TileCodingFeatures tilecoding = new TileCodingFeatures(inputFeatures);
        tilecoding.addTilingsForAllDimensionsWithWidths(
                new double []{
                        agentXWidth,
                        agentYWidth,
                        agentXVelocityWidth,
                        agentYVelocityWidth,
                        agentHeightWidth,
                        agentWidthWidth,
                        cargoXWidth,
                        cargoYWidth,
                        cargoHeightWidth,
                        cargoWidthWidth,
                        loc1XWidth,
                        loc1YWidth,
                        loc1HeightWidth,
                        loc1WidthWidth,
                        loc2XWidth,
                        loc2YWidth,
                        loc2HeightWidth,
                        loc2WidthWidth,
                        wall1XWidth,
                        wall1YWidth,
                        wall1HeightWidth,
                        wall1WidthWidth,
                        wall2XWidth,
                        wall2YWidth,
                        wall2HeightWidth,
                        wall2WidthWidth,
                        wall3XWidth,
                        wall3YWidth,
                        wall3HeightWidth,
                        wall3WidthWidth,
                        wall4XWidth,
                        wall4YWidth,
                        wall4HeightWidth,
                        wall4WidthWidth
                },
                nTilings,
                TilingArrangement.RANDOM_JITTER);




        double defaultQ = 0.5;
        DifferentiableStateActionValue vfa = tilecoding.generateVFA(defaultQ/nTilings);
        GradientDescentSarsaLam agent = new GradientDescentSarsaLam(domain, 0.95, vfa, 0.02, 0.5);
        Visualizer v = LiftCopterVisualizer.getVisualizer(5,5);
        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
        List<Episode> episodes = new ArrayList<Episode>();

//        VisualActionObserver obs = new VisualActionObserver(domain, v);
//        obs.setFrameDelay(0);
//        obs.initGUI();
//        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
//        env.addObservers(obs);

        Episode ea = agent.runLearningEpisode(env);
        int i = 0;
        while(! ea.rewardSequence.contains(0.95D)){
            episodes.add(ea);
            env.resetEnvironment();
            System.out.println(i + ": " + ea.maxTimeStep());
            System.out.println(ea.rewardSequence.toString());
            ea = agent.runLearningEpisode(env);
            i++;
        }
        episodes.add(ea);
        env.resetEnvironment();
        System.out.println(i + ": " + ea.maxTimeStep());
        System.out.println(ea.rewardSequence.toString());
//        for(int i = 0; i < 1000; i++){
//            Episode ea = agent.runLearningEpisode(env);
//            episodes.add(ea);
//            System.out.println(i + ": " + ea.maxTimeStep());
//            System.out.println(ea.rewardSequence.toString());
//            env.resetEnvironment();
//        }


        new EpisodeSequenceVisualizer(v, domain, episodes);


    }

}
