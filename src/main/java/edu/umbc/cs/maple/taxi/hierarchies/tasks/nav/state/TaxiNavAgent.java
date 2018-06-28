package edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiNavAgent extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y
    );

    public TaxiNavAgent(String name, int x, int y) {
        this(name, (Object) x, (Object) y);
    }

    private TaxiNavAgent(String name, Object x, Object y) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_TAXI;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiNavAgent(
                objectName,
                get(ATT_X),
                get(ATT_Y));
    }

    @Override
    public TaxiNavAgent copy() {
        return (TaxiNavAgent) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
