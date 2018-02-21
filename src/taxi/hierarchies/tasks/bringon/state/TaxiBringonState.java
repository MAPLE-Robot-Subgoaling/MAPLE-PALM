package taxi.hierarchies.tasks.bringon.state;

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
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;

public class TaxiBringonState implements MutableOOState {

	/**
	 * contain one taxi, and any number of depots and passengers  
	 */
	private TaxiBringonAgent taxi;
	private Map<String, TaxiBringonPassenger> passengers;

	public TaxiBringonState(TaxiBringonAgent t, List<TaxiBringonPassenger> pass) {
		this.taxi = t;
		
		this.passengers = new HashMap<>();
		for(TaxiBringonPassenger p : pass){
			this.passengers.put(p.name(), p);
		}
	}
	
	private TaxiBringonState(TaxiBringonAgent t, Map<String, TaxiBringonPassenger> pass) {
		this.taxi = t;
		this.passengers = pass;
	}
	@Override
	public int numObjects() {
		int total = taxi == null ? 0 : 1;
		total += passengers.size();
		return total;
	}

	@Override
	public ObjectInstance object(String oname) {
		if(taxi != null && oname.equals(taxi.name()))
			return taxi;
		
		ObjectInstance o = passengers.get(oname);
		if(o != null)
			return o;

		return null;
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> obj = new ArrayList<ObjectInstance>();
		if (taxi != null) {
			obj.add(taxi);
		}
		obj.addAll(passengers.values());
		return obj;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(Taxi.CLASS_TAXI))
            return taxi == null ? new ArrayList<ObjectInstance>() : Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(Taxi.CLASS_PASSENGER))
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
	public TaxiBringonState copy() {
		return new TaxiBringonState(touchTaxi(), touchPassengers());
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		
		if(taxi != null && key.obName.equals(taxi.name())){
			touchTaxi().set(variableKey, value);
		}else if(passengers.get(key.obName) != null){
			touchPassenger(key.obName).set(variableKey, value);
		} else {
			throw new RuntimeException("ERROR: unable to set value for " + variableKey);
		}
		return this;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(o instanceof TaxiBringonAgent || o.className().equals(Taxi.CLASS_TAXI)){
			touchTaxi();
			taxi = (TaxiBringonAgent) o;
		}else if(o instanceof TaxiBringonPassenger || o.className().equals(Taxi.CLASS_PASSENGER)){
			touchPassengers().put(o.name(), (TaxiBringonPassenger) o);
		}else{
			throw new RuntimeException("Can only add certain objects to state.");
		}
		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {
        ObjectInstance objectInstance = this.object(oname);
        if (objectInstance instanceof TaxiBringonAgent) {
//            touchTaxi();
            taxi = null;
        } else if (objectInstance instanceof TaxiBringonPassenger) {
            touchPassenger(oname);
            passengers.remove(oname);
        }
        return this;

    }

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		throw new RuntimeException("Rename not implemented");
	}

	//touch methods allow a shallow copy of states and a copy of objects only when modified
	public TaxiBringonAgent touchTaxi(){
		if (taxi != null) {
		    this.taxi = taxi.copy();
        }
		return taxi;
	}
	
	public TaxiBringonPassenger touchPassenger(String passName){
		TaxiBringonPassenger p = passengers.get(passName).copy();
		touchPassengers().remove(passName);
		passengers.put(passName, p);
		return p;
	}

	public Map<String, TaxiBringonPassenger> touchPassengers(){
		this.passengers = new HashMap<String, TaxiBringonPassenger>(passengers);
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
	
	public Object getTaxiAtt(String attName){
		if (taxi == null) {
		    return null;
        }
		return taxi.get(attName);
	}
	
	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}
	
	@Override
	public String toString(){
		String out = "{ " + this.getClass().getSimpleName() + "\n";

		if (taxi != null) {
            out += taxi.toString() + "\n";
        }

		for(TaxiBringonPassenger p : passengers.values()){
			out += p.toString() + "\n";
		}
		out += "}";
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TaxiBringonState that = (TaxiBringonState) o;

		if (taxi != null ? !taxi.equals(that.taxi) : that.taxi != null) return false;
		return passengers != null ? passengers.equals(that.passengers) : that.passengers == null;
	}

	@Override
	public int hashCode() {
		int result = taxi != null ? taxi.hashCode() : 0;
		result = 31 * result + (passengers != null ? passengers.hashCode() : 0);
		return result;
	}
}
