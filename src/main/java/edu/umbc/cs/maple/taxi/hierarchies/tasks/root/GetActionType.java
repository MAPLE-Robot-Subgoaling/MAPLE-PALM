package edu.umbc.cs.maple.taxi.hierarchies.tasks.root;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_LOCATION;
import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_VAL_IN_TAXI;


public class GetActionType extends ObjectParameterizedActionType {

    public GetActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        OOState state = (OOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        return !passenger.get(ATT_LOCATION).equals(ATT_VAL_IN_TAXI);
    }
}
