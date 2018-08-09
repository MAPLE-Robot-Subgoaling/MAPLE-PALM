package edu.umbc.cs.maple.cleanup.hierarchies.tasks.move;

import edu.umbc.cs.maple.cleanup.state.CleanupBlock;
import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
import edu.umbc.cs.maple.cleanup.state.CleanupRoom;
import edu.umbc.cs.maple.cleanup.state.CleanupState;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.MoveMapper.toNameMap;
import static edu.umbc.cs.maple.cleanup.hierarchies.tasks.move.MoveMapper.toSuperMap;

public class MoveState extends CleanupState {

    public MoveState(MoveAgent agent, List<? extends CleanupBlock> blocks, List<? extends CleanupDoor> doors, List<? extends CleanupRoom> rooms) {
        this.agent  = agent;
        this.blocks = toSuperMap(blocks);
        this.doors  = toSuperMap(doors);
        this.rooms  = toSuperMap(rooms);
    }




//    @Override
//    public CleanupAgent touchAgent() {
//        if (agent == null) return null;
//        this.agent = (CleanupAgent) agent.copy();
//        return agent;
//    }
//
//    @Override
//    public CleanupBlock touchBlock(String name) {
//        CleanupBlock n = (CleanupBlock) blocks.get(name).copy();
//        touchBlocks().remove(name);
//        blocks.put(name, n);
//        return n;
//    }
//
//    @Override
//    public CleanupDoor touchDoor(String name) {
//        CleanupDoor n = (CleanupDoor) doors.get(name).copy();
//        touchDoors().remove(name);
//        doors.put(name, n);
//        return n;
//    }
//
//    @Override
//    public CleanupRoom touchRoom(String name) {
//        CleanupRoom n = (CleanupRoom) rooms.get(name).copy();
//        touchRooms().remove(name);
//        rooms.put(name, n);
//        return n;
//    }

    @Override
    public CleanupState copy() {
        // all cleanup states are using copy-on-write for shallow copy state (@ShallowCopyState)
        return new MoveState(
                (MoveAgent)agent,
                new ArrayList<>(blocks.values()),
                new ArrayList<>(doors.values()),
                new ArrayList<>(rooms.values()));
    }

}
