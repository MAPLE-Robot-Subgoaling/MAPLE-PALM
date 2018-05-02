package taxi.hierarchies.tasks.nav.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import utilities.MutableObject;import static taxi.TaxiConstants.*;


public class TaxiNavLocation extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_X,
			ATT_Y
			);
	
	public TaxiNavLocation(String name, int x, int y) {
		this(name, (Object) x, (Object) y);
	}
	
	private TaxiNavLocation(String name, Object x, Object y) {
		this.set(ATT_X, x);
		this.set(ATT_Y, y);
		this.setName(name);
	}
	@Override
	public String className() {
		return CLASS_LOCATION;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiNavLocation(
				objectName, 
				get(ATT_X),
				get(ATT_Y));
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
