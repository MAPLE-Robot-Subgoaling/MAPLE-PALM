package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class ContradictionPF extends PropositionalFunction {

    public ContradictionPF() {
        super("ContradictionPF", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return false;
    }
}
