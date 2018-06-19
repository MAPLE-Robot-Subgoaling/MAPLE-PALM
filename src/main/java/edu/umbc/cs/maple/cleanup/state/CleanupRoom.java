package edu.umbc.cs.maple.cleanup.state;

import burlap.mdp.core.state.annotations.DeepCopyState;
import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

@DeepCopyState
public class CleanupRoom extends MutableObject {

    public CleanupRoom() {

    }

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_COLOR,
            Cleanup.ATT_SHAPE
    );

    public CleanupRoom(String name, int left, int right, int bottom, int top, String color, String shape) {
        this.set(Cleanup.ATT_LEFT, left);
        this.set(Cleanup.ATT_RIGHT, right);
        this.set(Cleanup.ATT_BOTTOM, bottom);
        this.set(Cleanup.ATT_TOP, top);
        this.set(Cleanup.ATT_COLOR, color);
        this.set(Cleanup.ATT_SHAPE, shape);
        this.name = name;
    }

    @Override
    public String className() {
        return Cleanup.CLASS_ROOM;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public CleanupRoom copyWithName(String objectName) {
        return new CleanupRoom(objectName,
                (int) get(Cleanup.ATT_LEFT),
                (int) get(Cleanup.ATT_RIGHT),
                (int) get(Cleanup.ATT_BOTTOM),
                (int) get(Cleanup.ATT_TOP),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR)
        );
    }

}
