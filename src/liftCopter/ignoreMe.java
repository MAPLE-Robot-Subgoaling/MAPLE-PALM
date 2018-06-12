package liftCopter;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.dense.ConcatenatedObjectFeatures;
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.TileCodingFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.TilingArrangement;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.domain.singleagent.lunarlander.LLVisualizer;
import burlap.domain.singleagent.lunarlander.LunarLanderDomain;
import burlap.domain.singleagent.lunarlander.state.LLAgent;
import burlap.domain.singleagent.lunarlander.state.LLBlock;
import burlap.domain.singleagent.lunarlander.state.LLState;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
class ContinuousDomainTutorial {

    private ContinuousDomainTutorial() {
        // do nothing
    }

    public static void LLSARSA() {

        LunarLanderDomain lld = new LunarLanderDomain();
        OOSADomain domain = lld.generateDomain();

        LLState s = new LLState(new LLAgent(5, 0, 0), new LLBlock.LLPad(75, 95, 0, 10, "pad"));

        ConcatenatedObjectFeatures inputFeatures = new ConcatenatedObjectFeatures()
                .addObjectVectorizion(LunarLanderDomain.CLASS_AGENT, new NumericVariableFeatures());

        int nTilings = 5;
        double resolution = 10.;

        double xWidth = (lld.getXmax() - lld.getXmin()) / resolution;
        double yWidth = (lld.getYmax() - lld.getYmin()) / resolution;
        double velocityWidth = 2 * lld.getVmax() / resolution;
        double angleWidth = 2 * lld.getAngmax() / resolution;


        TileCodingFeatures tilecoding = new TileCodingFeatures(inputFeatures);
        tilecoding.addTilingsForAllDimensionsWithWidths(
                new double[]{xWidth, yWidth, velocityWidth, velocityWidth, angleWidth},
                nTilings,
                TilingArrangement.RANDOM_JITTER);


        double defaultQ = 0.5;
        DifferentiableStateActionValue vfa = tilecoding.generateVFA(defaultQ / nTilings);
        GradientDescentSarsaLam agent = new GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
        List<Episode> episodes = new ArrayList<Episode>();
        for (int i = 0; i < 5000; i++) {
            Episode ea = agent.runLearningEpisode(env);
            episodes.add(ea);
            System.out.println(i + ": " + ea.maxTimeStep());
            env.resetEnvironment();
        }

        Visualizer v = LLVisualizer.getVisualizer(lld.getPhysParams());
        new EpisodeSequenceVisualizer(v, domain, episodes);

    }


    public static void main(String[] args) {
        //MCLSPIFB();
        //MCLSPIRBF();
        //IPSS();
        LLSARSA();
    }

}
