package taxi.hierarchies.tasks.get.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import utilities.MutableObject;

public class TaxiGetLocation extends MutableObject {

	/**
	 * contains the color
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiGetDomain.ATT_COLOR
			);
	
	public TaxiGetLocation(String name, String color) {
		this(name, (Object) color);
	}
	
	private TaxiGetLocation(String name, Object color) {
		this.set(TaxiGetDomain.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiGetDomain.CLASS_LOCATION;
	}

	@Override
	public TaxiGetLocation copy() {
		return (TaxiGetLocation) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiGetLocation(
				objectName, 
				get(TaxiGetDomain.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
