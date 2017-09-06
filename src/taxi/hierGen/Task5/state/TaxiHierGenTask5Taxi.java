package taxi.hierGen.Task5.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiHierGenTask5Taxi extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y);

	public TaxiHierGenTask5Taxi(String name, int x, int y){
		this(name, (Object) x, (Object) y);
	}

	private TaxiHierGenTask5Taxi(String name, Object x, Object y){
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiHierGenTask5State.CLASS_ROOT_Taxi;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiHierGenTask5Taxi(
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
	public TaxiHierGenTask5Taxi copy() {
		return (TaxiHierGenTask5Taxi) copyWithName(name());
	}
}
