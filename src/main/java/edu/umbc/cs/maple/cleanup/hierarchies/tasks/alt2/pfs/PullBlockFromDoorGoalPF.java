package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class PullBlockFromDoorGoalPF extends PropositionalFunction {

    public PullBlockFromDoorGoalPF(){
        super("", new String[]{});
        name = this.getClass().getSimpleName();
    }

    public PullBlockFromDoorGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        if (state.numObjects() == 0) { return true; }
        String objectName = params[0];
        String doorName = params[1];
        String roomName = params[2];
        ObjectInstance object = state.object(objectName);
        ObjectInstance door = state.object(doorName);
        ObjectInstance room = state.object(roomName);
        if (door == null) {
            return false;
        }
        if (object == null) {
            return false;
        }
        if (room == null) {
            return false;
        }
        int ox = (int) object.get(ATT_X);
        int oy = (int) object.get(ATT_Y);
        boolean objectInRoom = CleanupState.regionContainsPoint(room, ox, oy, false);
        return objectInRoom;
    }

}