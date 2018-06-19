package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.palm.rmax.agent.ExpectedRmaxModel;

public abstract class DiscountProvider {

    public abstract double yield(State s, Action action, State sPrime);

}
