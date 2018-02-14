package taxi.hierarchies.tasks.nav.state;

import java.util.*;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;

public class TaxiNavState implements MutableOOState{
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
        this.walls = new HashMap<String, TaxiNavWall>(walls);
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

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
		if (taxi != null) { objs.add(taxi); }
		objs.addAll(locations.values());
		objs.addAll(walls.values());
		return objs;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(Taxi.CLASS_TAXI))
			return taxi == null ? new ArrayList<ObjectInstance>() : Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(Taxi.CLASS_LOCATION))
			return new ArrayList<ObjectInstance>(locations.values());
		else if(oclass.equals(Taxi.CLASS_WALL))
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
            taxi = null;
        } else if (objectInstance instanceof TaxiNavWall) {
            touchWall(oname);
            walls.remove(oname);
        } else if (objectInstance instanceof TaxiNavLocation) {
            touchLocation(oname);
            locations.remove(oname);
        }
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

	public Collection<TaxiNavWall> getWallObjects() {
	    return walls.values();
    }


	public Object getTaxiAtt(String attName){
		if(taxi == null) {
		    return null;
		}
		return taxi.get(attName);
	}

	public Object getLocationAtt(String locName, String attName){
		return locations.get(locName).get(attName);
	}

	public Object getWallAtt(String wallName, String attName){
		return walls.get(wallName).get(attName);
	}

	@Override
	public String toString() {
		return "TaxiNavState{" +
				taxi +
				", " + locations +
				", " + walls +
				'}';
	}
}
