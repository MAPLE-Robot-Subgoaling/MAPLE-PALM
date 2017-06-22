package taxi.abstraction1.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.abstraction1.TaxiL1;
import utilities.MutableObject;

public class TaxiL1Agent extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiL1.ATT_TAXI_OCCUPIED,
			TaxiL1.ATT_CURRENT_LOCATION
			);
	
	public TaxiL1Agent(String name, String currentLocation, boolean taxiOccupied) {
		this(name, (Object) currentLocation, (Object) taxiOccupied);
	}
	
	private TaxiL1Agent(String name, Object currentLocation, Object taxiOccupied) {
		this.set(TaxiL1.ATT_CURRENT_LOCATION, currentLocation);
		this.set(TaxiL1.ATT_TAXI_OCCUPIED, taxiOccupied);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiL1.CLASS_L1TAXI;
	}

	@Override
	public TaxiL1Agent copy() {
		return (TaxiL1Agent) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiL1Agent(
				objectName,
				get(TaxiL1.ATT_CURRENT_LOCATION),
				get(TaxiL1.ATT_TAXI_OCCUPIED)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
