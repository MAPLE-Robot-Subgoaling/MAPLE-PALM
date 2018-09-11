package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt.OnlyThisRoomMapper;
import edu.umbc.cs.maple.cleanup.state.*;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class LocationMapper implements StateMapping {

    public static OnlyThisRoomMapper mapper = new OnlyThisRoomMapper();

    @Override
    public State mapState(State s) {

        CleanupState state = (CleanupState) s;
        CleanupState locationState = new CleanupState();
        CleanupAgent agent = state.getAgent();

        // add the agent
        CleanupAgent abstractAgent = (CleanupAgent) agent.copy();
        locationState.setAgent(abstractAgent);

        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        CleanupRoom inRoom = state.roomContainingPoint(ax, ay);
//        if (inRoom == null) {
//            //in a door
//            CleanupDoor door = state.doorContainingPoint(ax, ay);
//        }

        // add all blocks in the room
        for (CleanupBlock block : state.getBlocks().values()) {
            int bx = (int) block.get(ATT_X);
            int by = (int) block.get(ATT_Y);
            if (inRoom != null && CleanupState.regionContainsPoint(inRoom, bx, by, true)) {
                locationState.addObject(block.copy());
            } else if (Cleanup.isAdjacent(state,new String[]{block.name()})) {
                // if in a door, add adjacent blocks
                locationState.addObject(block.copy());
            } else {
                String region = state.regionContainingPoint(bx, by).name();
                LocationBlock clone = new LocationBlock(block.name(), region, (String) block.get(ATT_SHAPE), (String) block.get(ATT_COLOR));
                locationState.addObject(clone);
            }
        }

        // add all doors
        for (CleanupDoor door : state.getDoors().values()) {
            locationState.addObject(door.copy());
        }

        // add all rooms
        for (CleanupRoom room : state.getRooms().values()) {
            locationState.addObject(room.copy());
        }

        return locationState;
    }

}
