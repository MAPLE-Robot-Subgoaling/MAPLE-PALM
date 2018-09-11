package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt2;

import edu.umbc.cs.maple.cleanup.Cleanup;
import edu.umbc.cs.maple.cleanup.state.CleanupBlock;

import java.util.Arrays;
import java.util.List;

public class LocationBlock extends CleanupBlock {

    public LocationBlock() {

    }

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_REGION,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR
    );

    public LocationBlock(String name, String region, String shape, String color) {
        this.set(Cleanup.ATT_REGION, region);
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
        return new LocationBlock(objectName,
                (String) get(Cleanup.ATT_REGION),
                (String) get(Cleanup.ATT_SHAPE),
                (String) get(Cleanup.ATT_COLOR)
        );
    }

}
