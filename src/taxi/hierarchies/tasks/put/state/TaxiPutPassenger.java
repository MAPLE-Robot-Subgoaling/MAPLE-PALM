package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiPutPassenger extends MutableObject {

	/**
	 * current location, whether they are in taxi, the goal, whether they haven been picked up
	 * whether they have just been picked up and haven't changed goal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiPutDomain.ATT_GOAL_LOCATION,
			TaxiPutDomain.ATT_IN_TAXI
			);
	
	public TaxiPutPassenger(String name, String goalLocation) {
		this(name, (Object) goalLocation, false);
	}
	
	public TaxiPutPassenger(String name, String goalLocation, boolean inTaxi){
		this(name, (Object) goalLocation, (Object) inTaxi);
	}

	private TaxiPutPassenger(String name, Object goalLocation, Object inTaxi){
		this.set(TaxiPutDomain.ATT_GOAL_LOCATION, goalLocation);
		this.set(TaxiPutDomain.ATT_IN_TAXI, inTaxi);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return Taxi.CLASS_PASSENGER;
	}

	@Override
	public TaxiPutPassenger copy() {
		return (TaxiPutPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiPutPassenger(
				objectName,
				get(TaxiPutDomain.ATT_GOAL_LOCATION),
				get(TaxiPutDomain.ATT_IN_TAXI)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}}
