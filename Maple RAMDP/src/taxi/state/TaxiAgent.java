package taxi.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import utilities.MutableObject;

public class TaxiAgent extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y,
			Taxi.ATT_TAXI_OCCUPIED
			);

	public TaxiAgent(String name, int x, int y) {
		this(name, x, y, false);
	}
	
	public TaxiAgent(String name, int x, int y, boolean taxiOccupied) {
		this(name, (Object) x, (Object) y, (Object) taxiOccupied);
	}
	
	private TaxiAgent(String name, Object x, Object y, Object taxiOccupied) {
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.set(Taxi.ATT_TAXI_OCCUPIED, taxiOccupied);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return Taxi.CLASS_TAXI;
	}

	@Override
	public TaxiAgent copy() {
		return (TaxiAgent) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiAgent(
				objectName,
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y),
				get(Taxi.ATT_TAXI_OCCUPIED)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
