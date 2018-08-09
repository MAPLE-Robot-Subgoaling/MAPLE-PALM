package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.nav.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.*;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class LCNavState implements MutableOOState, DeepCopyForShallowCopyState {
    private LCNavAgent agent;
    private Map<String, LCNavLocation> locations;
    private Map<String, LCNavWall> walls;

    public LCNavState(LCNavAgent agent, List<LCNavLocation> locations, List<LCNavWall> walls) {
        this.agent = agent;

        this.locations = new HashMap<String, LCNavLocation>();
        for(LCNavLocation l : locations){
            this.locations.put(l.name(), l);
        }

        this.walls = new HashMap<String, LCNavWall>();
        for(LCNavWall w : walls){
            this.walls.put(w.name(), w);
        }
    }

    public LCNavState(LCNavAgent a, Map<String, LCNavLocation> locs, Map<String, LCNavWall> walls) {
        this.agent= a;
        this.locations = locs;
        this.walls = walls;
    }

    public LCNavAgent touchCopter(){
        if (this.agent != null) {
            this.agent = agent.copy();
        }
        return agent;
    }

    public LCNavLocation touchLocation(String name){
        LCNavLocation loc = locations.get(name).copy();
        touchLocations().remove(name);
        locations.put(name, loc);
        return loc;
    }

    public Map<String, LCNavLocation> touchLocations(){
        this.locations = new HashMap<String, LCNavLocation>(locations);
        return locations;
    }

    public LCNavWall touchWall(String name){
        LCNavWall wall = walls.get(name).copy();
        touchWalls().remove(name);
        walls.put(name, wall);
        return wall;
    }

    public Map<String, LCNavWall> touchWalls(){
        this.walls = new HashMap<String, LCNavWall>(walls);
        return walls;
    }



    @Override
    public int numObjects() {
        int total = (agent == null) ? 0 : 1;
        total += walls.size();
        total += locations.size();
        return total;
    }

    @Override
    public ObjectInstance object(String oname) {
        if(agent != null && agent.name().equals(oname))
            return agent;

        ObjectInstance o = locations.get(oname);
        if(o != null)
            return o;

        o = walls.get(oname);
        if(o != null)
            return o;

        return null;
    }

    private List<ObjectInstance> cachedObjectList = null;
    @Override
    public List<ObjectInstance> objects() {
        if (cachedObjectList == null) { cachedObjectList = new ArrayList<ObjectInstance>(); }
        else { return cachedObjectList; }
        List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
        if (agent != null) { objs.add(agent); }
        objs.addAll(locations.values());
        objs.addAll(walls.values());
        cachedObjectList = objs;
        return objs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_AGENT))
            return agent == null ? new ArrayList<ObjectInstance>() : Arrays.<ObjectInstance>asList(agent);
        else if(oclass.equals(CLASS_LOCATION))
            return new ArrayList<ObjectInstance>(locations.values());
        else if(oclass.equals(CLASS_WALL))
            return new ArrayList<ObjectInstance>(walls.values());
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
    public LCNavState copy() {
        return new LCNavState(touchCopter(), touchLocations(), touchWalls());
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
        if (objectInstance instanceof LCNavAgent) {
            touchCopter();
            agent = null;
        } else if (objectInstance instanceof LCNavWall) {
            touchWall(oname);
            walls.remove(oname);
        } else if (objectInstance instanceof LCNavLocation) {
            touchLocation(oname);
            locations.remove(oname);
        } else {
            throw new RuntimeException("Error: unknown object of name: " + oname);
        }
        cachedObjectList = null;
        return this;
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    public String[] getLocations(){
        String[] ret = new String[locations.size()];
        int i = 0;
        for(String name : locations.keySet())
            ret[i++] = name;
        return ret;
    }

    public String[] getWalls(){
        String[] ret = new String[walls.size()];
        int i = 0;
        for(String name: walls.keySet())
            ret[i++] = name;
        return ret;
    }

    public Collection<LCNavWall> getWallObjects() {
        return walls.values();
    }


    public Object getAgentAtt(String attName){
        if(agent == null) {
            return null;
        }
        return agent.get(attName);
    }

    public Object getLocationAtt(String locName, String attName){
        return locations.get(locName).get(attName);
    }

    public Object getWallAtt(String wallName, String attName){
        return walls.get(wallName).get(attName);
    }

    @Override
    public String toString() {
        String out = "{ " + this.getClass().getSimpleName() + "\n";
        out += agent.toString();
        for(LCNavLocation loc : locations.values()){
            out += loc.toString() + "\n";
        }
        for(LCNavWall wall : walls.values()){
            out += wall.toString() + "\n";
        }
        out += "\n}";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LCNavState that = (LCNavState) o;

        if (agent != null ? !agent.equals(that.agent) : that.agent != null) return false;
        if (locations != null ? !locations.equals(that.locations) : that.locations != null) return false;
        return walls != null ? walls.equals(that.walls) : that.walls == null;
    }

    @Override
    public int hashCode() {
        int result = agent != null ? agent.hashCode() : 0;
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (walls != null ? walls.hashCode() : 0);
        return result;
    }

    @Override
    public MutableOOState deepCopy() {
        LCNavState copy = this.copy();
        copy.touchCopter();
        copy.touchLocations();
        copy.touchWalls();
        return copy;
    }
}
