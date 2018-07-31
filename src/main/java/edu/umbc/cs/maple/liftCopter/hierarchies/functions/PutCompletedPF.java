package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state.LCPutState;
import edu.umbc.cs.maple.utilities.MutableObject;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;


public class PutCompletedPF extends PropositionalFunction{
    //put is complete when the passenger at the put actions goal

    public PutCompletedPF() {
        super("put", new String[]{CLASS_CARGO});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCPutState)) { return false; }
        String passengerName = params[0];
        MutableObject passenger = (MutableObject) s.object(passengerName);
        if (passenger == null) { return false; }
        String passengerGoal = (String) passenger.get(ATT_GOAL_LOCATION);
        String passengerLocation = (String) passenger.get(ATT_LOCATION);
        return passengerGoal.equals(passengerLocation);
    }
}
