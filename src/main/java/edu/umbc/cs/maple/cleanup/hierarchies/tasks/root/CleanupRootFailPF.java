package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class CleanupRootFailPF extends PropositionalFunction {

    public CleanupRootFailPF(){
        super("rootFail", new String[]{});
    }
    public CleanupRootFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        return false;
    }
}
