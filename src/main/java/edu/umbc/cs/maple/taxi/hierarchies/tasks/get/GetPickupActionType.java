package edu.umbc.cs.maple.taxi.hierarchies.tasks.get;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.get.state.TaxiGetState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_LOCATION;

public class GetPickupActionType extends ObjectParameterizedActionType {
    public GetPickupActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiGetState state = (TaxiGetState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        // passenger location is not IN_TAXI if it matches Taxi location
        boolean atPassenger =  passenger.get(ATT_LOCATION).equals(state.getTaxiAtt(ATT_LOCATION));
        return atPassenger;
    }
}

