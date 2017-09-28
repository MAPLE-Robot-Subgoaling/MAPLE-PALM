package cleanup.hierarchies.tasks.pick;

import cleanup.hierarchies.tasks.move.MoveRoom;
import cleanup.state.CleanupRoom;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static cleanup.Cleanup.ATT_COLOR;
import static cleanup.Cleanup.ATT_CONNECTED;
import static cleanup.Cleanup.CLASS_ROOM;

public class PickRoom extends MoveRoom {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_COLOR
    );

    public PickRoom(String name, String color) {
        this.setName(name);
        this.set(ATT_COLOR, color);
    }

    @Override
    public String className() {
        return CLASS_ROOM;
    }

    @Override
    public PickRoom copyWithName(String objectName) {
        return new PickRoom(objectName, (String) get(ATT_COLOR));
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }


}