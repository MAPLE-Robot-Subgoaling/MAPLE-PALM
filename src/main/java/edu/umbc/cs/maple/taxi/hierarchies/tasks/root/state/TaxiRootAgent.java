package edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_LOCATION;
import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_TAXI;

public class TaxiRootAgent extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCATION
    );

    public TaxiRootAgent(String name, String location) {
        this(name, (Object)location);
    }

    private TaxiRootAgent(String name, Object location) {
        this.set(ATT_LOCATION, location);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_TAXI;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiRootAgent( objectName, get(ATT_LOCATION));
    }

    @Override
    public TaxiRootAgent copy() {
        return (TaxiRootAgent) copyWithName(name());
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
