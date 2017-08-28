package cleanup.hierarchies;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class PickRoomBlockGoalPF extends PropositionalFunction {

    public PickRoomBlockGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}