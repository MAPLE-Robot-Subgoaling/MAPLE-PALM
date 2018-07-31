package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.LCGetPutState;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.ATT_LOCATION;

public class NavigateActionType extends ObjectParameterizedActionType {

    public NavigateActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        LCGetPutState state = (LCGetPutState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String locName = params[0];
        ObjectInstance location = state.object(locName);
        return !location.name().equals(state.getAgentAtt(ATT_LOCATION));
    }
}
