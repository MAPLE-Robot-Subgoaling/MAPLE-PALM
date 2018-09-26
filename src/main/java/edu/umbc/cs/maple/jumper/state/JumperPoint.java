package edu.umbc.cs.maple.jumper.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.jumper.JumperConstants;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_X;
import static edu.umbc.cs.maple.jumper.JumperConstants.ATT_Y;

public abstract class JumperPoint extends MutableObject {

    private final static List<Object> keys = Arrays.asList(
            JumperConstants.ATT_X,
            JumperConstants.ATT_Y
    );

    public JumperPoint() {
        // for de/serialization
    }

    public JumperPoint(String name, double x, double y) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.name = name;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
