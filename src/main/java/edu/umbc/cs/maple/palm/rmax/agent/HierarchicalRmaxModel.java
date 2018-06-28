package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.utilities.ConstantDiscountProvider;

public class HierarchicalRmaxModel extends RmaxModel {

	private boolean useMultitimeModel;

	/**
	 * creates a rmax model
	 * @param task the grounded task to model
	 * @param threshold rmax sample threshold
	 * @param rmax max rewardTotal in domain
	 * @param hs provided hashing factory
	 */
	public HierarchicalRmaxModel(Task task, int threshold, double rmax, HashableStateFactory hs, double gamma, boolean useMultitimeModel) {
		super(task, threshold, rmax, hs, gamma);
		this.useMultitimeModel = useMultitimeModel;
	}

    @Override
    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ConstantDiscountProvider(gamma);
    }


    @Override
    public double getInternalDiscountReward(EnvironmentOutcome eo, int k) {
        double discount = 1.0;
        if (useMultitimeModel) {
            double gamma = discountProvider.yield(eo.o, eo.a, eo.op, false);
            discount = Math.min(1.0, Math.pow(gamma, k - 1)); // note: use k - 1
        }
        return discount;
    }

    @Override
    public double getInternalDiscountProbability(EnvironmentOutcome eo, int k) {
        double discount = 1.0;
        if (useMultitimeModel) {
            double gamma = discountProvider.yield(eo.o, eo.a, eo.op, false);
            discount = Math.min(1.0, Math.pow(gamma, k)); // note: use k
        }
        return discount;
    }

}
