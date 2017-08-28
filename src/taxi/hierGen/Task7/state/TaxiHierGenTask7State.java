package taxi.hierGen.Task7.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;

import java.util.*;

public class TaxiHierGenTask7State implements MutableOOState{

	public static final String CLASS_TASK7_PASSENGER = 		"task7Passenger";
	public static final String CLASS_TASK7_Taxi =			"Task7axi";

	private TaxiHierGenTask7Taxi taxi;
	private Map<String, TaxiHierGenTask7Passenger> passengers;

	public TaxiHierGenTask7State(TaxiHierGenTask7Taxi taxi, List<TaxiHierGenTask7Passenger> passes){
		this.taxi = taxi;

		this.passengers = new HashMap<String, TaxiHierGenTask7Passenger>();
		for(TaxiHierGenTask7Passenger passenger : passes){
			this.passengers.put(passenger.name(), passenger);
		}
	}

	private TaxiHierGenTask7State(TaxiHierGenTask7Taxi taxi, Map<String, TaxiHierGenTask7Passenger> passes){
		this.taxi = taxi;
		this.passengers = passes;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		throw new RuntimeException("Not needed for HierGen");
	}

	@Override
	public MutableOOState removeObject(String oname) {
		throw new RuntimeException("Not needed for HierGen");
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		throw new RuntimeException("Not needed for HierGen");
	}

	@Override
	public int numObjects() {
		return 1 + this.passengers.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		if(taxi.name().equals(oname))
			return taxi;
		return passengers.get(oname);
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>(passengers.values());
		objects.add(taxi);
		return objects;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(CLASS_TASK7_Taxi))
			return Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(CLASS_TASK7_PASSENGER))
			return new ArrayList<>(passengers.values());
		throw new RuntimeException("No object class " + oclass);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		return null;
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
		return new TaxiHierGenTask7State(taxi, passengers);
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
		return taxi.get(attName);
	}

	public String getTaxiName(){
		return taxi.name();
	}

	public Object getPassengerAtt(String passname, String attName){
		return passengers.get(passname).get(attName);
	}
}
