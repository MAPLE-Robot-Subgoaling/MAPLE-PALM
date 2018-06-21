package edu.umbc.cs.maple.taxi.hiergen.task5.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.taxi.hiergen.TaxiHierGenState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.CLASS_TAXI;

public class TaxiHierGenTask5State extends TaxiHierGenState implements MutableOOState, DeepCopyForShallowCopyState {

    private TaxiHierGenTask5Taxi taxi;

    public TaxiHierGenTask5State(TaxiHierGenTask5Taxi taxi){
        this.taxi = taxi;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Not needed for HierGen");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiHierGenTask5Taxi) {
            touchTaxi();
            taxi = null;
        }
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Not needed for HierGen");
    }

    @Override
    public int numObjects() {
        int total = taxi == null ? 0 : 1;
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(taxi.name().equals(oname))
            return taxi;
        throw new RuntimeException("No object with name " + oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
        if (taxi != null) objects.add(taxi);
        return objects;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_TAXI))
            return Arrays.<ObjectInstance>asList(taxi);
        throw new RuntimeException("No object class " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        return null;
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        return OOStateUtilities.get(this, variableKey);
    }

    @Override
    public TaxiHierGenTask5State copy() {
        return new TaxiHierGenTask5State(taxi);
    }

    public TaxiHierGenTask5Taxi touchTaxi() {
        if (this.taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiHierGenTask5State copy = this.copy();
        copy.touchTaxi();
        return copy;
    }

    public TaxiHierGenTask5Taxi getTaxi() {
        return taxi;
    }
}
