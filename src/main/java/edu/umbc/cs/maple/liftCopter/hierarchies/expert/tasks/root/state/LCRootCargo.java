package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCRootCargo extends MutableObject {

    /**
     * current location, whether they are in taxi, the goal, whether they haven been picked up
     * whether they have just been picked up and haven't changed goal
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_LOCATION,
            ATT_GOAL_LOCATION
            );

    public LCRootCargo(String name, String currentLocation) {
        this(name, (Object) currentLocation, null);
    }

    public LCRootCargo(String name, String currentLocation, String goalLocation) {
        this(name, (Object) currentLocation, (Object) goalLocation);
    }

    private LCRootCargo(String name, Object currentLocation, Object goalLocation){
        this.set(ATT_LOCATION, currentLocation);
        this.set(ATT_GOAL_LOCATION, goalLocation);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_CARGO;
    }

    @Override
    public LCRootCargo copy() {
        return (LCRootCargo) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCRootCargo(
                objectName,
                get(ATT_LOCATION),
                get(ATT_GOAL_LOCATION)
                );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }}
