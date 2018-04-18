package taxi.hierarchies.tasks.put;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

public class DropoffActionType extends ObjectParameterizedActionType {
    public DropoffActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiPutState state = (TaxiPutState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        String taxiLoc = (String)state.getTaxiAtt(TaxiPutDomain.ATT_TAXI_LOCATION);
        return ((String)passenger.get(TaxiPutDomain.ATT_LOCATION)).equals(TaxiPutDomain.IN_TAXI)
                && !taxiLoc.equals(TaxiPutDomain.ON_ROAD);
    }
}

