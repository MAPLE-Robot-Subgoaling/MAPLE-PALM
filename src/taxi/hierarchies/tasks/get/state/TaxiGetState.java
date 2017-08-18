package taxi.hierarchies.tasks.get.state;

import java.util.*;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import cern.colt.list.adapter.ObjectListAdapter;
import taxi.hierarchies.tasks.get.TaxiGetDomain;

public class TaxiGetState implements MutableOOState {

	//this state has passengers and depots
	private TaxiGetAgent taxi;
	private Map<String, TaxiGetPassenger> passengers;

	public TaxiGetState(TaxiGetAgent taxi, List<TaxiGetPassenger> pass) {
		this.taxi = taxi;
		this.passengers = new HashMap<String, TaxiGetPassenger>();
		for(TaxiGetPassenger p : pass){
			this.passengers.put(p.name(), p);
		}
	}
	
	private TaxiGetState(TaxiGetAgent taxi, Map<String, TaxiGetPassenger> pass) {
		this.taxi = taxi;
		this.passengers = pass;
	}
	
	@Override
	public int numObjects() {
		return 1 + passengers.size();
	}

	@Override
	public ObjectInstance object(String oname) {
	    if(oname.equals(taxi.name())) {
	    	return taxi;
		}

		ObjectInstance o = passengers.get(oname);
		if(o != null)
			return o;

		return null;
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> obj = new ArrayList<ObjectInstance>();
		obj.add(taxi);
		obj.addAll(passengers.values());
		return obj;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
	    if(oclass.equals(TaxiGetDomain.CLASS_TAXI))
	        return Arrays.<ObjectInstance>asList(taxi);
		if(oclass.equals(TaxiGetDomain.CLASS_PASSENGER))
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
	public TaxiGetState copy() {
		return new TaxiGetState(taxi, passengers);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);

		if(key.obName.equals(taxi.name())) {
			touchTaxi().set(variableKey, value);
		} else if(passengers.get(key.obName) != null){
			touchPassenger(key.obName).set(variableKey, value);
		} else {
			throw new RuntimeException("ERROR: unable to set value for " + variableKey);
		}
		return this;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
	    if(o instanceof TaxiGetAgent || o.className().equals(TaxiGetDomain.CLASS_TAXI)) {
	    	taxi = (TaxiGetAgent)o;
		} else if(o instanceof TaxiGetPassenger || o.className().equals(TaxiGetDomain.CLASS_PASSENGER)){
			touchPassengers().put(o.name(), (TaxiGetPassenger) o);
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
	public TaxiGetAgent touchTaxi() {
	    this.taxi = taxi.copy();
	    return taxi;
	}

	public TaxiGetPassenger touchPassenger(String passName){
		TaxiGetPassenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}

	public Map<String, TaxiGetPassenger> touchPassengers(){
		this.passengers = new HashMap<String, TaxiGetPassenger>(passengers);
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

	public Object getTaxiAtt(String attName) {
		return taxi.get(attName);
	}

	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}

	@Override
	public String toString(){
		String out = "{\n";

		out += taxi.toString() + "\n";
		
		for(TaxiGetPassenger p : passengers.values()){
			out += p.toString() + "\n";
		}
		return out;
	}
	}
