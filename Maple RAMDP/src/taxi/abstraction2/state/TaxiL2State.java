package taxi.abstraction2.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.abstraction2.TaxiL2;

public class TaxiL2State implements MutableOOState {

	private Map<String, TaxiL2Passenger> passengers;
	private Map<String, TaxiL2Location> locations;
	
	public TaxiL2State(List<TaxiL2Passenger> pass, List<TaxiL2Location> loc) {
		this.passengers = new HashMap<String, TaxiL2Passenger>();
		for(TaxiL2Passenger p : pass){
			this.passengers.put(p.name(), p);
		}
		
		this.locations = new HashMap<String, TaxiL2Location>();
		for(TaxiL2Location l : loc){
			this.locations.put(l.name(), l);
		}
	}
	
	private TaxiL2State(Map<String, TaxiL2Passenger> pass, Map<String, TaxiL2Location> loc) {
		this.passengers = pass;
		this.locations = loc;
	}
	
	@Override
	public int numObjects() {
		return passengers.size() + locations.size();
	}

	@Override
	public ObjectInstance object(String oname) {
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
		obj.addAll(passengers.values());
		obj.addAll(locations.values());
		return obj;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(TaxiL2.CLASS_L2PASSENGER))
			return new ArrayList<ObjectInstance>(passengers.values());
		else if(oclass.equals(TaxiL2.CLASS_L2LOCATION))
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
	public TaxiL2State copy() {
		return new TaxiL2State(passengers, locations);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		
		if(passengers.get(key.obName) != null){
			touchPassenger(key.obName).set(variableKey, value);
		}else if(locations.get(key.obName) != null){
			touchLocation(key.obName).set(variableKey, value);
		} else {
			throw new RuntimeException("ERROR: unable to set value for " + variableKey);
		}
		return this;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(o instanceof TaxiL2Passenger || o.className().equals(TaxiL2.CLASS_L2PASSENGER)){
			touchPassengers().put(o.name(), (TaxiL2Passenger) o);			
		}else if(o instanceof TaxiL2Location || o.className().equals(TaxiL2.CLASS_L2LOCATION)){
			touchLocations().put(o.name(), (TaxiL2Location) o);
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
	public TaxiL2Passenger touchPassenger(String passName){
		TaxiL2Passenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}
	
	public TaxiL2Location touchLocation(String locName){
		TaxiL2Location l = locations.get(locName).copy();
		touchLocations().remove(locName);
		locations.put(locName, l);
		return l;
	}
	
	public Map<String, TaxiL2Passenger> touchPassengers(){
		this.passengers = new HashMap<String, TaxiL2Passenger>(passengers);
		return passengers;
	}
	
	public Map<String, TaxiL2Location> touchLocations(){
		this.locations = new HashMap<String, TaxiL2Location>(locations);
		return locations;
	}
	
	//get values from objects
	public String[] getPassengers(){
		String[] ret = new String[passengers.size()];
		int i = 0;
		for(String name : passengers.keySet())
			ret[i++] = name;
		return ret;
	}
	
	public String[] getLocations(){
		String[] ret = new String[locations.size()];
		int i = 0;
		for(String name : locations.keySet())
			ret[i++] = name;
		return ret;
	}
	
	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}
	
	public Object getLocationAtt(String locName, String attName){
		return locations.get(locName).get(attName);
	}
}
