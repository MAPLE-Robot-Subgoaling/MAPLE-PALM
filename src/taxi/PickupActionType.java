package taxi;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

import static taxi.TaxiConstants.*;

public class PickupActionType extends ObjectParameterizedActionType {
    public PickupActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiState state = (TaxiState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        TaxiPassenger passenger = (TaxiPassenger)state.object(passengerName);

        // Can't pick up a passenger already in the taxi
        if((boolean)passenger.get(ATT_IN_TAXI)) {
            return false;
        }

        int tx = (int)state.getTaxi().get(ATT_X);
        int ty = (int)state.getTaxi().get(ATT_Y);
        int px = (int)passenger.get(ATT_X);
        int py = (int)passenger.get(ATT_Y);

        return tx == px && ty == py;
    }
}

