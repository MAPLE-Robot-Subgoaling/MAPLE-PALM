package taxi.hierGen.Task7.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiHierGenTask7Passenger extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y,
			Taxi.ATT_IN_TAXI
	);

	public TaxiHierGenTask7Passenger(String name, int x, int y,  boolean inTaxi){
		this(name, (Object) x, (Object) y, (Object) inTaxi);
	}

	private TaxiHierGenTask7Passenger(String name, Object x, Object y, Object inTaxi){
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.set(Taxi.ATT_IN_TAXI, inTaxi);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiHierGenTask7State.CLASS_TASK7_PASSENGER;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiHierGenTask7Passenger(
				objectName,
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y),
				get(Taxi.ATT_IN_TAXI)
		);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public TaxiHierGenTask7Passenger copy() {
		return (TaxiHierGenTask7Passenger) copyWithName(name());
	}
}
