package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.nav.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class LCNavLocation extends MutableObject {

    /**
     * standard x, y, color
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_H,
            ATT_W
    );
    public LCNavLocation() {
        // for de/serialization
    }

    public LCNavLocation(String name, double x, double y, double h, double w) {
        this(name, (Object) x, (Object) y, (Object) h, (Object) w);
    }

    private LCNavLocation(String name, Object x, Object y, Object h, Object w) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_H, h);
        this.set(ATT_W, w);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_LOCATION;
    }

    @Override
    public LCNavLocation copy() {
        return (LCNavLocation) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCNavLocation(
                objectName,
                get(ATT_X),
                get(ATT_Y),
                get(ATT_H),
                get(ATT_W)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
}
