package cleanup.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;
import cleanup.Cleanup;
import utilities.MutableObject;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@DeepCopyState
public class CleanupDoor extends MutableObject {

    public CleanupDoor() {

    }

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_X,
            Cleanup.ATT_Y,
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_LOCKED,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR
    );

    public CleanupDoor(String name, int left, int right, int bottom, int top, String locked, String shape, String color) {
        this.set(Cleanup.ATT_LEFT, left);
        this.set(Cleanup.ATT_X, left);
        this.set(Cleanup.ATT_RIGHT, right);
        this.set(Cleanup.ATT_BOTTOM, bottom);
        this.set(Cleanup.ATT_Y, bottom);
        this.set(Cleanup.ATT_TOP, top);
        this.set(Cleanup.ATT_LOCKED, locked);
        this.set(Cleanup.ATT_SHAPE, shape);
        this.set(Cleanup.ATT_COLOR, color);
        this.name = name;
    }

    public CleanupDoor(String name, int left, int right, int bottom, int top, String lockableState, String color) {
        this(name, left, right, bottom, top, lockableState, Cleanup.SHAPE_DOOR, color);
    }

    public CleanupDoor(String name, int left, int right, int bottom, int top, String lockableState) {
        this(name, left, right, bottom, top, lockableState, Cleanup.COLOR_GRAY);
    }

    @Override
    public String className() {
        return Cleanup.CLASS_DOOR;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public CleanupDoor copyWithName(String objectName) {
        return new CleanupDoor(objectName,
                (int) get(Cleanup.ATT_LEFT),
                (int) get(Cleanup.ATT_RIGHT),
                (int) get(Cleanup.ATT_BOTTOM),
                (int) get(Cleanup.ATT_TOP),
                (String) get(Cleanup.ATT_LOCKED),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR)
        );
    }

}
