package cleanup.hierarchies.tasks.move;

import cleanup.Cleanup;
import cleanup.state.CleanupBlock;

import java.util.Arrays;
import java.util.List;

import static cleanup.Cleanup.*;

public class MoveBlock extends CleanupBlock {

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_X,
            Cleanup.ATT_Y,
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR,
            Cleanup.ATT_REGION
    );

    public MoveBlock(String name, int x, int y, String shape, String color, String blockInRegion) {
        super(name, x, y, shape, color);
        set(ATT_REGION, blockInRegion);
    }

    public MoveBlock(CleanupBlock base, String blockInRegion) {
        this(base.name(),
            (int) base.get(Cleanup.ATT_X),
            (int) base.get(Cleanup.ATT_Y),
            (String) base.get(Cleanup.ATT_SHAPE),
            (String) base.get(Cleanup.ATT_COLOR),
            blockInRegion
        );
    }

    @Override
    public String className() {
        return CLASS_BLOCK;
    }

    @Override
    public MoveBlock copyWithName(String objectName) {
        return new MoveBlock(objectName,
                (int) get(Cleanup.ATT_X),
                (int) get(Cleanup.ATT_Y),
                (String) get(ATT_SHAPE),
                (String) get(ATT_COLOR),
                (String) get(ATT_REGION));
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
