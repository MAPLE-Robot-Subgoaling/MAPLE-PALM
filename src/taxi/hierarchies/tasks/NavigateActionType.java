package taxi.hierarchies.tasks;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierarchies.TaxiGetPutState;

import static taxi.TaxiConstants.ATT_LOCATION;
import static taxi.TaxiConstants.CLASS_TAXI;

public class NavigateActionType extends ObjectParameterizedActionType {

    public NavigateActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiGetPutState state = (TaxiGetPutState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String locName = params[0];
        ObjectInstance taxi = state.objectsOfClass(CLASS_TAXI).get(0);
        ObjectInstance location = state.object(locName);
        return !location.name().equals(taxi.get(ATT_LOCATION));
    }

}
