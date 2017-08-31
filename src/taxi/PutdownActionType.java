package taxi;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.state.TaxiState;

public class PutdownActionType extends ObjectParameterizedActionType {
    public PutdownActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiState state = (TaxiState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);

        // Must be in Taxi
        if (!(boolean)passenger.get(Taxi.ATT_IN_TAXI)) {
            return false;
        }

        // Must be at a depot
        int px = (int)state.getPassengerAtt(passengerName, Taxi.ATT_X);
        int py = (int)state.getPassengerAtt(passengerName, Taxi.ATT_Y);
        for(String loc : state.getLocations()) {
            int lx = (int)state.getLocationAtt(loc, Taxi.ATT_X);
            int ly = (int)state.getLocationAtt(loc, Taxi.ATT_Y);

            if(lx == px && ly == py) {
                return true;
            }
        }

        return false;
    }
}

