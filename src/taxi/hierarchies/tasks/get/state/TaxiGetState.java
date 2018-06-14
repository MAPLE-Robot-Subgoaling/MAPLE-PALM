package taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import taxi.hierarchies.TaxiGetPutState;
import utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static taxi.TaxiConstants.*;

public class TaxiGetState extends TaxiGetPutState implements DeepCopyForShallowCopyState {

    //this state has passengers and depots
    private TaxiGetAgent taxi;
    private Map<String, TaxiGetPassenger> passengers;
    private Map<String, TaxiGetLocation> locations;

    public TaxiGetState(TaxiGetAgent taxi, List<TaxiGetPassenger> pass, List<TaxiGetLocation> locs) {
        this.taxi = taxi;

        this.passengers = new HashMap<>();
        for(TaxiGetPassenger p : pass){
            this.passengers.put(p.name(), p);
        }

        this.locations = new HashMap<>();
        for(TaxiGetLocation loc : locs){
            this.locations.put(loc.name(), loc);
        }
    }

    private TaxiGetState(TaxiGetAgent taxi, Map<String, TaxiGetPassenger> pass, Map<String, TaxiGetLocation> locs) {
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

//        throw new RuntimeException("Error: no object found with name " + oname);
        return null; // return null, needed for imagined state in rmax implementations
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> obj = new ArrayList<ObjectInstance>();
        if (taxi != null) { obj.add(taxi); }
        obj.addAll(passengers.values());
        obj.addAll(locations.values());
        return obj;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_TAXI))
            return taxi == null ? new ArrayList<>() : Arrays.<ObjectInstance>asList(taxi);
        if(oclass.equals(CLASS_PASSENGER))
            return new ArrayList<>(passengers.values());
        if(oclass.equals(CLASS_LOCATION))
            return new ArrayList<>(locations.values());
        throw new RuntimeException("Error: no class found with name " + oclass);
    }

    private List<Object> variableKeys;
    @Override
    public List<Object> variableKeys() {
        if (variableKeys == null) {
            variableKeys = OOStateUtilities.flatStateKeys(this);
        }
        return variableKeys;
    }

    @Override
    public TaxiGetState copy() {
        return new TaxiGetState(taxi, passengers, locations);
    }

    @Override
    public Object get(Object variableKey) {
        return OOStateUtilities.get(this, variableKey);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(key.obName.equals(taxi.name())) {
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
        if(o instanceof TaxiGetAgent || o.className().equals(CLASS_TAXI)) {
            taxi = (TaxiGetAgent)o;
        } else if(o instanceof TaxiGetPassenger || o.className().equals(CLASS_PASSENGER)){
            touchPassengers().put(o.name(), (TaxiGetPassenger) o);
        } else if(o instanceof TaxiGetLocation || o.className().equals(CLASS_LOCATION)){
            touchLocations().put(o.name(), (TaxiGetLocation) o);
        } else {
            throw new RuntimeException("Can only add certain objects to state.");
        }
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiGetAgent) {
            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiGetPassenger) {
            touchPassenger(oname);
            passengers.remove(oname);
        } else if (objectInstance instanceof TaxiGetLocation) {
            touchLocation(oname);
            locations.remove(oname);
        } else {
            throw new RuntimeException("Error: unknown object of name: " + oname);
        }
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    //touch methods allow a shallow copy of states and a copy of objects only when modified
    public TaxiGetAgent touchTaxi() {
        if (this.taxi != null) { this.taxi = taxi.copy(); }
        return taxi;
    }

    public TaxiGetPassenger touchPassenger(String name){
        TaxiGetPassenger p = passengers.get(name).copy();
        touchPassengers().remove(name);
        passengers.put(name, p);
        return p;
    }

    public Map<String, TaxiGetPassenger> touchPassengers(){
        this.passengers = new HashMap<>(passengers);
        return passengers;
    }

    public TaxiGetLocation touchLocation(String name){
        TaxiGetLocation loc = locations.get(name).copy();
        touchLocations().remove(name);
        locations.put(name, loc);
        return loc;
    }

    public Map<String, TaxiGetLocation> touchLocations(){
        this.locations = new HashMap(locations);
        return locations;
    }

    @Override
    public String toString(){
        String out = "{ " + this.getClass().getSimpleName() + "\n";

        if (taxi != null) {
            out += taxi.toString() + "\n";
        }

        for(TaxiGetPassenger p : passengers.values()){
            out += p.toString() + "\n";
        }

        for(TaxiGetLocation loc : locations.values()){
            out += loc.toString() + "\n";
        }
        out += "}";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaxiGetState that = (TaxiGetState) o;

        if (taxi != null ? !taxi.equals(that.taxi) : that.taxi != null) return false;
        if (passengers != null ? !passengers.equals(that.passengers) : that.passengers != null) return false;
        return locations != null ? locations.equals(that.locations) : that.locations == null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 59)
            .append(taxi)
            .append(passengers)
            .append(locations)
            .toHashCode();
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiGetState copy = this.copy();
        copy.touchTaxi();
        copy.touchPassengers();
        copy.touchLocations();
        return copy;
    }
}
