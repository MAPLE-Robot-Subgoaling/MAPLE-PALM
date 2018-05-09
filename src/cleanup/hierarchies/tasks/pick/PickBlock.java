package cleanup.hierarchies.tasks.pick;

import cleanup.hierarchies.tasks.move.MoveBlock;

import static cleanup.Cleanup.*;

public class PickBlock extends MoveBlock {
    public PickBlock(String name, String shape, String color, String blockInRegion) {
        super(name, shape, color, blockInRegion);
    }

    @Override
    public PickBlock copyWithName(String objectName) {
        return new PickBlock(objectName,
                (String) get(ATT_SHAPE),
                (String) get(ATT_COLOR),
                (String) get(ATT_REGION));
    }
}
