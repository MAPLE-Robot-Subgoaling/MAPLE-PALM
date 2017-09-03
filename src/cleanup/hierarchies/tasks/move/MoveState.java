package cleanup.hierarchies.tasks.move;

import cleanup.state.*;
import utilities.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveState extends CleanupState {

    public MoveState(MoveAgent agent, List<? extends CleanupBlock> blocks, List<? extends CleanupDoor> doors, List<? extends CleanupRoom> rooms) {
        this.agent = agent;
        this.blocks = (Map<String, CleanupBlock>) toNameMap(blocks);
        this.doors = (Map<String, CleanupDoor>) toNameMap(doors);
        this.rooms = (Map<String, CleanupRoom>) toNameMap(rooms);
    }

    public Map<String, ? extends MutableObject> toNameMap(List<? extends MutableObject> objects) {
        Map<String, MutableObject> map = new HashMap<>();
        for (MutableObject object : objects) {
            map.put(object.name(), object);
        }
        return map;
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
