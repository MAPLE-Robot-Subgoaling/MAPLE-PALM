package edu.umbc.cs.maple.liftCopter;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.utilities.MutableObject;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class PickupActionType extends ObjectParameterizedActionType {
    public PickupActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        MutableOOState state = (MutableOOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String cargoName = params[0];
        MutableObject cargo = (MutableObject)state.object(cargoName);

        // Can't pick up a cargo already in the taxi
        if((boolean)cargo.get(ATT_PICKED_UP)) {
            return false;
        }

        double tx = (double)state.objectsOfClass(CLASS_AGENT).get(0).get(ATT_X);
        double ty = (double)state.objectsOfClass(CLASS_AGENT).get(0).get(ATT_Y);
        double px = (double)cargo.get(ATT_X);
        double py = (double)cargo.get(ATT_Y);
        double ph = (double)cargo.get(ATT_H);
        double pw = (double)cargo.get(ATT_W);

        return px + pw >= tx && px <= tx && py + ph >= ty && py <= ty;
    }
}

