package edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
public class TaxiRootState implements MutableOOState, DeepCopyForShallowCopyState {

    //this state has passengers
    private Map<String, TaxiRootPassenger> passengers;

    public TaxiRootState(List<TaxiRootPassenger> pass) {
        this.passengers = new HashMap<>();
        for(TaxiRootPassenger p : pass){
            this.passengers.put(p.name(), p);
        }
    }

    private TaxiRootState(Map<String, TaxiRootPassenger> pass) {
        this.passengers = pass;
    }

    @Override
    public int numObjects() {
        return passengers.size();
    }

    @Override
    public ObjectInstance object(String oname) {
        ObjectInstance o = passengers.get(oname);
        if(o != null) {
            return o;
        }

        return null;
    }

//    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
//        if (cachedObjectList == null) { cachedObjectList = new ArrayList<ObjectInstance>(); }
//        else { return cachedObjectList; }
        List<ObjectInstance> obj = new ArrayList<>();
        obj.addAll(passengers.values());
//        cachedObjectList = obj;
        return obj;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_PASSENGER)) {
            return new ArrayList<>(passengers.values());
        }
        throw new RuntimeException("No object class " + oclass);
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
    public TaxiRootState copy() {
        return new TaxiRootState(touchPassengers());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(passengers.get(key.obName) != null){
            touchPassenger(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof TaxiRootPassenger || o.className().equals(CLASS_PASSENGER)){
            touchPassengers().put(o.name(), (TaxiRootPassenger) o);
        }else{
            throw new RuntimeException("Can only add certain objects to state.");
        }
//        cachedObjectList = null;
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        touchPassenger(oname);
        passengers.remove(oname);
//        cachedObjectList = null;

        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    //touch methods allow a shallow copy of states and a copy of objects only when modified
    public TaxiRootPassenger touchPassenger(String passName){
        TaxiRootPassenger p = passengers.get(passName).copy();
        touchPassengers().remove(passName);
        passengers.put(passName, p);
        return p;
    }

    public Map<String, TaxiRootPassenger> touchPassengers(){
        this.passengers = new HashMap<>(passengers);
        return passengers;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("");
        for (TaxiRootPassenger passenger : passengers.values()) {
            buf.append("P");
            buf.append(passenger.name().charAt(passenger.name().length()-1));
            buf.append(":{");
            String at = (String) passenger.get(ATT_LOCATION);
            if (at.contains("Location")) {
                buf.append("L");
                buf.append(at.charAt(at.length()-1));
            } else {
                buf.append(at);
            }
            buf.append("->");
            String goal = (String) passenger.get(ATT_GOAL_LOCATION);
            if (goal.contains("Location")) {
                buf.append("L");
                buf.append(goal.charAt(goal.length()-1));
            } else {
                buf.append(goal);
            }
            buf.append("} ");
        }
        buf.append(";\n");
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaxiRootState that = (TaxiRootState) o;

        return passengers != null ? passengers.equals(that.passengers) : that.passengers == null;
    }

    @Override
    public int hashCode() {
        return passengers != null ? passengers.hashCode() : 0;
    }


    @Override
    public MutableOOState deepCopy() {
        TaxiRootState copy = this.copy();
        copy.touchPassengers();
        return copy;
    }
}
