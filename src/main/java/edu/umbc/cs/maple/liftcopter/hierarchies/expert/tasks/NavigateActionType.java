package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.LCGetPutState;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.ATT_LOCATION;
import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.ATT_VAL_CRASHED;

public class NavigateActionType extends ObjectParameterizedActionType {

    public NavigateActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {

        LCGetPutState state = (LCGetPutState) s;
        if (state.getAgentAtt(ATT_LOCATION).equals(ATT_VAL_CRASHED)) { return false; }
        String[] params = objectParameterizedAction.getObjectParameters();
        String locName = params[0];
        ObjectInstance location = state.object(locName);
        return !location.name().equals(state.getAgentAtt(ATT_LOCATION));
    }
}
