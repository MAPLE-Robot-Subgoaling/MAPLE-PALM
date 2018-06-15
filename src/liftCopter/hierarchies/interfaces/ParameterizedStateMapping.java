package liftCopter.hierarchies.interfaces;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

/**
 * Created by sparr on 8/24/17.
 */
public interface ParameterizedStateMapping extends StateMapping {

    State mapState(State s, String... params);

    @Override
    default State mapState(State s) {
        return mapState(s, new String[]{});
    }
}