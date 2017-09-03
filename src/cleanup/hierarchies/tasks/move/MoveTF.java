package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

public class MoveTF implements TerminalFunction {
    @Override
    public boolean isTerminal(State state) {
        throw new RuntimeException("not implemented");
    }
}
