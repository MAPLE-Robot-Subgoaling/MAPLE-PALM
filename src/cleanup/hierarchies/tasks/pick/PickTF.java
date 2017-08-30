package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

public class PickTF implements TerminalFunction {

    @Override
    public boolean isTerminal(State state) {
        throw new RuntimeException("not implemented");
    }
}
