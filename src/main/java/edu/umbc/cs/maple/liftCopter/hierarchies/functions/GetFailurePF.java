package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.CLASS_CARGO;

public class GetFailurePF extends PropositionalFunction {
    //get fails if any passenger if in taxi unless it is the right one

    public GetFailurePF() {
        super("getFail", new String[]{CLASS_CARGO});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {

        return false;
    }

}
