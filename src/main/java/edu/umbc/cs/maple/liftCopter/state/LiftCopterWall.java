package edu.umbc.cs.maple.liftCopter.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LiftCopterWall extends MutableObject {

    /**
     * contains startx and y and length and if it is horizontal
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_START_X,
            ATT_START_Y,
            ATT_HEIGHT,
            ATT_WIDTH
    );

    public LiftCopterWall(String name, double startX, double startY, double width, double height) {
        this(name, (Object) startX, (Object) startY, (Object) width, (Object) height);
    }

    public LiftCopterWall(String name, Object startX, Object startY, Object width, Object height) {
        this.set(ATT_START_X, startX);
        this.set(ATT_START_Y, startY);
        this.set(ATT_HEIGHT, height);
        this.set(ATT_WIDTH, width);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_WALL;
    }

    @Override
    public LiftCopterWall copy() {
        return (LiftCopterWall) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LiftCopterWall(
                objectName,
                get(ATT_START_X),
                get(ATT_START_Y),
                get(ATT_HEIGHT),
                get(ATT_WIDTH)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
}
