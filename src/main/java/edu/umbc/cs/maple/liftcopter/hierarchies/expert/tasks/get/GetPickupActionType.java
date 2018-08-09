package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state.LCGetState;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;


public class GetPickupActionType extends ObjectParameterizedActionType {
    public GetPickupActionType() {
        super(ACTION_PICKUP, new String[]{CLASS_CARGO});
    }

    public GetPickupActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        LCGetState state = (LCGetState) s;
        ObjectInstance agent = state.objectsOfClass(CLASS_AGENT).get(0);
        String agentLocation = (String) agent.get(ATT_LOCATION);
        if (agentLocation.equals(ATT_VAL_CRASHED)) { return false; }
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        // passenger location is not IN_TAXI if it matches Taxi location
        boolean atPassenger =  passenger.get(ATT_LOCATION).equals(agentLocation);
        return atPassenger;
    }
}

