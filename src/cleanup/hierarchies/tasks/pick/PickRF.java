package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class PickRF implements RewardFunction {

    @Override
    public double reward(State state, Action action, State state1) {
        throw new RuntimeException("not implemented");
    }
}
