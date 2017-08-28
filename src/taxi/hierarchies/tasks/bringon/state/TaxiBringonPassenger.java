package taxi.hierarchies.tasks.bringon.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import utilities.MutableObject;

public class TaxiBringonPassenger extends MutableObject {

	/**
	 * current location, if they are in taxi, goal location, if they have been picked up,
	 * and if they have been just picked up 
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiBringonDomain.ATT_LOCATION
			);
	
	public TaxiBringonPassenger(String name, String currentLocation) {
		this(name, (Object) currentLocation);
	}

	private TaxiBringonPassenger(String name, Object currentLocation){
		this.set(TaxiBringonDomain.ATT_LOCATION, currentLocation);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiBringonDomain.CLASS_PASSENGER;
	}

	@Override
	public TaxiBringonPassenger copy() {
		return (TaxiBringonPassenger) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiBringonPassenger( objectName, get(TaxiBringonDomain.ATT_LOCATION) );
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
