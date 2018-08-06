package edu.umbc.cs.maple.cleanup.hierarchies.tasks.root;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.MoveAgent;
import edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.MoveState;
import edu.umbc.cs.maple.cleanup.state.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_X;
import static edu.umbc.cs.maple.cleanup.Cleanup.ATT_Y;

public class CleanupRootAbstractMapper implements StateMapping {
    @Override
    public State mapState(State state) {
        CleanupState s = (CleanupState) state;


        CleanupAgent agent = s.getAgent();
        int ax = (int) agent.get(ATT_X);
        int ay = (int) agent.get(ATT_Y);
        //abstract away agent's direction
        CleanupAgent cleanupAgent = new CleanupAgent(ax, ay, "");

        //abstract away colors and shape for rooms
        List<CleanupRoom> rooms = new ArrayList<CleanupRoom>(s.getRooms().values());
        Map<String, CleanupRoom> abstractRooms = new HashMap<>();
        for(CleanupRoom r : rooms){
            abstractRooms.put(r.getName(),
                    new CleanupRoom(r.getName(),
                        (int)r.get(Cleanup.ATT_LEFT),
                        (int)r.get(Cleanup.ATT_RIGHT),
                        (int)r.get(Cleanup.ATT_BOTTOM),
                        (int)r.get(Cleanup.ATT_TOP),
                        "",
                        ""));
        }

        //abstract away colors and shapes for blocks
        List<CleanupBlock> blocks = new ArrayList<>(s.getBlocks().values());
        Map<String, CleanupBlock> abstractBlocks = new HashMap<>();
        for(CleanupBlock b : blocks){
            abstractBlocks.put(
                    b.getName(),
                    new CleanupBlock(b.getName(),
                        (int)b.get(Cleanup.ATT_X),
                        (int)b.get(Cleanup.ATT_Y),
                        "",
                        ""));
        }

        //abstract away locked, colors, and shapes of doors
        List<CleanupDoor> doors = new ArrayList<>(s.getDoors().values());
        Map<String,CleanupDoor> abstractDoors = new HashMap<>();
        for(CleanupDoor d : doors){
            abstractDoors.put(d.getName(),
                    new CleanupDoor(d.getName(),
                        (int)d.get(Cleanup.ATT_LEFT),
                        (int)d.get(Cleanup.ATT_RIGHT),
                        (int)d.get(Cleanup.ATT_BOTTOM),
                        (int)d.get(Cleanup.ATT_TOP),
                        "",
                        "",
                        ""));
        }

        //w h agent block room door
        return new CleanupState(s.getWidth(), s.getHeight(),cleanupAgent, abstractBlocks, abstractRooms, abstractDoors);
    }
}
