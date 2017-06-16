package taxi.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import taxi.Taxi;

public class TaxiState implements MutableOOState{

	private TaxiAgent taxi;
	private Map<String, TaxiPassenger> passengers;
	private Map<String, TaxiLocation> locations;
	private Map<String, TaxiWall> walls;
	
	public TaxiState(TaxiAgent taxi, List<TaxiPassenger> passengers, List<TaxiLocation> locations,
			List<TaxiWall> walls) {
		this.taxi = taxi;
		
		this.passengers = new HashMap<String, TaxiPassenger>();
		for(TaxiPassenger p : passengers){
			this.passengers.put(p.name(), p);
		}
		
		this.locations = new HashMap<String, TaxiLocation>();
		for(TaxiLocation l : locations){
			this.locations.put(l.name(), l);
		}
		
		this.walls = new HashMap<String, TaxiWall>();
		for(TaxiWall w : walls){
			this.walls.put(w.name(), w);
		}
	}
	
	public TaxiState(TaxiAgent t, Map<String, TaxiPassenger> pass, Map<String, TaxiLocation> locs,
			Map<String, TaxiWall> walls) {
		this.taxi = t;
		this.passengers = pass;
		this.locations = locs;
		this.walls = walls;
	}
	
	@Override
	public int numObjects() {
		return 1 + passengers.size() + locations.size() + walls.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		if(taxi.name().equals(oname))
			return taxi;
		
		ObjectInstance o = passengers.get(oname);
		if(o != null)
			return o;
		
		o = locations.get(oname);
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
		objs.addAll(passengers.values());
		objs.addAll(locations.values());
		objs.addAll(walls.values());
		return objs;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(Taxi.CLASS_TAXI))
			return Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(Taxi.CLASS_PASSENGER))
			return new ArrayList<ObjectInstance>(passengers.values());
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
	public TaxiState copy() {
		return new TaxiState(taxi, passengers, locations, walls);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		
		if(key.obName.equals(taxi.name())){
			touchTaxi().set(variableKey, value);
		}else if(passengers.get(key.obName) != null){
			touchPassenger(key.obName).set(variableKey, value);
		}else if(locations.get(key.obName) != null){
			touchLocation(key.obName).set(variableKey, value);
		}else if(walls.get(key.obName) != null){
			touchWall(key.obName).set(variableKey, value);
		} else {
			throw new RuntimeException("ERROR: unable to set value for " + variableKey);
		}
		return this;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(o instanceof TaxiAgent || o.className().equals(Taxi.CLASS_TAXI)){
			touchTaxi();
			taxi = (TaxiAgent) o;
		}else if(o instanceof TaxiPassenger || o.className().equals(Taxi.CLASS_PASSENGER)){
			touchPassengers().put(o.name(), (TaxiPassenger) o);			
		}else if(o instanceof TaxiLocation || o.className().equals(Taxi.CLASS_LOCATION)){
			touchLocations().put(o.name(), (TaxiLocation) o);
		}else if(o instanceof TaxiWall || o.className().equals(Taxi.CLASS_WALL)){
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

	//copy on write
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
		this.passengers = new HashMap<String, TaxiPassenger>(passengers);
		return passengers;
	}
	
	public Map<String, TaxiLocation> touchLocations(){
		this.locations = new HashMap<String, TaxiLocation>(locations);
		return locations;
	}
	
	public Map<String, TaxiWall> touchWalls(){
		this.walls = new HashMap<String, TaxiWall>(walls);
		return walls;
	}
	
	//get values from objects
	public String[] getPassengers(){
		return (String[]) passengers.keySet().toArray();
	}
	
	public String[] getLocations(){
		return (String[]) locations.keySet().toArray();
	}
	
	public Object getTaxiAtt(String attName){
		return taxi.get(attName);
	}
	
	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}
	
	public Object getLocationAtt(String locName, String attName){
		return locations.get(locName).get(attName);
	}
	
	public boolean wallNorth(){
		int tx = (int) taxi.get(Taxi.ATT_X);
		int ty = (int) taxi.get(Taxi.ATT_Y);
		for(TaxiWall w : walls.values()){
			boolean ish = (boolean) w.get(Taxi.ATT_IS_HORIZONTAL);
			int wx = (int) w.get(Taxi.ATT_START_X);
			int wy = (int) w.get(Taxi.ATT_START_Y);
			int wlen = (int) w.get(Taxi.ATT_LENGTH);
			if(ish){
				//wall in above line
				if(ty == wy + 1){
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
		int tx = (int) taxi.get(Taxi.ATT_X);
		int ty = (int) taxi.get(Taxi.ATT_Y);
		for(TaxiWall w : walls.values()){
			boolean ish = (boolean) w.get(Taxi.ATT_IS_HORIZONTAL);
			int wx = (int) w.get(Taxi.ATT_START_X);
			int wy = (int) w.get(Taxi.ATT_START_Y);
			int wlen = (int) w.get(Taxi.ATT_LENGTH);
			if(!ish){
				if(tx == wx + 1){
					if(ty >= wy && ty < wy + wlen){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean wallSouth(){
		int tx = (int) taxi.get(Taxi.ATT_X);
		int ty = (int) taxi.get(Taxi.ATT_Y);
		for(TaxiWall w : walls.values()){
			boolean ish = (boolean) w.get(Taxi.ATT_IS_HORIZONTAL);
			int wx = (int) w.get(Taxi.ATT_START_X);
			int wy = (int) w.get(Taxi.ATT_START_Y);
			int wlen = (int) w.get(Taxi.ATT_LENGTH);
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
		int tx = (int) taxi.get(Taxi.ATT_X);
		int ty = (int) taxi.get(Taxi.ATT_Y);
		for(TaxiWall w : walls.values()){
			boolean ish = (boolean) w.get(Taxi.ATT_IS_HORIZONTAL);
			int wx = (int) w.get(Taxi.ATT_START_X);
			int wy = (int) w.get(Taxi.ATT_START_Y);
			int wlen = (int) w.get(Taxi.ATT_LENGTH);
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
}
