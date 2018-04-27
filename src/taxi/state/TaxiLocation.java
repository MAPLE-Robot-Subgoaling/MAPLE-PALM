package taxi.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import utilities.MutableObject;
import static taxi.TaxiConstants.*;

public class TaxiLocation extends MutableObject{ 

	/**
	 * standard x, y, color
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_X,
			ATT_Y,
			ATT_COLOR
			);
	
	public TaxiLocation(String name, int x, int y, String color) {
		this(name, (Object) x, (Object) y, (Object) color);
	}
	
	private TaxiLocation(String name, Object x, Object y, Object color) {
		this.set(ATT_X, x);
		this.set(ATT_Y, y);
		this.set(ATT_COLOR, color);
		this.setName(name);
	}
	@Override
	public String className() {
		return CLASS_LOCATION;
	}

	@Override
	public TaxiLocation copy() {
		return (TaxiLocation) copyWithName(name());
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiLocation(
				objectName,
				get(ATT_X),
				get(ATT_Y),
				get(ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
