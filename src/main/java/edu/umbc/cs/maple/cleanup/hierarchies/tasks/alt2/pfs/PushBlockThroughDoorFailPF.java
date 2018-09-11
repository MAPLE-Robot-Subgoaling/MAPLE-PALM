package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class PushBlockThroughDoorFailPF extends PropositionalFunction {

    public PushBlockThroughDoorFailPF(){
        super("", new String[]{});
        name = this.getClass().getSimpleName();
    }

    public PushBlockThroughDoorFailPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {

        if (state.numObjects() == 0) { return false; }
        String objectName = params[0];
        String doorName = params[1];
        String roomName = params[2];
        ObjectInstance object = state.object(objectName);
        ObjectInstance door = state.object(doorName);
        ObjectInstance room = state.object(roomName);
        if (door == null) {
            return true;
        }
        if (object == null) {
            return true;
        }
        // the room may be null, since it may be on the other side of the door and the agent not in the door
        int ox = (int) object.get(ATT_X);
        int oy = (int) object.get(ATT_Y);
        boolean objectInDoor = CleanupState.regionContainsPoint(door, ox, oy, true);
        if (!objectInDoor) {
            CleanupRoom currentRoom = state.roomContainingPoint(ox, oy);
            if (currentRoom != null && !currentRoom.equals(room)) {
                // failed if the block is in a room that is not the goal room
                return true;
            }
        }
        return false;
    }

}