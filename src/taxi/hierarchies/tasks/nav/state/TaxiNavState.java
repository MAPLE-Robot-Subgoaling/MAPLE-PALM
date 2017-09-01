package taxi.hierarchies.tasks.nav.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		this.taxi = taxi.copy();
		return taxi;
	}

	@Override
	public int numObjects() {
		return 1 + locations.size() + walls.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		if(taxi.name().equals(oname))
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
		objs.add(taxi);
		objs.addAll(locations.values());
		objs.addAll(walls.values());
		return objs;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(Taxi.CLASS_TAXI))
			return Arrays.<ObjectInstance>asList(taxi);
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
		return new TaxiNavState(taxi, locations, walls);
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
		throw new RuntimeException("Remove not implemented");
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

	public Object getTaxiAtt(String attName){
		return taxi.get(attName);
	}

	public Object getLocationAtt(String locName, String attName){
		return locations.get(locName).get(attName);
	}

	public Object getWallAtt(String wallName, String attName){
		return walls.get(wallName).get(attName);
	}

}
