package edu.umbc.cs.maple.taxi.hiergen.task5.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiHierGenTask5Taxi extends MutableObject{

    private final static List<Object> keys = Arrays.<Object>asList(
            ATT_X,
            ATT_Y);

    public TaxiHierGenTask5Taxi(String name, int x, int y){
        this(name, (Object) x, (Object) y);
    }

    private TaxiHierGenTask5Taxi(String name, Object x, Object y){
        this.set(ATT_X, x);
        this.set(ATT_Y, y);
        this.setName(name);
    }

    @Override
    public String className() {
        return CLASS_TAXI;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new TaxiHierGenTask5Taxi(
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
    public TaxiHierGenTask5Taxi copy() {
        return (TaxiHierGenTask5Taxi) copyWithName(name());
    }
}
