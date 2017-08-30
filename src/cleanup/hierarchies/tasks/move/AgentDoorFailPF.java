package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class AgentDoorFailPF extends PropositionalFunction {

    public AgentDoorFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        return false;
    }
}
