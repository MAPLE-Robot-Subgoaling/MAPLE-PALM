package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ValueFunction;
import edu.umbc.cs.maple.config.tilecoding.TileCodingConfig;


public class SarsaLambdaConfig extends SolverConfig{
    double gamma;
    public double lambda;
    public double learningRate;
    public TileCodingConfig TCC;

    public SarsaLambdaConfig(){
        this.setType("SarsaLambda");
    }
    @Override
    public Planner generateSolver(ValueFunction knownValueFunction) {
        if(knownValueFunction instanceof GradientDescentSarsaLam){
            return (GradientDescentSarsaLam)knownValueFunction;
        }else{
            GradientDescentSarsaLam planner = new GradientDescentSarsaLam(domain, gamma, TCC.generateVFA(), learningRate, lambda);
            return planner;
        }

    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

}
