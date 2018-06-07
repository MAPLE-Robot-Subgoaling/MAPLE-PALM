package utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

public class ConstantDiscountProvider extends DiscountProvider {

    protected double constantGamma;

    public ConstantDiscountProvider(double constantGamma) {
        this.constantGamma = constantGamma;
    }

    @Override
    public double yield(State s, Action action, State sPrime) {
        return constantGamma;
    }
}
