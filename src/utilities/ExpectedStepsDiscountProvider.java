package utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import palm.rmax.agent.ExpectedRmaxModel;

public class ExpectedStepsDiscountProvider extends DiscountProvider {

    protected double gamma;
    protected ExpectedRmaxModel model;

    public ExpectedStepsDiscountProvider(double gamma, ExpectedRmaxModel model) {
        this.gamma = gamma;
        this.model = model;
    }

    public double yield(State s, Action action, State sPrime) {
        double kappa = model.getExpectedNumberOfSteps(s, action, sPrime);
        double discount = Math.pow(gamma, kappa);
        return discount;
    }
}
