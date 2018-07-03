package edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiPutPassenger extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_GOAL_LOCATION,
            ATT_LOCATION
            );

    public TaxiPutPassenger(String name, String goalLocation, String location){
        this(name, (Object) goalLocation, (Object) location);
    }

    private TaxiPutPassenger(String name, Object goalLocation, Object location){
        this.set(ATT_GOAL_LOCATION, goalLocation);
        this.set(ATT_LOCATION, location);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_PASSENGER;
    }

    @Override
    public TaxiPutPassenger copy() {
        return (TaxiPutPassenger) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiPutPassenger(
                objectName,
                get(ATT_GOAL_LOCATION),
                get(ATT_LOCATION)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }}
