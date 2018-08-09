package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.utilities.OnlyInternalDiscountProvider;

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
        this.discountProvider = new OnlyInternalDiscountProvider(gamma);
    }

    @Override
    public double getInternalDiscountReward(EnvironmentOutcome eo, int k) {
        double discount = 1.0;
        if (useMultitimeModel) {
            double gamma = ((OnlyInternalDiscountProvider)discountProvider).yieldInternal(eo.o, eo.a, eo.op);
            discount = Math.min(1.0, Math.pow(gamma, k - 1)); // note: use k - 1
        }
        return discount;
    }

    @Override
    public double getInternalDiscountProbability(EnvironmentOutcome eo, int k) {
        double discount = 1.0;
        if (useMultitimeModel) {
            double gamma = ((OnlyInternalDiscountProvider)discountProvider).yieldInternal(eo.o, eo.a, eo.op);
            discount = Math.min(1.0, Math.pow(gamma, k)); // note: use k
        }
        return discount;
    }

}
