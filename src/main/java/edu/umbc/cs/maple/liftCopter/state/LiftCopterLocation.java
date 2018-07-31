package edu.umbc.cs.maple.liftCopter.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LiftCopterLocation extends MutableObject {

    /**
     * standard x, y, color
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_H,
            ATT_W,
            ATT_COLOR
    );

    public LiftCopterLocation(String name, double x, double y, double h, double w, String color) {
        this(name, (Object) x, (Object) y, (Object) h, (Object) w, (Object) color);
    }

    private LiftCopterLocation(String name, Object x, Object y, Object h, Object w, Object color) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_H, h);
        this.set(ATT_W, w);
        this.set(ATT_COLOR, color);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_LOCATION;
    }

    @Override
    public LiftCopterLocation copy() {
        return (LiftCopterLocation) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LiftCopterLocation(
                objectName,
                get(ATT_X),
                get(ATT_Y),
                get(ATT_H),
                get(ATT_W),
                get(ATT_COLOR)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
}
