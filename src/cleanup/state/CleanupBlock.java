package cleanup.state;

import burlap.mdp.core.state.annotations.DeepCopyState;
import cleanup.Cleanup;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

@DeepCopyState
public class CleanupBlock extends MutableObject {

    public CleanupBlock() {

    }

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_X,
            Cleanup.ATT_Y,
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR
    );

    public CleanupBlock(String name, int x, int y, String shape, String color) {
        this.set(Cleanup.ATT_X, x);
        this.set(Cleanup.ATT_Y, y);
        this.set(Cleanup.ATT_LEFT, x);
        this.set(Cleanup.ATT_RIGHT, x);
        this.set(Cleanup.ATT_BOTTOM, y);
        this.set(Cleanup.ATT_TOP, y);
        this.set(Cleanup.ATT_SHAPE, shape);
        this.set(Cleanup.ATT_COLOR, color);
        this.name = name;
    }

    @Override
    public String className() {
        return Cleanup.CLASS_BLOCK;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public CleanupBlock copyWithName(String objectName) {
        return new CleanupBlock(objectName,
                (int) get(Cleanup.ATT_X),
                (int) get(Cleanup.ATT_Y),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR)
        );
    }

}
