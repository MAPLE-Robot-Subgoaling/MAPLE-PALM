package edu.umbc.cs.maple.liftCopter;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.utilities.MutableObject;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

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
        if(!(boolean)passenger.get(ATT_PICKED_UP)) {
            return false;
        }

        double px = (double)passenger.get(ATT_X);
        double py = (double)passenger.get(ATT_Y);

        for(ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)) {
            double lx = (double)location.get(ATT_X);
            double ly = (double)location.get(ATT_Y);
            // must be at ANY location in order to put the passenger down
            if(lx == px && ly == py) {
                return true;
            }
        }

        // the passenger is in the taxi but not at a location
        return false;
    }
}

