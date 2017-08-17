package taxi.hierarchies.tasks.root.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiRootLocation extends MutableObject {

	/**
	 * contains the color
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiRootDomain.ATT_COLOR
			);
	
	public TaxiRootLocation(String name, String color) {
		this(name, (Object) color);
	}
	
	private TaxiRootLocation(String name, Object color) {
		this.set(TaxiRootDomain.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiRootDomain.CLASS_LOCATION;
	}

	@Override
	public TaxiRootLocation copy() {
		return (TaxiRootLocation) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiRootLocation(
				objectName, 
				get(TaxiRootDomain.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
