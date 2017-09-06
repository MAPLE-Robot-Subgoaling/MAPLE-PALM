package taxi.hierGen.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiHierGenRootPassenger extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y,
			TaxiHierGenRootState.ATT_DESTINAION_X,
			TaxiHierGenRootState.ATT_DESTINAION_Y,
			Taxi.ATT_IN_TAXI
	);

	public TaxiHierGenRootPassenger(String name, int x, int y, int destX, int destY, boolean inTaxi){
		this(name, (Object) x, (Object) y, (Object) destX, (Object) destY, (Object) inTaxi);
	}

	private TaxiHierGenRootPassenger(String name, Object x, Object y, Object destX, Object destY, Object inTaxi){
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.set(TaxiHierGenRootState.ATT_DESTINAION_X, destX);
		this.set(TaxiHierGenRootState.ATT_DESTINAION_Y, destY);
		this.set(Taxi.ATT_IN_TAXI, inTaxi);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiHierGenRootState.CLASS_ROOT_PASSENGER;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiHierGenRootPassenger(
				objectName,
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y),
				get(TaxiHierGenRootState.ATT_DESTINAION_X),
				get(TaxiHierGenRootState.ATT_DESTINAION_Y),
				get(Taxi.ATT_IN_TAXI)
		);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public TaxiHierGenRootPassenger copy() {
		return (TaxiHierGenRootPassenger) copyWithName(name());
	}
}
