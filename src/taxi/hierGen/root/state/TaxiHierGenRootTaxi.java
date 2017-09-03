package taxi.hierGen.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiHierGenRootTaxi extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y);

	public TaxiHierGenRootTaxi(String name, int x, int y){
		this(name, (Object) x, (Object) y);
	}

	private TaxiHierGenRootTaxi(String name, Object x, Object y){
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.setName(name);
	}

	@Override
	public String className() {
		return TaxiHierGenRootState.CLASS_ROOT_Taxi;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiHierGenRootTaxi(
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
	public TaxiHierGenRootTaxi copy() {
		return (TaxiHierGenRootTaxi) copyWithName(name());
	}
}
