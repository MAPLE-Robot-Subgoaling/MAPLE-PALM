package liftCopter.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;

public class LiftCopterAgent extends MutableObject {
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_VX,
            ATT_VY
    );

    public LiftCopterAgent(String name, double x, double y) {
        this(name, x, y, 0.0, 0.0);
    }
    public LiftCopterAgent(String name, double x, double y, double vx, double vy) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_VX, vx);
        this.set(ATT_VY, vy);

        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public LiftCopterAgent copy() {
        return (LiftCopterAgent) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LiftCopterAgent(
                objectName,
                (Double) get(ATT_X),
                (Double) get(ATT_Y),
                (Double) get(ATT_VX),
                (Double) get(ATT_VY)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
}
