package cleanup.hierarchies.tasks.move;

import cleanup.Cleanup;
import cleanup.state.CleanupDoor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cleanup.Cleanup.ATT_CONNECTED;
import static cleanup.Cleanup.ATT_LOCKED;
import static cleanup.Cleanup.CLASS_DOOR;

public class MoveDoor extends CleanupDoor {

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_X,
            Cleanup.ATT_Y,
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_LOCKED,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR,
            ATT_CONNECTED
    );

    public MoveDoor(String name, int left, int right, int bottom, int top, String locked, String shape, String color, HashSet<String> connected) {
        super(name, left, right, bottom, top, locked, shape, color);
        this.set(ATT_CONNECTED, connected);
    }

    public MoveDoor(CleanupDoor base, HashSet<String> connected) {
        this(base.name(),
                (int) base.get(Cleanup.ATT_LEFT),
                (int) base.get(Cleanup.ATT_RIGHT),
                (int) base.get(Cleanup.ATT_BOTTOM),
                (int) base.get(Cleanup.ATT_TOP),
                (String) base.get(Cleanup.ATT_LOCKED),
                (String) base.get(Cleanup.ATT_SHAPE),
                (String) base.get(Cleanup.ATT_COLOR),
                connected
        );
    }

    @Override
    public String className() {
        return CLASS_DOOR;
    }

    @Override
    public MoveDoor copyWithName(String objectName) {
        return new MoveDoor(objectName,
                (int) get(Cleanup.ATT_LEFT),
                (int) get(Cleanup.ATT_RIGHT),
                (int) get(Cleanup.ATT_BOTTOM),
                (int) get(Cleanup.ATT_TOP),
                (String) get(Cleanup.ATT_LOCKED),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR),
                (HashSet<String>) get(ATT_CONNECTED));
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
