package liftCopter.hierarchies.expert.tasks.nav.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static liftCopter.LiftCopterConstants.*;

public class LCNavAgent extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_VX,
            ATT_VY,
            ATT_H,
            ATT_W
            );

    public LCNavAgent(String name, double x, double y, double h, double w) {
        this(name, x, y, 0.0, 0.0, h, w);
    }
    public LCNavAgent(String name, double x, double y, double vx, double vy, double h, double w) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_VX, vx);
        this.set(ATT_VY, vy);
        this.set(ATT_H, h);
        this.set(ATT_W, w);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public LCNavAgent copy() {
        return (LCNavAgent) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCNavAgent(
                objectName,
                (Double) get(ATT_X),
                (Double) get(ATT_Y),
                (Double) get(ATT_VX),
                (Double) get(ATT_VY),
                (Double) get(ATT_H),
                (Double) get(ATT_W)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
}
