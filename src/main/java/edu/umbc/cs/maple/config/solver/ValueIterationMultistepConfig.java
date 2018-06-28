package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.MDPSolver;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.ValueIterationMultiStep;

public class ValueIterationMultistepConfig extends SolverConfig {
    double maxDelta;
    int maxIterations;
    SADomain domain;

    HashableStateFactory hashingFactory;
    DiscountProvider discountProvider;

    public ValueIterationMultistepConfig(){}

    @Override
    public MDPSolver generateSolver(){
        return new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterations,discountProvider);
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
    public SADomain getDomain() {
        return domain;
    }

    public void setDomain(SADomain domain) {
        this.domain = domain;
    }

    public HashableStateFactory getHashingFactory() {
        return hashingFactory;
    }

    public void setHashingFactory(HashableStateFactory hashingFactory) {
        this.hashingFactory = hashingFactory;
    }

    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }

    public void setDiscountProvider(DiscountProvider discountProvider) {
        this.discountProvider = discountProvider;
    }
}
