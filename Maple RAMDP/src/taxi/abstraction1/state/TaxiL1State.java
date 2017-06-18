package taxi.abstraction1.state;

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
import taxi.abstraction1.TaxiL1;

public class TaxiL1State implements MutableOOState{

	private TaxiL1Agent taxi;
	private Map<String, TaxiL1Passenger> passengers;
	private Map<String, TaxiL1Location> locations;
	
	public TaxiL1State(TaxiL1Agent t, List<TaxiL1Passenger> pass, List<TaxiL1Location> loc) {
		this.taxi = t;
		
		this.passengers = new HashMap<String, TaxiL1Passenger>();
		for(TaxiL1Passenger p : pass){
			this.passengers.put(p.name(), p);
		}
		
		this.locations = new HashMap<String, TaxiL1Location>();
		for(TaxiL1Location l : loc){
			this.locations.put(l.name(), l);
		}
	}
	
	private TaxiL1State(TaxiL1Agent t, Map<String, TaxiL1Passenger> pass, Map<String, TaxiL1Location> loc) {
		this.taxi = t;
		this.passengers = pass;
		this.locations = loc;
	}
	@Override
	public int numObjects() {
		return 1 + passengers.size() + locations.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		if(oname.equals(taxi.name()))
			return taxi;
		
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
		obj.add(taxi);
		obj.addAll(passengers.values());
		obj.addAll(locations.values());
		return obj;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(TaxiL1.CLASS_L1TAXI))
			return Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(TaxiL1.CLASS_L1PASSENGER))
			return new ArrayList<ObjectInstance>(passengers.values());
		else if(oclass.equals(TaxiL1.CLASS_L1LOCATION))
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
	public TaxiL1State copy() {
		return new TaxiL1State(taxi, passengers, locations);
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
		} else {
			throw new RuntimeException("ERROR: unable to set value for " + variableKey);
		}
		return this;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(o instanceof TaxiL1Agent || o.className().equals(TaxiL1.CLASS_L1TAXI)){
			touchTaxi();
			taxi = (TaxiL1Agent) o;
		}else if(o instanceof TaxiL1Passenger || o.className().equals(TaxiL1.CLASS_L1PASSENGER)){
			touchPassengers().put(o.name(), (TaxiL1Passenger) o);			
		}else if(o instanceof TaxiL1Location || o.className().equals(TaxiL1.CLASS_L1LOCATION)){
			touchLocations().put(o.name(), (TaxiL1Location) o);
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
	public TaxiL1Agent touchTaxi(){
		this.taxi = taxi.copy();
		return taxi;
	}
	
	public TaxiL1Passenger touchPassenger(String passName){
		TaxiL1Passenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}
	
	public TaxiL1Location touchLocation(String locName){
		TaxiL1Location l = locations.get(locName).copy();
		touchLocations().remove(locName);
		locations.put(locName, l);
		return l;
	}
	
	public Map<String, TaxiL1Passenger> touchPassengers(){
		this.passengers = new HashMap<String, TaxiL1Passenger>(passengers);
		return passengers;
	}
	
	public Map<String, TaxiL1Location> touchLocations(){
		this.locations = new HashMap<String, TaxiL1Location>(locations);
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
	
	public Object getTaxiAtt(String attName){
		return taxi.get(attName);
	}
	
	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}
	
	public Object getLocationAtt(String locName, String attName){
		return locations.get(locName).get(attName);
	}
	
	@Override
	public String toString(){
		String out = "{\n";
		out += taxi.toString() + "\n";
		
		for(TaxiL1Passenger p : passengers.values()){
			out += p.toString() + "\n";
		}
		
		for(TaxiL1Location l : locations.values()){
			out += l.toString() + "\n";
		}
		return out;
	}
}
