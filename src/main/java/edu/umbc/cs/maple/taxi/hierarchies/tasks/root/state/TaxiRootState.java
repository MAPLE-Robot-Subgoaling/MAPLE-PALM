package edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
public class TaxiRootState implements MutableOOState, DeepCopyForShallowCopyState {

    private TaxiRootAgent taxi;
    private Map<String, TaxiRootPassenger> passengers;

    public TaxiRootState(TaxiRootAgent taxi, List<TaxiRootPassenger> pass) {
        this.taxi = taxi;
        this.passengers = new HashMap<>();
        for(TaxiRootPassenger p : pass){
            this.passengers.put(p.name(), p);
        }
    }

    private TaxiRootState(TaxiRootAgent taxi, Map<String, TaxiRootPassenger> pass) {
        this.taxi = taxi;
        this.passengers = pass;
    }

    @Override
    public int numObjects() {
        int total = 0;
        if (taxi != null) { total += 1; }
        total += passengers.size();
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(taxi != null && oname.equals(taxi.name())) {
            return taxi;
        }

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
        if (taxi != null) { obj.add(taxi); }
        obj.addAll(passengers.values());
//        cachedObjectList = obj;
        return obj;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_TAXI)) {
            return taxi == null ? new ArrayList<>() : Arrays.<ObjectInstance>asList(taxi);
        }
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
        return new TaxiRootState(touchTaxi(), touchPassengers());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(taxi != null && key.obName.equals(taxi.name())) {
            touchTaxi().set(variableKey, value);
        } else if(passengers.get(key.obName) != null){
            touchPassenger(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof TaxiRootAgent || o.className().equals(CLASS_TAXI)) {
            taxi = (TaxiRootAgent)o;
        } if(o instanceof TaxiRootPassenger || o.className().equals(CLASS_PASSENGER)){
            touchPassengers().put(o.name(), (TaxiRootPassenger) o);
        }else{
            throw new RuntimeException("Can only add certain objects to state.");
        }
//        cachedObjectList = null;
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiRootAgent) {
            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiRootPassenger) {
            touchPassenger(oname);
            passengers.remove(oname);
        } else {
            throw new RuntimeException("Error: unknown object of name: " + oname);
        }
//        cachedObjectList = null;
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    //touch methods allow a shallow copy of states and a copy of objects only when modified
    public TaxiRootAgent touchTaxi() {
        if (taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

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
        if (taxi != null) {
            buf.append("Tx:{");
            String at = (String) taxi.get(ATT_LOCATION);
            if (at.contains("Location")) {
                buf.append("L");
                buf.append(at.charAt(at.length()-1));
            } else {
                buf.append(at);
            }
            buf.append("} ");
        }
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

        if (taxi != null ? !taxi.equals(that.taxi) : that.taxi != null) return false;
        return passengers != null ? passengers.equals(that.passengers) : that.passengers == null;
    }

    @Override
    public int hashCode() {
        int result = taxi != null ? taxi.hashCode() : 0;
        result = 31 * result + (passengers != null ? passengers.hashCode() : 0);
        return result;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiRootState copy = this.copy();
        copy.touchTaxi();
        copy.touchPassengers();
        return copy;
    }
}
