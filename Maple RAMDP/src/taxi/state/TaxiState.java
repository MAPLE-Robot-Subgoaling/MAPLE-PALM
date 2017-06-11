package taxi.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
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
	public State copy() {
		List<TaxiPassenger> pass = new ArrayList<TaxiPassenger>(passengers.values());
		List<TaxiLocation> locs = new ArrayList<TaxiLocation>(locations.values());
		List<TaxiWall> wall = new ArrayList<TaxiWall>(walls.values());
		return new TaxiState(taxi, pass, locs, wall);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MutableOOState removeObject(String oname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		// TODO Auto-generated method stub
		return null;
	}

}
