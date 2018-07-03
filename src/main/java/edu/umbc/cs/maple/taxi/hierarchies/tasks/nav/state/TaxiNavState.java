package edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;
public class TaxiNavState implements MutableOOState, DeepCopyForShallowCopyState {
    private TaxiNavAgent taxi;
    private Map<String, TaxiNavLocation> locations;
    private Map<String, TaxiNavWall> walls;

    public TaxiNavState(TaxiNavAgent taxi, List<TaxiNavLocation> locations, List<TaxiNavWall> walls) {
        this.taxi = taxi;

        this.locations = new HashMap<String, TaxiNavLocation>();
        for(TaxiNavLocation l : locations){
            this.locations.put(l.name(), l);
        }

        this.walls = new HashMap<String, TaxiNavWall>();
        for(TaxiNavWall w : walls){
            this.walls.put(w.name(), w);
        }
    }

    public TaxiNavState(TaxiNavAgent t, Map<String, TaxiNavLocation> locs, Map<String, TaxiNavWall> walls) {
        this.taxi = t;
        this.locations = locs;
        this.walls = walls;
    }

    public TaxiNavAgent touchTaxi(){
        if (this.taxi != null) {
            this.taxi = taxi.copy();
        }
        return taxi;
    }

    public TaxiNavLocation touchLocation(String name){
        TaxiNavLocation loc = locations.get(name).copy();
        touchLocations().remove(name);
        locations.put(name, loc);
        return loc;
    }

    public Map<String, TaxiNavLocation> touchLocations(){
        this.locations = new HashMap<String, TaxiNavLocation>(locations);
        return locations;
    }

    public TaxiNavWall touchWall(String name){
        TaxiNavWall wall = walls.get(name).copy();
        touchWalls().remove(name);
        walls.put(name, wall);
        return wall;
    }

    public Map<String, TaxiNavWall> touchWalls(){
        this.walls = new HashMap<>(walls);
        return walls;
    }



    @Override
    public int numObjects() {
        int total = taxi == null ? 0 : 1;
        total += walls.size();
        total += locations.size();
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(taxi != null && taxi.name().equals(oname))
            return taxi;

        ObjectInstance o = locations.get(oname);
        if(o != null)
            return o;

        o = walls.get(oname);
        if(o != null)
            return o;

        return null;
    }

//    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
//        if (cachedObjectList == null) { cachedObjectList = new ArrayList<ObjectInstance>(); }
//        else { return cachedObjectList; }
        List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
        if (taxi != null) { objs.add(taxi); }
        objs.addAll(locations.values());
        objs.addAll(walls.values());
//        cachedObjectList = objs;
        return objs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_TAXI))
            return taxi == null ? new ArrayList<>() : Arrays.<ObjectInstance>asList(taxi);
        else if(oclass.equals(CLASS_LOCATION))
            return new ArrayList<>(locations.values());
        else if(oclass.equals(CLASS_WALL))
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
    public TaxiNavState copy() {
        return new TaxiNavState(touchTaxi(), touchLocations(), touchWalls());
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        throw new RuntimeException("Set not implemented");
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        throw new RuntimeException("Add not implemented");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiNavAgent) {
            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiNavWall) {
            touchWall(oname);
            walls.remove(oname);
        } else if (objectInstance instanceof TaxiNavLocation) {
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

    @Override
    public String toString() {
        String out = "{ " + this.getClass().getSimpleName() + "\n";
        out += taxi.toString();
        for(TaxiNavLocation loc : locations.values()){
            out += loc.toString() + "\n";
        }
        for(TaxiNavWall wall : walls.values()){
            out += wall.toString() + "\n";
        }
        out += "\n}";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaxiNavState that = (TaxiNavState) o;

        if (taxi != null ? !taxi.equals(that.taxi) : that.taxi != null) return false;
        if (locations != null ? !locations.equals(that.locations) : that.locations != null) return false;
        return walls != null ? walls.equals(that.walls) : that.walls == null;
    }

    @Override
    public int hashCode() {
        int result = taxi != null ? taxi.hashCode() : 0;
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (walls != null ? walls.hashCode() : 0);
        return result;
    }

    @Override
    public MutableOOState deepCopy() {
        TaxiNavState copy = this.copy();
        copy.touchTaxi();
        copy.touchLocations();
        copy.touchWalls();
        return copy;
    }
}
