package taxi.hierGen.root.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;

import java.util.*;

public class TaxiHierGenRootState implements MutableOOState{

	public static final String CLASS_ROOT_PASSENGER = 		"rootPassenger";
	public static final String CLASS_ROOT_Taxi =			"rootTaxi";
	public static final String ATT_DESTINAION_X = 			"destX";
	public static final String ATT_DESTINAION_Y = 			"destY";

	private TaxiHierGenRootTaxi taxi;
	private Map<String, TaxiHierGenRootPassenger> passengers;

	public TaxiHierGenRootState(TaxiHierGenRootTaxi taxi, List<TaxiHierGenRootPassenger> passes){
		this.taxi = taxi;

		this.passengers = new HashMap<String, TaxiHierGenRootPassenger>();
		for(TaxiHierGenRootPassenger passenger : passes){
			this.passengers.put(passenger.name(), passenger);
		}
	}

	private TaxiHierGenRootState(TaxiHierGenRootTaxi taxi, Map<String, TaxiHierGenRootPassenger> passes){
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
		if(oclass.equals(CLASS_ROOT_Taxi))
			return Arrays.<ObjectInstance>asList(taxi);
		else if(oclass.equals(CLASS_ROOT_PASSENGER))
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
		return new TaxiHierGenRootState(taxi, passengers);
	}
}
