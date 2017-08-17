package taxi.hierarchies.tasks.dropoff.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiDropoffAgent extends MutableObject{
	
	/**
	 * contains if taxi is occupied and where it is (onRoad or at depot
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiDropoffDomain.ATT_TAXI_OCCUPIED,
			TaxiDropoffDomain.ATT_CURRENT_LOCATION
			);
	
	public TaxiDropoffAgent(String name, String currentLocation, boolean taxiOccupied) {
		this(name, (Object) currentLocation, (Object) taxiOccupied);
	}
	
	private TaxiDropoffAgent(String name, Object currentLocation, Object taxiOccupied) {
		this.set(TaxiDropoffDomain.ATT_CURRENT_LOCATION, currentLocation);
		this.set(TaxiDropoffDomain.ATT_TAXI_OCCUPIED, taxiOccupied);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiDropoffDomain.CLASS_TAXI;
	}

	@Override
	public TaxiDropoffAgent copy() {
		return (TaxiDropoffAgent) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiDropoffAgent(
				objectName,
				get(TaxiDropoffDomain.ATT_CURRENT_LOCATION),
				get(TaxiDropoffDomain.ATT_TAXI_OCCUPIED)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
