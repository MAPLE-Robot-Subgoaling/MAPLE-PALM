package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

public class ConstantDiscountProvider extends DiscountProvider {

    protected double constantGamma;

    public ConstantDiscountProvider(double constantGamma) {
        this.constantGamma = constantGamma;
    }

    @Override
    public double yield(State s, Action action, State sPrime, boolean oneOff) {
        return constantGamma;
    }

    @Override
    public double getGamma() {
        return constantGamma;
    }
}
