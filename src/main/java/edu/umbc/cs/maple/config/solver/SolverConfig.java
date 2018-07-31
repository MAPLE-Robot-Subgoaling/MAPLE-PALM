package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.utilities.DiscountProvider;

public abstract class SolverConfig {
    String type;
    SADomain domain;
    HashableStateFactory hashingFactory;
    int numSamples;
    double maxDelta;
    int maxIterations;
    DiscountProvider discountProvider;

    public abstract Planner generateSolver(ValueFunction knownValueFunction);


    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setDomain(SADomain domain) {
        this.domain = domain;
    }
    public SADomain getDomain() {
        return domain;
    }
    public HashableStateFactory getHashingFactory() {
        return hashingFactory;
    }
    public void setHashingFactory(HashableStateFactory hashingFactory) {
        this.hashingFactory = hashingFactory;
    }
    public double getMaxDelta() {
        return maxDelta;
    }
    public void setMaxDelta(double maxDelta) {
        this.maxDelta = maxDelta;
    }
    public int getMaxIterations() {
        return maxIterations;
    }
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }
    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }
    public void setDiscountProvider(DiscountProvider discountProvider) {
        this.discountProvider = discountProvider;
    }
    public int getNumSamples() {
        return numSamples;
    }
    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }


}
