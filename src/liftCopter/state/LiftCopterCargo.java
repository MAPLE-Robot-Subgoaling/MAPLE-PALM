package liftCopter.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;

public class LiftCopterCargo extends MutableObject {

    /**
     * x, y, whether in taxi, goal, whether they have been picked up, whether they wer just picked up
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_H,
            ATT_W,
            ATT_PICKED_UP,
            ATT_GOAL_LOCATION
    );

    public LiftCopterCargo(String name, double x, double y, double h, double w, String goalLocation) {
        this(name, (Object) x, (Object) y, (Object) h, (Object) w, (Object) goalLocation, false);
    }

    public LiftCopterCargo(String name, double x, double y, double h, double w, String goalLocation, boolean pickedUp) {
        this(name, (Object) x, (Object) y, (Object) h, (Object) w, (Object) goalLocation, (Object) pickedUp);
    }


    private LiftCopterCargo(String name, Object x, Object y, Object h, Object w, Object goalLocation, Object pickedUp) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_H, h);
        this.set(ATT_W, w);
        this.set(ATT_GOAL_LOCATION, goalLocation);
        this.set(ATT_PICKED_UP, pickedUp);
        this.setName(name);
    }


    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LiftCopterCargo(
                objectName,
                get(ATT_X),
                get(ATT_Y),
                get(ATT_H),
                get(ATT_W),
                get(ATT_GOAL_LOCATION),
                get(ATT_PICKED_UP)
        );
    }

    @Override
    public LiftCopterCargo copy() {
        return (LiftCopterCargo) copyWithName(name());
    }

    @Override
    public String className() {
        return CLASS_CARGO;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
