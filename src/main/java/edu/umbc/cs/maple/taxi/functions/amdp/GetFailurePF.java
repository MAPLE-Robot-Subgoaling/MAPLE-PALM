package edu.umbc.cs.maple.taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_PASSENGER;
public class GetFailurePF extends PropositionalFunction {
    //get fails if any passenger if in taxi unless it is the right one

    public GetFailurePF() {
        super("getFail", new String[]{CLASS_PASSENGER});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return false;
    }

}
