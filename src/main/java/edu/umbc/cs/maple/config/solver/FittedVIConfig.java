package edu.umbc.cs.maple.config.solver;

import burlap.behavior.functionapproximation.dense.ConcatenatedObjectFeatures;
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasis;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.vfa.fittedvi.FittedVI;
import burlap.behavior.valuefunction.ValueFunction;
import edu.umbc.cs.maple.burlap_weka.WekaVFATrainer;
import weka.classifiers.Classifier;


public class FittedVIConfig  extends SolverConfig{
    double gamma;
    int transitionSamples;



    public FittedVIConfig (){
        this.setType("FittedVI");
    }
    @Override
    public Planner generateSolver(ValueFunction knownValueFunction) {
//        ConcatenatedObjectFeatures inputFeatures = new ConcatenatedObjectFeatures()
//                .addObjectVectorizion(LunarLanderDomain.CLASS_AGENT, new NumericVariableFeatures());
//
//        GradientDescentSarsaLam
//        WekaVFATrainer vfa = new WekaVFATrainer( ------ );
//        FittedVI planner = new FittedVI(domain, gamma, vfa, transitionSamples, maxDelta, maxIterations);
        return null;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public int getTransitionSamples() {
        return transitionSamples;
    }

    public void setTransitionSamples(int transitionSamples) {
        this.transitionSamples = transitionSamples;
    }

}
