package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.put.state.TaxiPutState;
import utilities.MutableObject;

import static taxi.TaxiConstants.*;

public class PutCompletedPF extends PropositionalFunction{
    //put is complete when the passenger at the put actions goal

    public PutCompletedPF() {
        super("put", new String[]{CLASS_PASSENGER});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof TaxiPutState)) { return false; }

//        String passengerName = params[0];
//        MutableObject passenger = (MutableObject) s.object(passengerName);
       MutableObject passenger = (MutableObject) s.object(PutStateMapper.PUT_PASSENGER_ALIAS);
        if (passenger == null) { return false; }
        String passengerGoal = (String) passenger.get(ATT_GOAL_LOCATION);
        String passengerLocation = (String) passenger.get(ATT_LOCATION);
        return passengerGoal.equals(passengerLocation);
    }
}
