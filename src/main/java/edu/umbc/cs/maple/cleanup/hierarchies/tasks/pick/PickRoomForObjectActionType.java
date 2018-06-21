package edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_REGION;

public class PickRoomForObjectActionType extends ObjectParameterizedActionType {

    public PickRoomForObjectActionType(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        OOState state = (OOState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String objectName = params[0];
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        // applicable if object NOT already in the region
        return !object.get(ATT_REGION).equals(regionName);
    }

}
