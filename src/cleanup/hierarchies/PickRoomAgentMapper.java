package cleanup.hierarchies;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import cleanup.state.*;

import java.util.ArrayList;
import java.util.List;

import static cleanup.Cleanup.*;
import static cleanup.Cleanup.ATT_SHAPE;

public class PickRoomAgentMapper implements StateMapping{

    @Override
    public State mapState(State sIn) {

        CleanupState s = (CleanupState) sIn;

        CleanupAgent agent = s.getAgent();
        PickAgent agentL2 = new PickAgent(agent.name(), (String) agent.get(ATT_REGION));

        List<CleanupRoom> rooms = (List<CleanupRoom>) s.getRooms().values();
        List<PickRoom> abstractRooms = new ArrayList<PickRoom>();
        for(CleanupRoom r : rooms){
            PickRoom room = new PickRoom(r.name(), (String) r.get(ATT_COLOR));
            abstractRooms.add(room);
        }

        List<CleanupBlock> blocks = (List<CleanupBlock>) s.getBlocks().values();
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

        return new PickState(agentL2, abstractBlocks, abstractRooms);
    }


}
