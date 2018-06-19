package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class MoveRF implements RewardFunction {
    @Override
    public double reward(State state, Action action, State state1) {
        throw new RuntimeException("not implemented");
    }
}
