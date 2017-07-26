package taxi.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;

public class TaxiLocation extends MutableObject{ 

	/**
	 * standard x, y, color
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y,
			Taxi.ATT_COLOR
			);
	
	public TaxiLocation(String name, int x, int y, String color) {
		this(name, (Object) x, (Object) y, (Object) color);
	}
	
	private TaxiLocation(String name, Object x, Object y, Object color) {
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.set(Taxi.ATT_COLOR, color);
		this.setName(name);
	}
	@Override
	public String className() {
		return Taxi.CLASS_LOCATION;
	}

	@Override
	public TaxiLocation copy() {
		return (TaxiLocation) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiLocation(
				objectName,
				get(Taxi.ATT_X),
				get(Taxi.ATT_Y),
				get(Taxi.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
