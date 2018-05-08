package taxi.hierarchies.tasks.put.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.Taxi;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;
import static taxi.TaxiConstants.*;

public class TaxiPutAgent extends MutableObject {

	private final static List<Object> keys = Arrays.<Object>asList(
			ATT_LOCATION
			);

	public TaxiPutAgent(String name, String location) {
		this(name, (Object)location);
	}

	private TaxiPutAgent(String name, Object location) {
	    this.set(ATT_LOCATION, location);
		this.setName(name);
	}
	
	@Override
	public String className() {
		return CLASS_TAXI;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new TaxiPutAgent( objectName, get(ATT_LOCATION));
	}

	@Override
	public TaxiPutAgent copy() {
		return (TaxiPutAgent) copyWithName(name());
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

}
