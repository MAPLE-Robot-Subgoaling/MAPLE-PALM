package edu.umbc.cs.maple.liftCopter.state;


import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LiftCopterState implements MutableOOState, DeepCopyForShallowCopyState {

    //contains a agent, cargos, locations and walls
    private LiftCopterAgent copter;
    private Map<String, LiftCopterCargo> cargo;
    private Map<String, LiftCopterLocation> locations;
    private Map<String, LiftCopterWall> walls;

    public LiftCopterState(LiftCopterAgent copter, List<LiftCopterCargo> cargo, List<LiftCopterLocation> locations,
                           List<LiftCopterWall> walls) {
        this.copter = copter;

        this.cargo = new HashMap<String, LiftCopterCargo>();
        for (LiftCopterCargo c : cargo) {
            this.cargo.put(c.name(), c);
        }

        this.locations = new HashMap<>();
        for (LiftCopterLocation l : locations) {
            this.locations.put(l.name(), l);
        }

        this.walls = new HashMap<>();
        for (LiftCopterWall w : walls) {
            this.walls.put(w.name(), w);
        }
    }

    public LiftCopterState(LiftCopterAgent t, Map<String, LiftCopterCargo> pass, Map<String, LiftCopterLocation> locs, Map<String, LiftCopterWall> walls) {
        this.copter = t;
        this.cargo = pass;
        this.locations = locs;
        this.walls = walls;
    }

    @Override
    public int numObjects() {
        int count = 0;
        if (copter != null) {
            count += 1;
        }
        count += cargo.size();
        count += locations.size();
        count += walls.size();
        return count;
    }

    @Override
    public ObjectInstance object(String oname) {
        if (copter != null && copter.name().equals(oname)) {
            return copter;
        }

        ObjectInstance o = cargo.get(oname);
        if (o != null) {
            return o;
        }

        o = locations.get(oname);
        if (o != null) {
            return o;
        }

        o = walls.get(oname);
        if (o != null) {
            return o;
        }

        throw new RuntimeException("Error: no object found with name: " + oname);
    }

    @Override
    public List<ObjectInstance> objects() {
        List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
        if (copter != null) {
            objs.add(copter);
        }
        objs.addAll(cargo.values());
        objs.addAll(locations.values());
        objs.addAll(walls.values());
        return objs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if (oclass.equals(CLASS_AGENT))
            return Arrays.asList(copter);
        else if (oclass.equals(CLASS_CARGO))
            return new ArrayList<>(cargo.values());
        else if (oclass.equals(CLASS_LOCATION))
            return new ArrayList<>(locations.values());
        else if (oclass.equals(CLASS_WALL))
            return new ArrayList<>(walls.values());
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
    public LiftCopterState copy() {
        return new LiftCopterState(copter, cargo, locations, walls);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);

        if (key.obName.equals(copter.name())) {
            touchCopter().set(variableKey, value);
        } else if (cargo.get(key.obName) != null) {
            touchCargo(key.obName).set(variableKey, value);
        } else if (locations.get(key.obName) != null) {
            touchLocation(key.obName).set(variableKey, value);
        } else if (walls.get(key.obName) != null) {
            touchWall(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if (o instanceof LiftCopterAgent || o.className().equals(CLASS_AGENT)) {
            touchCargos();
            copter = (LiftCopterAgent) o;
        } else if (o instanceof LiftCopterCargo || o.className().equals(CLASS_CARGO)) {
            touchCargos().put(o.name(), (LiftCopterCargo) o);
        } else if (o instanceof LiftCopterLocation || o.className().equals(CLASS_LOCATION)) {
            touchLocations().put(o.name(), (LiftCopterLocation) o);
        } else if (o instanceof LiftCopterWall || o.className().equals(CLASS_WALL)) {
            touchWalls().put(o.name(), (LiftCopterWall) o);
        } else {
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
    public LiftCopterAgent touchCopter() {
        this.copter = copter.copy();
        return copter;
    }

    public LiftCopterCargo touchCargo(String cargoName) {
        LiftCopterCargo c = cargo.get(cargoName).copy();
        touchCargos().remove(cargoName);
        cargo.put(cargoName, c);
        return c;
    }

    public LiftCopterLocation touchLocation(String locName) {
        LiftCopterLocation l = locations.get(locName).copy();
        touchLocations().remove(locName);
        locations.put(locName, l);
        return l;
    }

    public LiftCopterWall touchWall(String wallName) {
        LiftCopterWall w = walls.get(wallName).copy();
        touchWalls().remove(wallName);
        walls.put(wallName, w);
        return w;
    }

    public Map<String, LiftCopterCargo> touchCargos() {
        this.cargo = new HashMap<>(cargo);
        return cargo;
    }

    public Map<String, LiftCopterLocation> touchLocations() {
        this.locations = new HashMap<>(locations);
        return locations;
    }

    public Map<String, LiftCopterWall> touchWalls() {
        this.walls = new HashMap<>(walls);
        return walls;
    }

    public String getCopterName() {
        return copter.name();
    }

//    //test to see if there is a wall on either side of the agent
//    public boolean wallNorth(){
//        int tx = (int) copter.get(ATT_X);
//        int ty = (int) copter.get(ATT_Y);
//        for(LiftCopterWall w : walls.values()){
//            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
//            int wx = (int) w.get(ATT_START_X);
//            int wy = (int) w.get(ATT_START_Y);
//            int wlen = (int) w.get(ATT_LENGTH);
//            if(ish){
//                //wall in above line
//                if(ty == wy - 1){
//                    //x value in wall bounds
//                    if(tx >= wx && tx < wx + wlen){
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
//
//    public boolean wallEast(){
//        int tx = (int) copter.get(ATT_X);
//        int ty = (int) copter.get(ATT_Y);
//        for(LiftCopterWall w : walls.values()){
//            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
//            int wx = (int) w.get(ATT_START_X);
//            int wy = (int) w.get(ATT_START_Y);
//            int wlen = (int) w.get(ATT_LENGTH);
//            if(!ish){
//                if(tx == wx - 1){
//                    if(ty >= wy && ty < wy + wlen){
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
//
//    public boolean wallSouth(){
//        int tx = (int) copter.get(ATT_X);
//        int ty = (int) copter.get(ATT_Y);
//        for(LiftCopterWall w : walls.values()){
//            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
//            int wx = (int) w.get(ATT_START_X);
//            int wy = (int) w.get(ATT_START_Y);
//            int wlen = (int) w.get(ATT_LENGTH);
//            if(ish){
//                if(ty == wy){
//                    if(tx >= wx && tx < wx + wlen){
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
//
//    public boolean wallWest(){
//        int tx = (int) copter.get(ATT_X);
//        int ty = (int) copter.get(ATT_Y);
//        for(LiftCopterWall w : walls.values()){
//            boolean ish = (boolean) w.get(ATT_IS_HORIZONTAL);
//            int wx = (int) w.get(ATT_START_X);
//            int wy = (int) w.get(ATT_START_Y);
//            int wlen = (int) w.get(ATT_LENGTH);
//            if(!ish){
//                if(tx == wx){
//                    if(ty >= wy && ty < wy + wlen){
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    // determine if at least one cargo is in the agent
    public boolean isLiftCopterOccupied() {
        for (String cargoName : this.cargo.keySet()) {
            boolean pickedUp = (boolean) cargo.get(cargoName).get(ATT_PICKED_UP);
            if (pickedUp) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MutableOOState deepCopy() {
        LiftCopterState copy = this.copy();
        copy.touchCopter();
        copy.touchCargos();
        copy.touchLocations();
        copy.touchWalls();
        return copy;
    }

    public LiftCopterAgent getCopter() {
        return copter;
    }
}

