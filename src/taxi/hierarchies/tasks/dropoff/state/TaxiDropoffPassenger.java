package taxi.hierarchies.tasks.dropoff.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiDropoffPassenger extends MutableObject {

	/**
	 * current location, if they are in taxi, goal location, if they have been picked up,
	 * and if they have been just picked up 
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiDropoffDomain.ATT_CURRENT_LOCATION,
			TaxiDropoffDomain.ATT_IN_TAXI
			);
	
	public TaxiDropoffPassenger(String name, String currentLocation, boolean inTaxi) {
		this(name, (Object) currentLocation, (Object) inTaxi);
	}

	private TaxiDropoffPassenger(String name, Object currentLocation, Object inTaxi){
		this.set(TaxiDropoffDomain.ATT_CURRENT_LOCATION, currentLocation);
		this.set(TaxiDropoffDomain.ATT_IN_TAXI, inTaxi);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiDropoffDomain.CLASS_PASSENGER;
	}

	@Override
	public TaxiDropoffPassenger copy() {
		return (TaxiDropoffPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiDropoffPassenger(
				objectName,
				get(TaxiDropoffDomain.ATT_CURRENT_LOCATION),
				get(TaxiDropoffDomain.ATT_IN_TAXI)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
