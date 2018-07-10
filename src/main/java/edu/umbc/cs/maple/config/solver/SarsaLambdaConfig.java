package edu.umbc.cs.maple.config.solver;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ValueFunction;


public class SarsaLambdaConfig extends SolverConfig{
    double gamma;
    public double lambda;
    public double learningRate;
    DifferentiableStateActionValue vfa;

    public SarsaLambdaConfig(){
        this.setType("SarsaLambda");
    }
    @Override
    public Planner generateSolver(ValueFunction knownValueFunction) {
        if(knownValueFunction instanceof GradientDescentSarsaLam){
            return (GradientDescentSarsaLam)knownValueFunction;
        }else{
            GradientDescentSarsaLam planner = new GradientDescentSarsaLam(domain, gamma, (DifferentiableStateActionValue) vfa, learningRate, lambda);
        }
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
