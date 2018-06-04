package taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
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
        if(o != null)
            return o;

        o = locations.get(oname);
        if(o != null)
            return o;

        return null;
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
            return taxi == null ? new ArrayList<ObjectInstance>() : Arrays.<ObjectInstance>asList(taxi);
        if(oclass.equals(CLASS_PASSENGER))
            return new ArrayList<ObjectInstance>(passengers.values());
        if(oclass.equals(CLASS_LOCATION))
            return new ArrayList<ObjectInstance>(locations.values());
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
    public TaxiGetState copy() {
        return new TaxiGetState(touchTaxi(), touchPassengers(), touchLocations());
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
        this.passengers = new HashMap<String, TaxiGetPassenger>(passengers);
        return passengers;
    }

    //get values from objects
    public String[] getPassengers(){
        String[] ret = new String[passengers.size()];
        int i = 0;
        for(String name : passengers.keySet())
            ret[i++] = name;
        return ret;
    }

    public TaxiGetLocation touchLocation(String name){
        TaxiGetLocation loc = locations.get(name).copy();
        touchLocations().remove(name);
        locations.put(name, loc);
        return loc;
    }

    public Map<String, TaxiGetLocation> touchLocations(){
        this.locations = new HashMap<String, TaxiGetLocation>(locations);
        return locations;
    }

    //get values from objects
    public String[] getLocations(){
        String[] ret = new String[locations.size()];
        int i = 0;
        for(String name : locations.keySet())
            ret[i++] = name;
        return ret;
    }

    public Object getTaxiAtt(String attName) {
        if (taxi == null) { return null; }
        return taxi.get(attName);
    }

    public Object getPassengerAtt(String passName, String attName){
        return passengers.get(passName).get(attName);
    }

    public Object getLocationAtt(String locName, String attName) {
        return locations.get(locName).get(attName);
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
        int result = taxi != null ? taxi.hashCode() : 0;
        result = 31 * result + (passengers != null ? passengers.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        return result;
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
