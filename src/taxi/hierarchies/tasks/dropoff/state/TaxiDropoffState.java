package taxi.hierarchies.tasks.dropoff.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;

import java.util.*;

public class TaxiDropoffState implements MutableOOState{

	/**
	 * contain one taxi, and any number of depots and passengers  
	 */
	private TaxiDropoffAgent taxi;
	private Map<String, TaxiDropoffPassenger> passengers;
	private Map<String, TaxiDropoffLocation> locations;

	public TaxiDropoffState(TaxiDropoffAgent t, List<TaxiDropoffPassenger> pass, List<TaxiDropoffLocation> loc) {
		this.taxi = t;

		this.passengers = new HashMap<String, TaxiDropoffPassenger>();
		for(TaxiDropoffPassenger p : pass){
			this.passengers.put(p.name(), p);
		}

		this.locations = new HashMap<String, TaxiDropoffLocation>();
		for(TaxiDropoffLocation l : loc){
			this.locations.put(l.name(), l);
		}
	}

	private TaxiDropoffState(TaxiDropoffAgent t, Map<String, TaxiDropoffPassenger> pass, Map<String, TaxiDropoffLocation> loc) {
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
		if(oclass.equals(TaxiBringonDomain.CLASS_TAXI))
			return Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(TaxiBringonDomain.CLASS_PASSENGER))
			return new ArrayList<ObjectInstance>(passengers.values());
		else if(oclass.equals(TaxiBringonDomain.CLASS_LOCATION))
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
	public TaxiDropoffState copy() {
		return new TaxiDropoffState(taxi, passengers, locations);
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
		if(o instanceof TaxiDropoffAgent || o.className().equals(TaxiBringonDomain.CLASS_TAXI)){
			touchTaxi();
			taxi = (TaxiDropoffAgent) o;
		}else if(o instanceof TaxiDropoffPassenger || o.className().equals(TaxiBringonDomain.CLASS_PASSENGER)){
			touchPassengers().put(o.name(), (TaxiDropoffPassenger) o);
		}else if(o instanceof TaxiDropoffLocation || o.className().equals(TaxiBringonDomain.CLASS_LOCATION)){
			touchLocations().put(o.name(), (TaxiDropoffLocation) o);
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
	public TaxiDropoffAgent touchTaxi(){
		this.taxi = taxi.copy();
		return taxi;
	}

	public TaxiDropoffPassenger touchPassenger(String passName){
		TaxiDropoffPassenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}

	public TaxiDropoffLocation touchLocation(String locName){
		TaxiDropoffLocation l = locations.get(locName).copy();
		touchLocations().remove(locName);
		locations.put(locName, l);
		return l;
	}

	public Map<String, TaxiDropoffPassenger> touchPassengers(){
		this.passengers = new HashMap<String, TaxiDropoffPassenger>(passengers);
		return passengers;
	}

	public Map<String, TaxiDropoffLocation> touchLocations(){
		this.locations = new HashMap<String, TaxiDropoffLocation>(locations);
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
		
		for(TaxiDropoffPassenger p : passengers.values()){
			out += p.toString() + "\n";
		}
		
		for(TaxiDropoffLocation l : locations.values()){
			out += l.toString() + "\n";
		}
		return out;
	}
}
