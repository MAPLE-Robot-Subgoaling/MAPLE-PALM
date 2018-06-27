package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.palm.rmax.agent.ExpectedRmaxModel;

public class ExpectedStepsDiscountProvider extends DiscountProvider {

    protected double gamma;
    protected ExpectedRmaxModel model;

    public ExpectedStepsDiscountProvider(double gamma, ExpectedRmaxModel model) {
        this.gamma = gamma;
        this.model = model;
    }

    // if the model has reached the threshold for the transition s/a
    // use the expected number of steps, k, rather than 1.0, as exponent to gamma/discount
    public double yield(State s, Action action, State sPrime, boolean oneOff) {
        int stateActionCount = model.getStateActionCount(s, action);
        int mThreshold = model.getThreshold();
        if (stateActionCount >= mThreshold) {
            double kappa = model.getExpectedNumberOfSteps(s, action, sPrime);
            if (oneOff) { kappa += -1; }
            double discount = Math.min(1.0, Math.pow(gamma, kappa));
            return discount;
        }
        return gamma;
    }

}
