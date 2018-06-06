package taxi;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierGen.root.state.TaxiHierGenRootState;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;
import utilities.MutableObject;

import static taxi.TaxiConstants.*;

public class PutdownActionType extends ObjectParameterizedActionType {
    public PutdownActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        MutableOOState state = (MutableOOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        MutableObject passenger = (MutableObject)state.object(passengerName);

        // Can't put down a passenger not in the taxi
        if(!(boolean)passenger.get(ATT_IN_TAXI)) {
            return false;
        }

        int px = (int)passenger.get(ATT_X);
        int py = (int)passenger.get(ATT_Y);
        // a temporary hack -- hiergen taxi root state really should still include depots...
        // but for now we just permit putdown in any case
        if (state instanceof TaxiHierGenRootState) {
            return true;
        }
        for(ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)) {
            int lx = (int)location.get(ATT_X);
            int ly = (int)location.get(ATT_Y);
            // must be at ANY location in order to put the passenger down
            if(lx == px && ly == py) {
                return true;
            }
        }

        // the passenger is in the taxi but not at a location
        return false;
    }
}

