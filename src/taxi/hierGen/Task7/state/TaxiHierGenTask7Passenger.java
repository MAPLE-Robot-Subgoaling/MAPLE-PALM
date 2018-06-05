package taxi.hierGen.Task7.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.*;

public class TaxiHierGenTask7Passenger extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y,
            ATT_IN_TAXI
    );

    public TaxiHierGenTask7Passenger(String name, int x, int y,  boolean inTaxi){
        this(name, (Object) x, (Object) y, (Object) inTaxi);
    }

    private TaxiHierGenTask7Passenger(String name, Object x, Object y, Object inTaxi){
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_IN_TAXI, inTaxi);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_PASSENGER;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiHierGenTask7Passenger(
                objectName,
                get(ATT_X),
                get(ATT_Y),
                get(ATT_IN_TAXI)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public TaxiHierGenTask7Passenger copy() {
        return (TaxiHierGenTask7Passenger) copyWithName(name());
    }
}
