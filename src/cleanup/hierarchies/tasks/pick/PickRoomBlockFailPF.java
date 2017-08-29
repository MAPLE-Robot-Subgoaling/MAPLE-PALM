package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class PickRoomBlockFailPF extends PropositionalFunction {

    public PickRoomBlockFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}