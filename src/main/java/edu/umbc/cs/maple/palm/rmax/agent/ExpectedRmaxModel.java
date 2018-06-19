package edu.umbc.cs.maple.palm.rmax.agent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.palm.agent.PossibleOutcome;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.ExpectedStepsDiscountProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpectedRmaxModel extends RmaxModel {

    private static final double DEFAULT_INTERNAL_DISCOUNT = 1.0; // would be < 1.0 if using Multi-time model

    /**
     * creates a rmax model
     * @param task the grounded task to model
     * @param threshold rmax sample threshold
     * @param rmax max rewardTotal in domain
     * @param hs provided hashing factory
     */
    public ExpectedRmaxModel(GroundedTask task, int threshold, double rmax, HashableStateFactory hs, double gamma) {
        super(task, threshold, rmax, hs, gamma);
    }

    @Override
    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ExpectedStepsDiscountProvider(gamma, this);
    }

    @Override
    public double getInternalDiscount(EnvironmentOutcome eo, int k) {
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
