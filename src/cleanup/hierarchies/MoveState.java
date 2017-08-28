package cleanup.hierarchies;

import cleanup.state.CleanupBlock;
import cleanup.state.CleanupDoor;
import cleanup.state.CleanupRoom;
import cleanup.state.CleanupState;
import org.apache.commons.lang3.mutable.Mutable;
import utilities.MutableObject;

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

}
