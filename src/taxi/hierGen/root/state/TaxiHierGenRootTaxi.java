package taxi.hierGen.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;
import static taxi.TaxiConstants.*;

public class TaxiHierGenRootTaxi extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_X,
			ATT_Y);

	public TaxiHierGenRootTaxi(String name, int x, int y){
		this(name, (Object) x, (Object) y);
	}

	private TaxiHierGenRootTaxi(String name, Object x, Object y){
		this.set(ATT_X, x);
		this.set(ATT_Y, y);
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
				get(ATT_X),
				get(ATT_Y)
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
