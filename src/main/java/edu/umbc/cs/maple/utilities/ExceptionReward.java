package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class ExceptionReward implements RewardFunction {
    @Override
    public double reward(State s, Action a, State sprime) {
        throw new RuntimeException("Error: no reward function was ever set for this domain");
    }
}
