package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.hierarchies.tasks.put.TaxiPutDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxiPutState implements MutableOOState {

	//this state has passengers and depots
	private Map<String, TaxiPutPassenger> passengers;
	private Map<String, TaxiPutLocation> locations;
	
	public TaxiPutState(List<TaxiPutPassenger> pass, List<TaxiPutLocation> loc) {
		this.passengers = new HashMap<String, TaxiPutPassenger>();
		for(TaxiPutPassenger p : pass){
			this.passengers.put(p.name(), p);
		}
		
		this.locations = new HashMap<String, TaxiPutLocation>();
		for(TaxiPutLocation l : loc){
			this.locations.put(l.name(), l);
		}
	}
	
	private TaxiPutState(Map<String, TaxiPutPassenger> pass, Map<String, TaxiPutLocation> loc) {
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
		if(oclass.equals(TaxiPutDomain.CLASS_PASSENGER))
			return new ArrayList<ObjectInstance>(passengers.values());
		else if(oclass.equals(TaxiPutDomain.CLASS_LOCATION))
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
	public TaxiPutState copy() {
		return new TaxiPutState(passengers, locations);
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
		if(o instanceof TaxiPutPassenger || o.className().equals(TaxiPutDomain.CLASS_PASSENGER)){
			touchPassengers().put(o.name(), (TaxiPutPassenger) o);
		}else if(o instanceof TaxiPutLocation || o.className().equals(TaxiPutDomain.CLASS_LOCATION)){
			touchLocations().put(o.name(), (TaxiPutLocation) o);
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
	public TaxiPutPassenger touchPassenger(String passName){
		TaxiPutPassenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}
	
	public TaxiPutLocation touchLocation(String locName){
		TaxiPutLocation l = locations.get(locName).copy();
		touchLocations().remove(locName);
		locations.put(locName, l);
		return l;
	}
	
	public Map<String, TaxiPutPassenger> touchPassengers(){
		this.passengers = new HashMap<String, TaxiPutPassenger>(passengers);
		return passengers;
	}
	
	public Map<String, TaxiPutLocation> touchLocations(){
		this.locations = new HashMap<String, TaxiPutLocation>(locations);
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
	
	@Override
	public String toString(){
		String out = "{\n";
		
		for(TaxiPutPassenger p : passengers.values()){
			out += p.toString() + "\n";
		}
		
		for(TaxiPutLocation l : locations.values()){
			out += l.toString() + "\n";
		}
		return out;
	}
	}
