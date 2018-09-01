package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.*;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.MaskedParameterizedStateMapping;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.ParameterizedStateMapping;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class OnlyThisRoomMapper implements StateMapping {

    public static OnlyThisRoomMapper mapper = new OnlyThisRoomMapper();

    @Override
    public State mapState(State s) {

        CleanupState state = (CleanupState) s;
        CleanupState onlyThisRoomState = new CleanupState();
        CleanupAgent agent = state.getAgent();

        // add the agent
        CleanupAgent abstractAgent = (CleanupAgent) agent.copy();
        onlyThisRoomState.setAgent(abstractAgent);

        // get the room
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        CleanupRoom inRoom = state.roomContainingPoint(ax, ay);

        if (inRoom == null) {
            // in a door, add it and return
            CleanupDoor door = state.doorContainingPoint(ax, ay);
            onlyThisRoomState.addObject(door.copy());
            return onlyThisRoomState;
        } else {
            // add the room
            onlyThisRoomState.addObject(inRoom.copy());
        }


        // add all blocks in the room
        for (CleanupBlock block : state.getBlocks().values()) {
            int bx = (int) block.get(ATT_X);
            int by = (int) block.get(ATT_Y);
            if (state.roomContainingPointIncludingBorder(bx, by).equals(inRoom)) {
                onlyThisRoomState.addObject(block.copy());
            }
        }

        // add all doors around the room
        for (CleanupDoor door : state.getDoors().values()) {
            int dx = (int) door.get(ATT_X);
            int dy = (int) door.get(ATT_Y);
            if (state.roomContainingPointIncludingBorder(dx, dy).equals(inRoom)) {
                onlyThisRoomState.addObject(door.copy());
            }
        }

        return onlyThisRoomState;
    }

}
