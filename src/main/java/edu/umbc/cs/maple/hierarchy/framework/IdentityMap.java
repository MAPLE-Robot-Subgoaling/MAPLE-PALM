package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

public class IdentityMap implements StateMapping{
    //a identity function for tasks which do not need abstraction
    @Override
    public State mapState(State s) {
        return s;
    }
}
