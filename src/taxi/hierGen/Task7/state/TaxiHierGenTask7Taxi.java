package taxi.hierGen.Task7.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiHierGenTask7Taxi extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y);

	public TaxiHierGenTask7Taxi(String name, int x, int y){
		this(name, (Object) x, (Object) y);
	}

	private TaxiHierGenTask7Taxi(String name, Object x, Object y){
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiHierGenTask7State.CLASS_TASK7_Taxi;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiHierGenTask7Taxi(
				objectName,
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y)
		);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public TaxiHierGenTask7Taxi copy() {
		return (TaxiHierGenTask7Taxi) copyWithName(name());
	}
}
