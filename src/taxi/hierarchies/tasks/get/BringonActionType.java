package taxi.hierarchies.tasks.get;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierarchies.tasks.get.state.TaxiGetState;

public class BringonActionType extends ObjectParameterizedActionType {
    public BringonActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiGetState state = (TaxiGetState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        // passenger location is not IN_TAXI if it matches Taxi location
        return passenger.get(TaxiGetDomain.ATT_LOCATION).equals(state.getTaxiAtt(TaxiGetDomain.ATT_LOCATION));
    }
}

