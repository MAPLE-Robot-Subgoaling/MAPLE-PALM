package edu.umbc.cs.maple.taxi.hierarchies.tasks.put;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.TaxiPutState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class PutPutdownActionType extends ObjectParameterizedActionType {
    public PutPutdownActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiPutState state = (TaxiPutState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        TaxiPutAgent taxi = state.getTaxi();
        String taxiLoc = (String)taxi.get(ATT_LOCATION);
        return (passenger.get(ATT_LOCATION)).equals(ATT_VAL_IN_TAXI) && !taxiLoc.equals(ATT_VAL_ON_ROAD);
    }
}

