package taxi.abstraction2.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.abstraction2.TaxiL2;
import utilities.MutableObject;

public class TaxiL2Passenger extends MutableObject {

	/**
	 * current location, whether they are in taxi, the goal, whether they haven been picked up
	 * whether they have just been picked up and haven't changed goal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiL2.ATT_CURRENT_LOCATION,
			TaxiL2.ATT_IN_TAXI,
			TaxiL2.ATT_GOAL_LOCATION,
			TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE,
			TaxiL2.ATT_JUST_PICKED_UP
			);
	
	public TaxiL2Passenger(String name, String currentLocation, String goalLocation) {
		this(name, (Object) currentLocation, (Object) goalLocation, false, false, false);
	}
	
	public TaxiL2Passenger(String name, String currentLocation, String goalLocation, boolean inTaxi){
		this(name, (Object) currentLocation, (Object) goalLocation, (Object) inTaxi, false, false);
	}

	public TaxiL2Passenger(String name, String currentLocation, String goalLocation, boolean inTaxi, boolean pickep,
			boolean justpickedup){
		this(name, (Object) currentLocation, (Object) goalLocation, (Object) inTaxi, (Object) pickep, (Object) justpickedup);
	}
	
	private TaxiL2Passenger(String name, Object currentLocation, Object goalLocation, Object inTaxi,
			Object pickedUpOnce, Object justPickedUp){
		this.set(TaxiL2.ATT_CURRENT_LOCATION, currentLocation);
		this.set(TaxiL2.ATT_IN_TAXI, inTaxi);
		this.set(TaxiL2.ATT_GOAL_LOCATION, goalLocation);
		this.set(TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE, pickedUpOnce);
		this.set(TaxiL2.ATT_JUST_PICKED_UP, justPickedUp);
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
				get(TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE),
				get(TaxiL2.ATT_JUST_PICKED_UP)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}}
