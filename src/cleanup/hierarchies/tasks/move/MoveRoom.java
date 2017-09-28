package cleanup.hierarchies.tasks.move;

import cleanup.Cleanup;
import cleanup.state.CleanupDoor;
import cleanup.state.CleanupRoom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cleanup.Cleanup.ATT_COLOR;
import static cleanup.Cleanup.ATT_CONNECTED;
import static cleanup.Cleanup.CLASS_ROOM;


public class MoveRoom extends CleanupRoom {

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_COLOR,
            Cleanup.ATT_SHAPE,
            ATT_CONNECTED
    );

    public MoveRoom() {

    }

    public MoveRoom(String name, int left, int right, int bottom, int top, String color, String shape, Set<String> connected) {
        super(name, left, right, bottom, top, color, shape);
        this.set(ATT_CONNECTED, connected);
    }

    @Override
    public String className() {
        return CLASS_ROOM;
    }

    public MoveRoom(CleanupRoom base, HashSet<String> connected) {
        this(base.name(),
                (int) base.get(Cleanup.ATT_LEFT),
                (int) base.get(Cleanup.ATT_RIGHT),
                (int) base.get(Cleanup.ATT_BOTTOM),
                (int) base.get(Cleanup.ATT_TOP),
                (String) base.get(Cleanup.ATT_SHAPE),
                (String) base.get(Cleanup.ATT_COLOR),
                connected
        );
    }

    @Override
    public MoveRoom copyWithName(String objectName) {
        return new MoveRoom(objectName,
                (int) get(Cleanup.ATT_LEFT),
                (int) get(Cleanup.ATT_RIGHT),
                (int) get(Cleanup.ATT_BOTTOM),
                (int) get(Cleanup.ATT_TOP),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR),
                (Set<String>) this.get(ATT_CONNECTED));
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    public void addConnectedRegion(String name) {
        Set<String> connected = (Set<String>) get(ATT_CONNECTED);
        connected.add(name);
    }

}
