package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.PossibleOutcome;
import edu.umbc.cs.maple.utilities.ExpectedStepsDiscountProvider;

import java.util.Map;

public class ExpectedRmaxModel extends RmaxModel {

    private static final double DEFAULT_INTERNAL_DISCOUNT = 1.0; // would be < 1.0 if using Multi-time model

    /**
     * creates a rmax model
     * @param task the task to model
     * @param threshold rmax sample threshold
     * @param rmax max rewardTotal in domain
     * @param hs provided hashing factory
     */
    public ExpectedRmaxModel(Task task, int threshold, double rmax, HashableStateFactory hs, double gamma) {
        super(task, threshold, rmax, hs, gamma);
    }

    @Override
    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ExpectedStepsDiscountProvider(gamma, this);
    }

    @Override
    public double getInternalDiscountReward(EnvironmentOutcome eo, int k) {
        return DEFAULT_INTERNAL_DISCOUNT;
    }

    @Override
    public double getInternalDiscountProbability(EnvironmentOutcome eo, int k) {
        return DEFAULT_INTERNAL_DISCOUNT;
    }

    public double getExpectedNumberOfSteps(State s, Action action, State sPrime) {
        HashableState hs = hashingFactory.hashState(s);
        HashableState hsPrime = hashingFactory.hashState(sPrime);
        Map<HashableState, PossibleOutcome> hsPrimeToOutcomes = getHsPrimeToOutcomes(hs, action);
        PossibleOutcome possibleOutcome = getPossibleOutcome(hsPrimeToOutcomes, hs, action, hsPrime);
        double expectedNumberOfSteps = possibleOutcome.getExpectedNumberOfSteps();
        return expectedNumberOfSteps;
    }
}
