package taxi.hierarchies.tasks.get.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import utilities.MutableObject;
import static taxi.TaxiConstants.*;

public class TaxiGetPassenger extends MutableObject {

	/**
	 * current location, whether they are in taxi, the goal, whether they haven been picked up
	 * whether they have just been picked up and haven't changed goal
	 */
	private final static List<Object> keys = Arrays.<Object>asList( ATT_LOCATION );
	
	public TaxiGetPassenger(String name, String currentLocation) {
		this(name, (Object) currentLocation);
	}

	private TaxiGetPassenger(String name, Object currentLocation){
		this.set(ATT_LOCATION, currentLocation);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return CLASS_PASSENGER;
	}

	@Override
	public TaxiGetPassenger copy() {
		return (TaxiGetPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiGetPassenger( objectName, get(ATT_LOCATION) );
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}}
