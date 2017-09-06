package taxi;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;

public class PickupActionType extends ObjectParameterizedActionType {
    public PickupActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        TaxiBringonState state = (TaxiBringonState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        String tloc = (String)state.getTaxiAtt(TaxiBringonDomain.ATT_LOCATION);
        boolean inTaxi = passenger.get(TaxiBringonDomain.ATT_LOCATION).equals(TaxiBringonDomain.IN_TAXI);
        return !inTaxi && tloc.equals(passenger.get(TaxiBringonDomain.ATT_LOCATION));
    }
}

