package edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.taxi.hierarchies.TaxiGetPutState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiPutState extends TaxiGetPutState implements DeepCopyForShallowCopyState {

    //this state has passengers and depots
    private TaxiPutAgent taxi;
    private Map<String, TaxiPutPassenger> passengers;
    private Map<String, TaxiPutLocation> locations;

    public TaxiPutState(TaxiPutAgent taxi, List<TaxiPutPassenger> pass, List<TaxiPutLocation> locs) {
        this.taxi = taxi;

        this.passengers = new HashMap<>();
        for(TaxiPutPassenger p : pass){
            this.passengers.put(p.name(), p);
        }

        this.locations = new HashMap<>();
        for(TaxiPutLocation loc : locs){
            this.locations.put(loc.name(), loc);
        }
    }

    private TaxiPutState(TaxiPutAgent taxi, Map<String, TaxiPutPassenger> pass, Map<String, TaxiPutLocation> locs) {
        this.taxi = taxi;
        this.passengers = pass;
        this.locations = locs;
    }

    @Override
    public int numObjects() {
        int total = taxi == null ? 0 : 1;
        total += passengers.size();
        total += locations.size();
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

        o = locations.get(oname);
        if(o != null) {
            return o;
        }

        return null;
    }

//    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
//        if (cachedObjectList == null) { cachedObjectList = new ArrayList<>(); }
//        else { return cachedObjectList; }
        List<ObjectInstance> obj = new ArrayList<ObjectInstance>();
        if (taxi != null) { obj.add(taxi); }
        obj.addAll(passengers.values());
        obj.addAll(locations.values());
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
        if(oclass.equals(CLASS_LOCATION)) {
            return new ArrayList<>(locations.values());
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
    public TaxiPutState copy() {
        return new TaxiPutState(touchTaxi(), touchPassengers(), touchLocations());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if(taxi != null && key.obName.equals(taxi.name())) {
            touchTaxi().set(variableKey, value);
        } else if(passengers.get(key.obName) != null){
            touchPassenger(key.obName).set(variableKey, value);
        } else if(locations.get(key.obName) != null){
            touchLocation(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof TaxiPutAgent || o.className().equals(CLASS_TAXI)) {
            taxi = (TaxiPutAgent)o;
        } else if(o instanceof TaxiPutPassenger || o.className().equals(CLASS_PASSENGER)){
            touchPassengers().put(o.name(), (TaxiPutPassenger) o);
        } else if(o instanceof TaxiPutLocation || o.className().equals(CLASS_LOCATION)){
            touchLocations().put(o.name(), (TaxiPutLocation) o);
        } else {
            throw new RuntimeException("Can only add certain objects to state.");
        }
//        cachedObjectList = null;
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiPutAgent) {
            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiPutPassenger) {
            touchPassenger(oname);
            passengers.remove(oname);
        } else if (objectInstance instanceof TaxiPutLocation) {
            touchLocation(oname);
            locations.remove(oname);
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
    public TaxiPutAgent touchTaxi() {
        if (taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

    public TaxiPutPassenger touchPassenger(String passName){
        TaxiPutPassenger p = passengers.get(passName).copy();
        touchPassengers().remove(passName);
        passengers.put(passName, p);
        return p;
    }

    public Map<String, TaxiPutPassenger> touchPassengers(){
        this.passengers = new HashMap<>(passengers);
        return passengers;
    }

    public TaxiPutLocation touchLocation(String locName){
        TaxiPutLocation loc = locations.get(locName).copy();
        touchLocations().remove(locName);
        locations.put(locName, loc);
        return loc;
    }

    public Map<String, TaxiPutLocation> touchLocations(){
        this.locations = new HashMap<>(locations);
        return locations;
    }

    @Override
    public String toString(){
        String out = "{ " + this.getClass().getSimpleName() + "\n";

        if (taxi != null) {
            out += taxi.toString() + "\n";
        }

        for(TaxiPutPassenger p : passengers.values()){
            out += p.toString() + "\n";
        }
        out += "}";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaxiPutState that = (TaxiPutState) o;

        if (taxi != null ? !taxi.equals(that.taxi) : that.taxi != null) return false;
        if (passengers != null ? !passengers.equals(that.passengers) : that.passengers != null) return false;
        return locations != null ? locations.equals(that.locations) : that.locations == null;
    }

    @Override
    public int hashCode() {
        int result = taxi != null ? taxi.hashCode() : 0;
        result = 17 * result + (passengers != null ? passengers.hashCode() : 0);
        result = 73 * result + (locations != null ? locations.hashCode() : 0);
        return result;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiPutState copy = this.copy();
        copy.touchTaxi();
        copy.touchPassengers();
        copy.touchLocations();
        return copy;
    }

    public TaxiPutAgent getTaxi() {
        return taxi;
    }
}
