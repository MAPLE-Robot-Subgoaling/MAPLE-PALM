package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import java.util.HashSet;

public class ObjectToRoomActionType extends ObjectParameterizedActionType {

    public ObjectToRoomActionType(){
        super("ERRORNOTSET", new String[]{});
    }

    public void setParameterClasses(String[] parameterClasses){
        this.parameterClasses = parameterClasses;
    }

    public void setParameterOrderGroup(String[] parameterOrderGroup) {
        this.parameterOrderGroup = parameterOrderGroup;
    }

    @Override
    protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
        CleanupState state = (CleanupState) s;
        String[] params = objectParameterizedAction.getObjectParameters();
        String objectName = params[0];
        String roomName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance room = state.object(roomName);
        return !((CleanupState) s).isObjectInRoom(object, (CleanupRoom) room);
    }

}
