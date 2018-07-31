package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.ATT_LOCATION;
import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.CLASS_CARGO;


public class LCGetCargo extends MutableObject {

    /**
     * current location, whether they are in taxi, the goal, whether they haven been picked up
     * whether they have just been picked up and haven't changed goal
     */
    private final static List<Object> keys = Arrays.<Object>asList( ATT_LOCATION );

    public LCGetCargo(String name, String currentLocation) {
        this(name, (Object) currentLocation);
    }

    private LCGetCargo(String name, Object currentLocation){
        this.set(ATT_LOCATION, currentLocation);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_CARGO;
    }

    @Override
    public LCGetCargo copy() {
        return (LCGetCargo) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCGetCargo( objectName, get(ATT_LOCATION) );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }}
