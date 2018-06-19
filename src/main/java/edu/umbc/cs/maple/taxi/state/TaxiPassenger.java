package edu.umbc.cs.maple.taxi.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiPassenger extends MutableObject{

    /**
     * x, y, whether in taxi, goal, whether they have been picked up, whether they wer just picked up
     */
    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_IN_TAXI,
            ATT_GOAL_LOCATION
            );

    public TaxiPassenger(String name, int x, int y, String goalLocation){
        this(name, (Object) x, (Object) y, (Object) goalLocation, false);
    }

    public TaxiPassenger(String name, int x, int y, String goalLocation, boolean inTaxi){
        this(name, (Object) x, (Object) y, (Object) goalLocation, (Object) inTaxi);
    }

    public TaxiPassenger(String name, int x, int y, String goalLocation, boolean inTaxi,
            boolean pickedUpAlLeastOnce, boolean justPickedUp){
        this(name, (Object) x, (Object) y, (Object) goalLocation, (Object) inTaxi);
    }

    private TaxiPassenger(String name, Object x, Object y, Object goalLocation, Object inTaxi){
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_GOAL_LOCATION, goalLocation);
        this.set(ATT_IN_TAXI, inTaxi);
//		this.set(ATT_JUST_PICKED_UP, justpickedUp);
        this.setName(name);
    }


    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiPassenger(
                objectName,
                get(ATT_X),
                get(ATT_Y),
                get(ATT_GOAL_LOCATION),
                get(ATT_IN_TAXI)
                );
    }

    @Override
    public TaxiPassenger copy() {
        return (TaxiPassenger) copyWithName(name());
    }

    @Override
    public String className() {
        return CLASS_PASSENGER;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

}
