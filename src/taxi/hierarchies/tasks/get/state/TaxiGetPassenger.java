package taxi.hierarchies.tasks.get.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import utilities.MutableObject;

public class TaxiGetPassenger extends MutableObject {

	/**
	 * current location, whether they are in taxi, the goal, whether they haven been picked up
	 * whether they have just been picked up and haven't changed goal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiGetDomain.ATT_CURRENT_LOCATION,
			TaxiGetDomain.ATT_IN_TAXI
			);
	
	public TaxiGetPassenger(String name, String currentLocation) {
		this(name, (Object) currentLocation, false);
	}
	
	public TaxiGetPassenger(String name, String currentLocation, boolean inTaxi){
		this(name, (Object) currentLocation, (Object) inTaxi);
	}

	private TaxiGetPassenger(String name, Object currentLocation, Object inTaxi){
		this.set(TaxiGetDomain.ATT_CURRENT_LOCATION, currentLocation);
		this.set(TaxiGetDomain.ATT_IN_TAXI, inTaxi);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiGetDomain.CLASS_PASSENGER;
	}

	@Override
	public TaxiGetPassenger copy() {
		return (TaxiGetPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiGetPassenger(
				objectName,
				get(TaxiGetDomain.ATT_CURRENT_LOCATION),
				get(TaxiGetDomain.ATT_IN_TAXI)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}}
