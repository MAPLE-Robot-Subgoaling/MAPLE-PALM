package hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

public class IdentityMap implements StateMapping{

	@Override
	public State mapState(State s) {
		return s;
	}

}
