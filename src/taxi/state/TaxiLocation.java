package taxi.state;

import java.util.Arrays;
import java.util.Collections;
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
	
	/**
	 * nav state only has x, y
	 */
	private final static List<Object> navKeys = Arrays.<Object>asList(
			Taxi.ATT_X,
			Taxi.ATT_Y
			);
	
	/**
	 * flag for nav state
	 */
	private boolean nav = false;

	public TaxiLocation(String name, int x, int y, List<String> colors){
		this(name, x, y, (Object)colors);
	}

	public TaxiLocation(String name, int x, int y, String color) {
		this(name, x,y, Collections.singletonList(color));
	}
	
	private TaxiLocation(String name, Object x, Object y, Object colors) {
		this.set(Taxi.ATT_X, x);
		this.set(Taxi.ATT_Y, y);
		this.set(Taxi.ATT_COLOR, colors);
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
