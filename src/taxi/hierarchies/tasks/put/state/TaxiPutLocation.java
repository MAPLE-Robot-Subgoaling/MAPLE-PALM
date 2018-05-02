package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;
import static taxi.TaxiConstants.*;

public class TaxiPutLocation extends MutableObject{

	private final static List<Object> keys = Arrays.<Object>asList( );

	public TaxiPutLocation(String name) {
		this.setName(name);;
	}

	@Override
	public String className() {
		return CLASS_LOCATION;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiPutLocation( objectName);
    }

	@Override
	public TaxiPutLocation copy() {
		return (TaxiPutLocation) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
