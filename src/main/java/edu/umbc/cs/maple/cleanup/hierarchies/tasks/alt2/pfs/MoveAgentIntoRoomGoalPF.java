package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class MoveAgentIntoRoomGoalPF extends PropositionalFunction {

    public MoveAgentIntoRoomGoalPF(){
        super("", new String[]{});
        name = this.getClass().getSimpleName();
    }

    public MoveAgentIntoRoomGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        if (state.numObjects() == 0) { return true; }
        String doorName = params[0];
        String roomName = params[1];
        ObjectInstance agent = state.getAgent();
        ObjectInstance door = state.object(doorName);
        ObjectInstance room = state.object(roomName);
        if (door == null) {
            return false;
        }
        if (room == null) {
            return false;
        }
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        boolean agentInRoom = CleanupState.regionContainsPoint(room, ax, ay, false);
        if (agentInRoom) {
            return true;
        }
        return false;
    }

}