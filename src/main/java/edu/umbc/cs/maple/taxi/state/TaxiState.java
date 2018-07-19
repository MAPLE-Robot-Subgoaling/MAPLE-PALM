package edu.umbc.cs.maple.taxi.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiState implements MutableOOState, DeepCopyForShallowCopyState {

    //contains a taxi, passengers, locations and walls
    private TaxiAgent taxi;
    private Map<String, TaxiPassenger> passengers;
    private Map<String, TaxiLocation> locations;
    private Map<String, TaxiWall> walls;

    public TaxiState() {
        // for de/serialization
    }

    public TaxiState(TaxiAgent taxi, List<TaxiPassenger> passengers, List<TaxiLocation> locations,
            List<TaxiWall> walls) {
        this.taxi = taxi;

        this.passengers = new HashMap<>();
        for(TaxiPassenger p : passengers){
            this.passengers.put(p.name(), p);
        }

        this.locations = new HashMap<>();
        for(TaxiLocation l : locations){
            this.locations.put(l.name(), l);
        }

        this.walls = new HashMap<>();
        for(TaxiWall w : walls){
            this.walls.put(w.name(), w);
        }
    }

    public TaxiState(TaxiAgent t, Map<String, TaxiPassenger> pass, Map<String, TaxiLocation> locs, Map<String, TaxiWall> walls) {
        this.taxi = t;
        this.passengers = pass;
        this.locations = locs;
        this.walls = walls;
    }

    @Override
    public int numObjects() {
        int count = 0;
        if (taxi != null) { count += 1; }
        count += passengers.size();
        count += locations.size();
        count += walls.size();
        return count;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(taxi != null && taxi.name().equals(oname)) { return taxi; }

        ObjectInstance o = passengers.get(oname);
        if(o != null) { return o; }

        o = locations.get(oname);
        if(o != null) { return o; }

        o = walls.get(oname);
        if(o != null) { return o; }

        throw new RuntimeException("Error: no object found with name: " + oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
        if (taxi != null) { objs.add(taxi); }
        if (passengers != null) { objs.addAll(passengers.values()); }
        if (locations != null) { objs.addAll(locations.values()); }
        if (walls != null) { objs.addAll(walls.values()); }
        return objs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_TAXI)) {
            return Arrays.asList(taxi);
        } else if(oclass.equals(CLASS_PASSENGER)) {
            return new ArrayList<>(passengers.values());
        } else if(oclass.equals(CLASS_LOCATION)) {
            return new ArrayList<>(locations.values());
        } else if(oclass.equals(CLASS_WALL)) {
            return new ArrayList<>(walls.values());
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
    public TaxiState copy() {
        return new TaxiState(taxi, passengers, locations, walls);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        throw new RuntimeException("not implemented");
//        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
//
//        if(key.obName.equals(taxi.name())){
//            touchTaxi().set(variableKey, value);
//        }else if(passengers.get(key.obName) != null){
//            touchPassenger(key.obName).set(variableKey, value);
//        }else if(locations.get(key.obName) != null){
//            touchLocation(key.obName).set(variableKey, value);
//        }else if(walls.get(key.obName) != null){
//            touchWall(key.obName).set(variableKey, value);
//        } else {
//            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
//        }
//        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if(o instanceof TaxiAgent || o.className().equals(CLASS_TAXI)){
            touchTaxi();
            taxi = (TaxiAgent) o;
        }else if(o instanceof TaxiPassenger || o.className().equals(CLASS_PASSENGER)){
            touchPassengers().put(o.name(), (TaxiPassenger) o);
        }else if(o instanceof TaxiLocation || o.className().equals(CLASS_LOCATION)){
            touchLocations().put(o.name(), (TaxiLocation) o);
        }else if(o instanceof TaxiWall || o.className().equals(CLASS_WALL)){
            touchWalls().put(o.name(), (TaxiWall) o);
        }else{
            throw new RuntimeException("Can only add certain objects to state.");
        }
        return this;
    }

    @Override
    public MutableOOState removeObject(String oname) {
        throw new RuntimeException("Remove not implemented");
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    //touch methods allow a shallow copy of states and a copy of objects only when modified
    public TaxiAgent touchTaxi(){
        this.taxi = taxi.copy();
        return taxi;
    }

    public TaxiPassenger touchPassenger(String passName){
        TaxiPassenger p = passengers.get(passName).copy();
        touchPassengers().remove(passName);
        passengers.put(passName, p);
        return p;
    }

    public TaxiLocation touchLocation(String locName){
        TaxiLocation l = locations.get(locName).copy();
        touchLocations().remove(locName);
        locations.put(locName, l);
        return l;
    }

    public TaxiWall touchWall(String wallName){
        TaxiWall w = walls.get(wallName).copy();
        touchWalls().remove(wallName);
        walls.put(wallName, w);
        return w;
    }

    public Map<String, TaxiPassenger> touchPassengers(){
        this.passengers = new HashMap<>(passengers);
        return passengers;
    }

    public Map<String, TaxiLocation> touchLocations(){
        this.locations = new HashMap<>(locations);
        return locations;
    }

    public Map<String, TaxiWall> touchWalls(){
        this.walls = new HashMap<>(walls);
        return walls;
    }

    public String getTaxiName(){
        return taxi.name();
    }

    //test to see if there is a wall on either side of the taxi
    public boolean wallNorth(){
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        for(TaxiWall w : walls.values()){
            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
            int wx = (int) w.get(ATT_START_X);
            int wy = (int) w.get(ATT_START_Y);
            int wlen = (int) w.get(ATT_LENGTH);
            if(ish){
                //wall in above line
                if(ty == wy - 1){
                    //x value in wall bounds
                    if(tx >= wx && tx < wx + wlen){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean wallEast(){
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        for(TaxiWall w : walls.values()){
            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
            int wx = (int) w.get(ATT_START_X);
            int wy = (int) w.get(ATT_START_Y);
            int wlen = (int) w.get(ATT_LENGTH);
            if(!ish){
                if(tx == wx - 1){
                    if(ty >= wy && ty < wy + wlen){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean wallSouth(){
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        for(TaxiWall w : walls.values()){
            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
            int wx = (int) w.get(ATT_START_X);
            int wy = (int) w.get(ATT_START_Y);
            int wlen = (int) w.get(ATT_LENGTH);
            if(ish){
                if(ty == wy){
                    if(tx >= wx && tx < wx + wlen){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean wallWest(){
        int tx = (int) taxi.get(ATT_X);
        int ty = (int) taxi.get(ATT_Y);
        for(TaxiWall w : walls.values()){
            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
            int wx = (int) w.get(ATT_START_X);
            int wy = (int) w.get(ATT_START_Y);
            int wlen = (int) w.get(ATT_LENGTH);
            if(!ish){
                if(tx == wx){
                    if(ty >= wy && ty < wy + wlen){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return OOStateUtilities.ooStateToString(this);
    }

    // determine if at least one passenger is in the taxi
    public boolean isTaxiOccupied() {
        for (String passengerName : this.passengers.keySet()) {
            boolean inTaxi = (boolean) passengers.get(passengerName).get(ATT_IN_TAXI);
            if (inTaxi) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiState copy = this.copy();
        copy.touchTaxi();
        copy.touchPassengers();
        copy.touchLocations();
        copy.touchWalls();
        return copy;
    }

    public TaxiAgent getTaxi() {
        return taxi;
    }

    public void setTaxi(TaxiAgent taxi) {
        this.taxi = taxi;
    }

    public Map<String, TaxiPassenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(Map<String, TaxiPassenger> passengers) {
        this.passengers = passengers;
    }

    public Map<String, TaxiLocation> getLocations() {
        return locations;
    }

    public void setLocations(Map<String, TaxiLocation> locations) {
        this.locations = locations;
    }

    public Map<String, TaxiWall> getWalls() {
        return walls;
    }

    public void setWalls(Map<String, TaxiWall> walls) {
        this.walls = walls;
    }
}

