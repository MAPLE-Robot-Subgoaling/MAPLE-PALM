package taxi.hierGen.actions;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierGen.root.state.TaxiHierGenRootState;

import static taxi.TaxiConstants.ATT_IN_TAXI;
import static taxi.TaxiConstants.ATT_LOCATION;

public class HierGenTask7ActionType extends ObjectParameterizedActionType {

    public HierGenTask7ActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        // always applicable ?
//        return true;
        TaxiHierGenRootState state = (TaxiHierGenRootState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        // passenger location is not IN_TAXI if it matches Taxi location
        boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
        return !inTaxi;
    }
}
