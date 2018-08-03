package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_BOTTOM;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_TOP;

public class ObjectInRoomGoalPF extends PropositionalFunction {

    public ObjectInRoomGoalPF(){
        super("objectInRoomGoalPF", new String[]{});
    }

    public ObjectInRoomGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        String objectName = params[0];
        String roomName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance room = state.object(roomName);
        if(object == null) object = state.object(MoveMapper.moveBlockTargetAlias);
        if (object == null || room == null) {
            return false;
        }
        return state.isObjectInRoom(object, (CleanupRoom) room);
    }

}
