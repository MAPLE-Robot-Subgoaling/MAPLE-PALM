package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.state.annotations.ShallowCopyState;
import cleanup.hierarchies.tasks.move.MoveState;
import cleanup.state.CleanupBlock;
import cleanup.state.CleanupRoom;
import cleanup.state.CleanupState;

import java.util.ArrayList;
import java.util.List;

@ShallowCopyState
public class PickState extends MoveState {

    public PickState(PickAgent agent, List<? extends CleanupBlock> blocks, List<? extends CleanupRoom> rooms) {
        super(agent, blocks, new ArrayList<>(), rooms);
    }

//    @Override
//    public PickAgent touchAgent() {
//        if (agent == null) return null;
//        this.agent = (PickAgent) agent.copy();
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
        return new PickState((PickAgent)agent, new ArrayList<>(blocks.values()), new ArrayList<>(rooms.values()));
    }

}
