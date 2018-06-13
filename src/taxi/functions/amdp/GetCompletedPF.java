package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.get.state.GetStateMapper;
import taxi.hierarchies.tasks.get.state.TaxiGetState;
import utilities.MutableObject;

import static taxi.TaxiConstants.*;

public class GetCompletedPF extends PropositionalFunction{ 
    // get is complete when desired passenger is in the taxi

    public GetCompletedPF() {
        super("get", new String[]{CLASS_PASSENGER});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof TaxiGetState)) { return false; }
//      String passengerName = params[0];
//      MutableObject passenger = (MutableObject) s.object(passengerName);
        MutableObject passenger = (MutableObject) s.object(GetStateMapper.GET_PASSENGER_ALIAS);
        if (passenger == null) { return false; }
        String pass_loc = (String) passenger.get(ATT_LOCATION);
        return pass_loc.equals(ATT_VAL_IN_TAXI);
    }
}
