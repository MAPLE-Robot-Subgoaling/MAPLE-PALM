package edu.umbc.cs.maple.utilities;

import burlap.behavior.singleagent.MDPSolver;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;

public abstract class MultiStepMDPSolver extends MDPSolver {

    protected DiscountProvider discountProvider;

    @Override
    public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {
        throw new RuntimeException("Warning: do not call this method, use solverInit with DiscountProvider");
    }

    public void solverInit(SADomain domain, HashableStateFactory hashingFactory, DiscountProvider discountProvider) {
        this.discountProvider = discountProvider;
        this.gamma = Double.NEGATIVE_INFINITY;
        this.hashingFactory = hashingFactory;
        this.setDomain(domain);
    }


}
