package cleanup.hierarchies.tasks.move;

import burlap.mdp.core.state.annotations.DeepCopyState;
import cleanup.Cleanup;
import cleanup.state.CleanupAgent;

import java.util.Arrays;
import java.util.List;

import static cleanup.Cleanup.*;
import static cleanup.Cleanup.ATT_COLOR;

@DeepCopyState
public class MoveAgent extends CleanupAgent {


    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_X,
            Cleanup.ATT_Y,
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_DIR,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR,
            Cleanup.ATT_REGION
    );

    public MoveAgent() {

    }

    public MoveAgent(String name, int x, int y, String direction, String shape, String color, String region) {
        super(name, x, y, direction, shape, color);
        this.set(Cleanup.ATT_REGION, region);
    }

    public MoveAgent(CleanupAgent base, String inRegion) {
        this(base.name(),
            (int) base.get(Cleanup.ATT_X),
            (int) base.get(Cleanup.ATT_Y),
            (String) base.get(Cleanup.ATT_DIR),
            (String) base.get(Cleanup.ATT_SHAPE),
            (String) base.get(Cleanup.ATT_COLOR),
            inRegion
        );
    }

    @Override
    public String className() {
        return Cleanup.CLASS_AGENT;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public MoveAgent copy() {
        return this.copyWithName(this.name());
    }

    @Override
    public MoveAgent copyWithName(String objectName) {
        return new MoveAgent(objectName,
                (int) get(Cleanup.ATT_X),
                (int) get(Cleanup.ATT_Y),
                (String) get(Cleanup.ATT_DIR),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR),
                (String) get(Cleanup.ATT_REGION));
    }

}
