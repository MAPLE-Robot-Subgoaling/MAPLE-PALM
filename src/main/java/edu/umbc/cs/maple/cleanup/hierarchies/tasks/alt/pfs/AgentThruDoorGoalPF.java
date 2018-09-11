package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.pfs;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;

public class AgentThruDoorGoalPF extends PropositionalFunction {

    public AgentThruDoorGoalPF(){
        super("AgentThruDoorGoalPF", new String[]{});
    }

    public AgentThruDoorGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState s, String[] params) {
        CleanupState state = (CleanupState) s;
        return isTrue(state, params);
    }

    public static boolean isTrue(CleanupState state, String[] params) {
        String doorName = params[0];
        String roomName = params[1];
        ObjectInstance agent = state.getAgent();
        ObjectInstance door = state.object(doorName);
        ObjectInstance room = state.object(roomName);
        if (room != null) {
            boolean inTheRoom = state.isObjectInRoom(agent, (CleanupRoom) room);
            if (inTheRoom) { return true; }
            else { return false; }
        }
        if (door != null) {
            boolean inTheDoor = state.isObjectInDoor(agent, (CleanupDoor) door);
            if (!inTheDoor) { return true; }
            else { return false; }
        }
        return false;
    }

}