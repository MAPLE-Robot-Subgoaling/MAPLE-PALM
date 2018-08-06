package edu.umbc.cs.maple.liftcopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state.LCGetState;
import edu.umbc.cs.maple.utilities.MutableObject;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class GetCompletedPF extends PropositionalFunction{ 
    // get is complete when desired cargo is in the taxi

    public GetCompletedPF() {
        super("get", new String[]{CLASS_CARGO});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCGetState)) { return false; }
        String cargoName = params[0];
        MutableObject cargo = (MutableObject) s.object(cargoName);
        if (cargo == null) { return false; }
        String cargo_loc = (String) cargo.get(ATT_LOCATION);
        return cargo_loc.equals(ATT_VAL_PICKED_UP);//?????
    }
}
