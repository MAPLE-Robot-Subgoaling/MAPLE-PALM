package cleanup.hierarchies;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class PickRoomAgentGoalPF extends PropositionalFunction {

    public PickRoomAgentGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}