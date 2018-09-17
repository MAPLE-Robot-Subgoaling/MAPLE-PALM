package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2.LocationBlock;
import edu.umbc.cs.maple.cleanup.state.*;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.MaskedParameterizedStateMapping;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.ParameterizedStateMapping;
import edu.umbc.cs.maple.utilities.MutableObject;

import javax.xml.stream.Location;
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
        onlyThisRoomState.setWidth(state.getWidth());
        onlyThisRoomState.setHeight(state.getHeight());
        CleanupAgent agent = state.getAgent();

        // add the agent
        CleanupAgent abstractAgent = (CleanupAgent) agent.copy();
        onlyThisRoomState.setAgent(abstractAgent);

        // get the room
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        CleanupRoom inRoom = state.roomContainingPoint(ax, ay);

        if (inRoom == null) {
            // in a door, add it
            CleanupDoor door = state.doorContainingPoint(ax, ay);
            onlyThisRoomState.addObject(door.copy());

            // add the rooms on either side of the door
            for (CleanupRoom room : state.getRooms().values()) {
                if (CleanupState.regionContainsPoint(room, ax, ay, true)) {
                    onlyThisRoomState.addObject(room.copy());
                }
            }

            // and any adjacent blocks
            for (CleanupBlock block : state.getBlocks().values()) {
                if (block instanceof LocationBlock) { continue; }
                if (Cleanup.isAdjacent(state, new String[]{block.name()})) {
                    onlyThisRoomState.addObject(block.copy());
                }
            }

            // return just these in the state
            return onlyThisRoomState;
        } else {
            // add the room
            onlyThisRoomState.addObject(inRoom.copy());
        }


        // add all blocks in the room
        for (CleanupBlock block : state.getBlocks().values()) {
            if (block instanceof LocationBlock) {
                continue;
            }
            int bx = (int) block.get(ATT_X);
            int by = (int) block.get(ATT_Y);
            if (CleanupState.regionContainsPoint(inRoom, bx, by, true)) {
                onlyThisRoomState.addObject(block.copy());
            }
        }

        // add all doors around the room
        for (CleanupDoor door : state.getDoors().values()) {
            int dx = (int) door.get(ATT_X);
            int dy = (int) door.get(ATT_Y);
            if (CleanupState.regionContainsPoint(inRoom, dx, dy, true)) {
                onlyThisRoomState.addObject(door.copy());
            }
        }

        return onlyThisRoomState;
    }

}
