package edu.umbc.cs.maple.taxi.hierarchies.tasks;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiGetPutState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_LOCATION;
import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_TAXI;

public class NavigateActionType extends ObjectParameterizedActionType {

    public NavigateActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiGetPutState state = (TaxiGetPutState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String locName = params[0];
        ObjectInstance location = state.object(locName);

        ObjectInstance taxi = state.objectsOfClass(CLASS_TAXI).get(0);
        return !location.name().equals(taxi.get(ATT_LOCATION));
    }
}
