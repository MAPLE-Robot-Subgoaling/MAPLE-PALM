package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class MoveAgentXYFailPF extends PropositionalFunction {
    public MoveAgentXYFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    public MoveAgentXYFailPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }
    public MoveAgentXYFailPF(){
        super("moveXYFail", new String[]{});
    }

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return false;
    }
}
