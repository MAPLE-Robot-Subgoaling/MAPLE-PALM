package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

public class OnlyInternalDiscountProvider extends DiscountProvider {

    protected double gamma;
    protected static final double EXTERNAL_GAMMA = 1.0;

    public OnlyInternalDiscountProvider(double gamma) {
        super();
        this.gamma = gamma;
    }

    @Override
    public double yield(State s, Action action, State sPrime, boolean oneOff) {
        return EXTERNAL_GAMMA;
    }

    public double yieldInternal(State s, Action action, State sPrime) {
        return gamma;
    }

    @Override
    public double getGamma() {
        return gamma;
    }

}
