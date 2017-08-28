package cleanup.hierarchies;

import burlap.mdp.core.oo.state.ObjectInstance;
import cleanup.state.CleanupBlock;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static cleanup.Cleanup.*;

public class MoveBlock extends CleanupBlock {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_SHAPE,
            ATT_COLOR,
            ATT_REGION
    );

    public MoveBlock(String name, String shape, String color, String blockInRegion) {
        setName(name);
        set(ATT_SHAPE, shape);
        set(ATT_COLOR, color);
        set(ATT_REGION, blockInRegion);
    }

    @Override
    public String className() {
        return CLASS_BLOCK;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new MoveBlock(objectName,
                (String) get(ATT_SHAPE),
                (String) get(ATT_COLOR),
                (String) get(ATT_REGION));
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
