package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class PickObjectRoomFailPF extends PropositionalFunction {

    public PickObjectRoomFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        return false;
    }
}