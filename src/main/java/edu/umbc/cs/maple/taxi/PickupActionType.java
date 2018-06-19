package edu.umbc.cs.maple.taxi;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.taxi.state.TaxiPassenger;
import edu.umbc.cs.maple.taxi.state.TaxiState;
import edu.umbc.cs.maple.utilities.MutableObject;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class PickupActionType extends ObjectParameterizedActionType {
    public PickupActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        MutableOOState state = (MutableOOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        MutableObject passenger = (MutableObject)state.object(passengerName);

        // Can't pick up a passenger already in the taxi
        if((boolean)passenger.get(ATT_IN_TAXI)) {
            return false;
        }

        int tx = (int)state.objectsOfClass(CLASS_TAXI).get(0).get(ATT_X);
        int ty = (int)state.objectsOfClass(CLASS_TAXI).get(0).get(ATT_Y);
        int px = (int)passenger.get(ATT_X);
        int py = (int)passenger.get(ATT_Y);

        return tx == px && ty == py;
    }
}

