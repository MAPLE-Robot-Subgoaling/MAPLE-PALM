package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickAgent;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickBlock;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickRoom;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.pick.PickState;
import edu.umbc.cs.maple.cleanup.state.*;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.cleanup.Cleanup.*;

public class CleanupRootMapper implements StateMapping {

    @Override
    public State mapState(State sIn) {

        CleanupState s = (CleanupState) sIn;

        CleanupAgent agent = s.getAgent();
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        CleanupRoom agentInRoom = s.roomContainingPoint(ax, ay);
        String agentInRegion = null;
        if (agentInRoom == null) {
            // special edge case, agent not in any room but in a door
            CleanupDoor agentInDoor = s.doorContainingPoint(ax, ay);
            agentInRegion = agentInDoor.name();
        } else {
            agentInRegion = agentInRoom.name();
        }
        PickAgent pickAgent = new PickAgent(agent.name(), agentInRegion);

        List<CleanupRoom> rooms = new ArrayList<>(s.getRooms().values());
        List<PickRoom> abstractRooms = new ArrayList<PickRoom>();
        for(CleanupRoom r : rooms){
            PickRoom room = new PickRoom(r.name(), (String) r.get(ATT_COLOR));
            abstractRooms.add(room);
        }

        List<CleanupBlock> blocks = new ArrayList<>(s.getBlocks().values());
        List<PickBlock> abstractBlocks = new ArrayList<PickBlock>();
        for(CleanupBlock b : blocks){

            int bx = (int) b.get(ATT_X);
            int by = (int) b.get(ATT_Y);
            CleanupRoom blockInRoom = s.roomContainingPoint(bx, by);

            String blockInRegion;
            if(blockInRoom != null){
                blockInRegion = blockInRoom.name();
            }
            else{
                CleanupDoor blockInDoor = s.doorContainingPoint(bx, by);
                blockInRegion = blockInDoor.name();
            }
            PickBlock ab  = new PickBlock(b.name(), (String) b.get(ATT_SHAPE), (String) b.get(ATT_COLOR), blockInRegion);
            abstractBlocks.add(ab);
        }

        return new PickState(pickAgent, abstractBlocks, abstractRooms);
    }


}