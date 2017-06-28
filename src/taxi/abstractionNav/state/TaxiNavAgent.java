package taxi.abstractionNav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

public class TaxiNavAgent extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y
			);
	
	public TaxiNavAgent(String name, int x, int y) {
		this(name, (Object) x, (Object) y);
	}
	
	private TaxiNavAgent(String name, Object x, Object y) {
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiNavState.CLASS_TAXI;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavAgent(
				objectName, 
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y)); 
	}

	@Override
	public TaxiNavAgent copy() {
		return (TaxiNavAgent) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
