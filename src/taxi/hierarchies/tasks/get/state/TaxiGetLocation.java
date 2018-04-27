package taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;
import static taxi.TaxiConstants.*;

public class TaxiGetLocation extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList( );

	public TaxiGetLocation(String name) {
		this.setName(name);
	}

	@Override
	public String className() {
		return CLASS_LOCATION;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiGetLocation( objectName);
    }

	@Override
	public TaxiGetLocation copy() {
		return (TaxiGetLocation) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
