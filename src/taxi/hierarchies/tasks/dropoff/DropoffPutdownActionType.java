package taxi.hierarchies.tasks.dropoff;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;

public class DropoffPutdownActionType extends ObjectParameterizedActionType {
    public DropoffPutdownActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiDropoffState state = (TaxiDropoffState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        String ploc = (String)passenger.get(TaxiDropoffDomain.ATT_LOCATION);
        // return if in taxi and not on road
        return !(ploc.equals(TaxiDropoffDomain.NOT_IN_TAXI) || ploc.equals(TaxiDropoffDomain.ON_ROAD));
    }
}

