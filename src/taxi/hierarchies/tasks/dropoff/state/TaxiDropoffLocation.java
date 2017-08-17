package taxi.hierarchies.tasks.dropoff.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class TaxiDropoffLocation extends MutableObject{

	/**
	 * color of location
	 */
	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiBringonDomain.ATT_COLOR
			);
	
	public TaxiDropoffLocation(String name, String color) {
		this(name, (Object) color);
	}
	
	private TaxiDropoffLocation(String name, Object color) {
		this.set(TaxiBringonDomain.ATT_COLOR, color);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return TaxiBringonDomain.CLASS_LOCATION;
	}

	@Override
	public TaxiDropoffLocation copy() {
		return (TaxiDropoffLocation) copyWithName(name());
	}
	
	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiDropoffLocation(
				objectName, 
				get(TaxiBringonDomain.ATT_COLOR)
				);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}
}
