package palm.rmax.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import utilities.ConstantDiscountProvider;

public class HierarchicalRmaxModel extends RmaxModel {

	private boolean useMultitimeModel;

	/**
	 * creates a rmax model
	 * @param task the grounded task to model
	 * @param threshold rmax sample threshold
	 * @param rmax max rewardTotal in domain
	 * @param hs provided hashing factory
	 */
	public HierarchicalRmaxModel( GroundedTask task, int threshold, double rmax, HashableStateFactory hs, double gamma, boolean useMultitimeModel) {
		super(task, threshold, rmax, hs, gamma);
		this.useMultitimeModel = useMultitimeModel;
	}

    @Override
    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ConstantDiscountProvider(gamma);
    }

    @Override
    public double getInternalDiscount(EnvironmentOutcome eo, int k) {
        double discount = 1.0;
        if (useMultitimeModel) {
            double gamma = discountProvider.yield(eo.o, eo.a, eo.op);
            discount = Math.min(1.0, Math.pow(gamma, k));
        }
        return discount;
    }

}
