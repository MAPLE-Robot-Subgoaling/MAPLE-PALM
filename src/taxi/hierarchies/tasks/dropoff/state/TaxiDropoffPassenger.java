package taxi.hierarchies.tasks.dropoff.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
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
			TaxiDropoffDomain.ATT_LOCATION
			);
	
	public TaxiDropoffPassenger(String name, String currentLocation) {
		this(name, (Object) currentLocation);
	}

	private TaxiDropoffPassenger(String name, Object currentLocation){
		this.set(TaxiDropoffDomain.ATT_LOCATION, currentLocation);
		this.setName(name);
	}

	@Override
	public String className() {
		return Taxi.CLASS_PASSENGER;
	}

	@Override
	public TaxiDropoffPassenger copy() {
		return (TaxiDropoffPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiDropoffPassenger(
				objectName,
				get(TaxiDropoffDomain.ATT_LOCATION)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
