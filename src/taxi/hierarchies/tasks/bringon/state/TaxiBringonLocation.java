package taxi.hierarchies.tasks.bringon.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import utilities.MutableObject;

public class TaxiBringonLocation extends MutableObject{

	/**
	 * color of location
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiBringonDomain.ATT_COLOR
			);
	
	public TaxiBringonLocation(String name, String color) {
		this(name, (Object) color);
	}
	
	private TaxiBringonLocation(String name, Object color) {
		this.set(TaxiBringonDomain.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiBringonDomain.CLASS_LOCATION;
	}

	@Override
	public TaxiBringonLocation copy() {
		return (TaxiBringonLocation) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiBringonLocation(
				objectName, 
				get(TaxiBringonDomain.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
