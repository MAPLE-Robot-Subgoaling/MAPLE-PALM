package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put.state.LCPutState;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class PutPutdownActionType extends ObjectParameterizedActionType {
    public PutPutdownActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        LCPutState state = (LCPutState) s;
        if (state.getAgentAtt(ATT_LOCATION).equals(ATT_VAL_CRASHED)) { return false; }
        String[] params = objectParameterizedAction.getObjectParameters();
        String passengerName = params[0];
        ObjectInstance passenger = state.object(passengerName);
        String taxiLoc = (String)state.getAgentAtt(ATT_LOCATION);
        return ((String)passenger.get(ATT_LOCATION)).equals(ATT_VAL_PICKED_UP)
                && !taxiLoc.equals(ATT_VAL_IN_AIR);
    }
}

