package taxi.abstraction2.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.abstraction2.TaxiL2;
import utilities.MutableObject;

public class TaxiL2Location extends MutableObject {

	/**
	 * contains the color
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiL2.ATT_COLOR
			);
	
	public TaxiL2Location(String name, List<String> color) {
		this(name, (Object) color);
	}
	
	private TaxiL2Location(String name, Object color) {
		this.set(TaxiL2.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiL2.CLASS_L2LOCATION;
	}

	@Override
	public TaxiL2Location copy() {
		return (TaxiL2Location) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiL2Location(
				objectName, 
				get(TaxiL2.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
