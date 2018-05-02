package taxi.hierarchies.tasks.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.*;
import static taxi.TaxiConstants.*;
public class TaxiRootPassenger extends MutableObject {

	/**
	 * current location, whether they are in taxi, the goal, whether they haven been picked up
	 * whether they have just been picked up and haven't changed goal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_CURRENT_LOCATION,
			ATT_GOAL_LOCATION
			);
	
	public TaxiRootPassenger(String name, String currentLocation) {
		this(name, (Object) currentLocation, null);
	}

	public TaxiRootPassenger(String name, String currentLocation, String goalLocation) {
		this(name, (Object) currentLocation, (Object) goalLocation);
	}

	private TaxiRootPassenger(String name, Object currentLocation, Object goalLocation){
		this.set(ATT_CURRENT_LOCATION, currentLocation);
		this.set(ATT_GOAL_LOCATION, goalLocation);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return CLASS_PASSENGER;
	}

	@Override
	public TaxiRootPassenger copy() {
		return (TaxiRootPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiRootPassenger(
				objectName,
				get(ATT_CURRENT_LOCATION),
				get(ATT_GOAL_LOCATION)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}}
