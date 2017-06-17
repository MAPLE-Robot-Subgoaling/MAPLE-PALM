package taxi.abstraction2.state;

import java.util.Arrays; 
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.abstraction2.TaxiL2;
import utilities.MutableObject;

public class TaxiL2Passenger extends MutableObject {
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiL2.ATT_CURRENT_LOCATION,
			TaxiL2.ATT_IN_TAXI,
			TaxiL2.ATT_GOAL_LOCATION,
			TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE
			);
	
	public TaxiL2Passenger(String name, String currentLocation, String goalLocation) {
		this(name, (Object) currentLocation, (Object) goalLocation, false, false);
	}
	
	public TaxiL2Passenger(String name, String currentLocation, String goalLocation, boolean inTaxi){
		this(name, (Object) currentLocation, (Object) goalLocation, (Object) inTaxi, false);
	}

	public TaxiL2Passenger(String name, String currentLocation, String goalLocation, boolean inTaxi, boolean pickep){
		this(name, (Object) currentLocation, (Object) goalLocation, (Object) inTaxi, (Object) pickep);
	}
	
	private TaxiL2Passenger(String name, Object currentLocation, Object goalLocation, Object inTaxi,
			Object pickedUpOnce){
		this.set(TaxiL2.ATT_CURRENT_LOCATION, currentLocation);
		this.set(TaxiL2.ATT_IN_TAXI, inTaxi);
		this.set(TaxiL2.ATT_GOAL_LOCATION, goalLocation);
		this.set(TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE, pickedUpOnce);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiL2.CLASS_L2PASSENGER;
	}

	@Override
	public TaxiL2Passenger copy() {
		return (TaxiL2Passenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiL2Passenger(
				objectName,
				get(TaxiL2.ATT_CURRENT_LOCATION),
				get(TaxiL2.ATT_GOAL_LOCATION),
				get(TaxiL2.ATT_IN_TAXI),
				get(TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}}
