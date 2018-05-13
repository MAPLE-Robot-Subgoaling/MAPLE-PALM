package taxi.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

import static taxi.TaxiConstants.*;
public class TaxiAgent extends MutableObject {

	/**
	 * x, y, taxi occupied 
	 * if the state is normal
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_X,
			ATT_Y
//			ATT_TAXI_OCCUPIED
			);

//	public TaxiAgent(String name, int x, int y) {
//		this(name, x, y, false);
//	}
//
//	public TaxiAgent(String name, int x, int y, boolean taxiOccupied) {
//		this(name, (Object) x, (Object) y, (Object) taxiOccupied);
//	}
	
	public TaxiAgent(String name, int x, int y) { //}, Object taxiOccupied) {
		this.set(ATT_X, x);
		this.set(ATT_Y, y);
//		this.set(ATT_TAXI_OCCUPIED, taxiOccupied);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return CLASS_TAXI;
	}

	@Override
	public TaxiAgent copy() {
		return (TaxiAgent) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiAgent(
				objectName,
				(Integer) get(ATT_X),
				(Integer) get(ATT_Y)
//				get(ATT_TAXI_OCCUPIED)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
