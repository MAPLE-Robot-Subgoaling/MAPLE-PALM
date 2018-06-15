package liftCopter.hierarchies.expert.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.*;

public class LCPutCargo extends MutableObject {

    /**
     * current location, whether they are in taxi, the goal, whether they haven been picked up
     * whether they have just been picked up and haven't changed goal
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_GOAL_LOCATION,
            ATT_LOCATION
            );

    public LCPutCargo(String name, String goalLocation, String location){
        this(name, (Object) goalLocation, (Object) location);
    }

    private LCPutCargo(String name, Object goalLocation, Object location){
        this.set(ATT_GOAL_LOCATION, goalLocation);
        this.set(ATT_LOCATION, location);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_PASSENGER;
    }

    @Override
    public LCPutCargo copy() {
        return (LCPutCargo) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new LCPutCargo(
                objectName,
                get(ATT_GOAL_LOCATION),
                get(ATT_LOCATION)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }}
