package taxi.hiergen.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.*;

public class TaxiHierGenRootPassenger extends MutableObject {

    private final static List<Object> keys = Arrays.asList(
            ATT_X,
            ATT_Y,
            ATT_DESTINATION_X,
            ATT_DESTINATION_Y,
            ATT_IN_TAXI
    );

    public TaxiHierGenRootPassenger(String name, int x, int y, int destX, int destY, boolean inTaxi){
        this(name, (Object) x, (Object) y, (Object) destX, (Object) destY, (Object) inTaxi);
    }

    private TaxiHierGenRootPassenger(String name, Object x, Object y, Object destX, Object destY, Object inTaxi){
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.set(ATT_DESTINATION_X, destX);
        this.set(ATT_DESTINATION_Y, destY);
        this.set(ATT_IN_TAXI, inTaxi);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_PASSENGER;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiHierGenRootPassenger(
                objectName,
                get(ATT_X),
                get(ATT_Y),
                get(ATT_DESTINATION_X),
                get(ATT_DESTINATION_Y),
                get(ATT_IN_TAXI)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public TaxiHierGenRootPassenger copy() {
        return (TaxiHierGenRootPassenger) copyWithName(name());
    }
}
