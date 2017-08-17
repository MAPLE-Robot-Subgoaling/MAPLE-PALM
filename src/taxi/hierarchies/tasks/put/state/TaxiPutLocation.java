package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiPutLocation extends MutableObject {

	/**
	 * contains the color
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiGetDomain.ATT_COLOR
			);
	
	public TaxiPutLocation(String name, String color) {
		this(name, (Object) color);
	}
	
	private TaxiPutLocation(String name, Object color) {
		this.set(TaxiGetDomain.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiGetDomain.CLASS_LOCATION;
	}

	@Override
	public TaxiPutLocation copy() {
		return (TaxiPutLocation) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiPutLocation(
				objectName, 
				get(TaxiGetDomain.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
