package taxi;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.interfaces.PassengerParameterizable;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;

import java.util.ArrayList;
import java.util.List;

public class PutdownActionType implements ActionType {

    public String typeName() {
        return Taxi.ACTION_PUTDOWN;
    }

    @Override
    public PutdownAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new PutdownAction(pass);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        PassengerParameterizable state = (PassengerParameterizable) s;
        List<Action> acts = new ArrayList<>();

        for (String pass : state.getPassengers()) {
            String location = state.getPassengerLocation(pass);
            if(! (location.equals(Taxi.ON_ROAD) || location.equals(TaxiDropoffDomain.NOT_IN_TAXI))) {
                acts.add(new PutdownAction(pass));
            }
        }

        return acts;
    }
}
