package taxi.hierarchies.tasks.nav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import utilities.MutableObject;

public class TaxiNavLocation extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			TaxiNavDomain.ATT_X,
			TaxiNavDomain.ATT_Y
			);
	
	public TaxiNavLocation(String name, int x, int y) {
		this(name, (Object) x, (Object) y);
	}
	
	private TaxiNavLocation(String name, Object x, Object y) {
		this.set(TaxiNavDomain.ATT_X, x);
		this.set(TaxiNavDomain.ATT_Y, y);
		this.setName(name);;
	}
	@Override
	public String className() {
		return Taxi.CLASS_LOCATION;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavLocation(
				objectName, 
				get(TaxiNavDomain.ATT_X),
				get(TaxiNavDomain.ATT_Y));
	}

	@Override
	public TaxiNavLocation copy() {
		return (TaxiNavLocation) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
