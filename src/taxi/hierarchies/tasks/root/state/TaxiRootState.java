package taxi.hierarchies.tasks.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import taxi.Taxi;
import taxi.hierarchies.tasks.root.TaxiRootDomain;

import java.util.*;

public class TaxiRootState implements MutableOOState {

	//this state has passengers and depots
	private Map<String, TaxiRootPassenger> passengers;

	public TaxiRootState(List<TaxiRootPassenger> pass) {
		this.passengers = new HashMap<>();
		for(TaxiRootPassenger p : pass){
			this.passengers.put(p.name(), p);
		}
	}

	private TaxiRootState(Map<String, TaxiRootPassenger> pass) {
		this.passengers = pass;
	}

	@Override
	public int numObjects() {
		return passengers.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		ObjectInstance o = passengers.get(oname);
		if(o != null)
			return o;

		return null;
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> obj = new ArrayList<>();
		obj.addAll(passengers.values());
		return obj;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(Taxi.CLASS_PASSENGER))
			return new ArrayList<ObjectInstance>(passengers.values());
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
	public TaxiRootState copy() {
		return new TaxiRootState(passengers);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);

		if(passengers.get(key.obName) != null){
			touchPassenger(key.obName).set(variableKey, value);
		} else {
			throw new RuntimeException("ERROR: unable to set value for " + variableKey);
		}
		return this;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(o instanceof TaxiRootPassenger || o.className().equals(Taxi.CLASS_PASSENGER)){
			touchPassengers().put(o.name(), (TaxiRootPassenger) o);
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
	public TaxiRootPassenger touchPassenger(String passName){
		TaxiRootPassenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}

	public Map<String, TaxiRootPassenger> touchPassengers(){
		this.passengers = new HashMap<>(passengers);
		return passengers;
	}

	//get values from objects
	public String[] getPassengers(){
		String[] ret = new String[passengers.size()];
		int i = 0;
		for(String name : passengers.keySet())
			ret[i++] = name;
		return ret;
	}

	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}

	@Override
	public String toString(){
		String out = "{\n";

		for(TaxiRootPassenger p : passengers.values()){
			out += p.toString() + "\n";
		}
		return out;
	}
}
