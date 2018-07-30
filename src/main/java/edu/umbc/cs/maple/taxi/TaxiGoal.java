package edu.umbc.cs.maple.taxi;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.config.DomainGoal;

public class TaxiGoal extends DomainGoal {
    @Override
    public boolean satisfies(State state) {
        return false;
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return satisfies(s);
    }
}
