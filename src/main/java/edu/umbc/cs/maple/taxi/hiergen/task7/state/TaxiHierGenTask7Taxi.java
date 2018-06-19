package edu.umbc.cs.maple.taxi.hiergen.task7.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_X;
import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_Y;
import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_TAXI;

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
        return CLASS_TAXI;
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
