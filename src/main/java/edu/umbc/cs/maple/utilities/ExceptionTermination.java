package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

public class ExceptionTermination implements TerminalFunction {

    @Override
    public boolean isTerminal(State s) {
        throw new RuntimeException("Error: no terminal function was ever set in this domain");
    }
}
