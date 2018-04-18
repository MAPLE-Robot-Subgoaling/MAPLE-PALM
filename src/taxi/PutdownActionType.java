package taxi;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.state.TaxiState;
import taxi.state.TaxiPassenger;

public class PutdownActionType extends ObjectParameterizedActionType {
    public PutdownActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiState state = (TaxiState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        TaxiPassenger passenger = (TaxiPassenger)state.object(passengerName);

        // Can't put down a passenger not in the taxi
        if(!(boolean)passenger.get(Taxi.ATT_IN_TAXI)) {
            return false;
        }

        int px = (int)passenger.get(Taxi.ATT_X);
        int py = (int)passenger.get(Taxi.ATT_Y);
        for(String loc : state.getLocations()) {
            int lx = (int)state.getLocationAtt(loc, Taxi.ATT_X);
            int ly = (int)state.getLocationAtt(loc, Taxi.ATT_Y);
            // must be at ANY location in order to put the passenger down
            if(lx == px && ly == py) {
                return true;
            }
        }

        // the passenger is in the taxi but not at a location
        return false;
    }
}

