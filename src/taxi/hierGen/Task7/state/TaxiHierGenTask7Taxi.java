package taxi.hierGen.Task7.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.ATT_X;
import static taxi.TaxiConstants.ATT_Y;

public class TaxiHierGenTask7Taxi extends MutableObject{

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y);

    public TaxiHierGenTask7Taxi(String name, int x, int y){
        this(name, (Object) x, (Object) y);
    }

    private TaxiHierGenTask7Taxi(String name, Object x, Object y){
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.setName(name);
    }

    @Override
    public String className() {
        return TaxiHierGenTask7State.CLASS_TASK7_Taxi;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiHierGenTask7Taxi(
                objectName,
                get(ATT_X),
                get(ATT_Y)
        );
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public TaxiHierGenTask7Taxi copy() {
        return (TaxiHierGenTask7Taxi) copyWithName(name());
    }
}
