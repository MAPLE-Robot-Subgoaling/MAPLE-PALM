package edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;


public class TaxiNavLocation extends MutableObject{

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y
            );

    public TaxiNavLocation(String name, int x, int y) {
        this(name, (Object) x, (Object) y);
    }

    private TaxiNavLocation(String name, Object x, Object y) {
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.setName(name);
    }
    @Override
    public String className() {
        return CLASS_LOCATION;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiNavLocation(
                objectName,
                get(ATT_X),
                get(ATT_Y));
    }

    @Override
    public TaxiNavLocation copy() {
        return (TaxiNavLocation) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
